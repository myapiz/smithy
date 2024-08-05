package com.myapiz.smithy4s.middleware

import cats.effect.{IO, IOLocal}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.myapiz.smithy.auth.*
import com.myapiz.smithy.error.*
import org.http4s.client.Client
import org.http4s.{Header, HttpApp}
import org.typelevel.ci.CIString
import smithy4s.Endpoint.Middleware
import smithy4s.Endpoint.Middleware.Standard
import smithy4s.http4s.{ClientEndpointMiddleware, ServerEndpointMiddleware}
import smithy4s.{Hints, ShapeId}

import scala.util.Try

object AuthMiddleware {

  case class AuthData(clientId: String, email: String, scope: String) {

    lazy val permissions: Map[String, Set[Permission]] = parseScope(scope)

    private def parseScope(scope: String): Map[String, Set[Permission]] =
      scope
        .split(',')
        .map(scope => {
          val s = scope.trim
          val service_perm = s.split(":").take(2)
          val perm =
            service_perm.drop(1).headOption.map(_.toLowerCase).fold(Nil) {
              case "*"                => Permission.values
              case "read"             => Permission.READ :: Nil
              case "write"            => Permission.WRITE :: Nil
              case "exec" | "execute" => Permission.EXECUTE :: Nil
            }
          service_perm(0) -> perm
        })
        .groupBy(_._1)
        .map { case (k, v) => k -> v.flatMap(_._2).toSet }
  }

  given aAuthDataCodec: JsonValueCodec[AuthData] = JsonCodecMaker.make

  private def decodeAuthData(encodedData: String) = {
    Try(readFromString[AuthData](encodedData))
      .fold(
        err => IO.raiseError(new NotAuthorizedError(s"Invalid AuthData: $err")),
        data => IO.pure(data)
      )
  }

  private def middleware(
      headerName: String,
      local: IOLocal[Option[AuthData]]
  ): HttpApp[IO] => HttpApp[IO] = { inputApp =>
    HttpApp[IO] { request => // 3
      val maybeKey = request.headers
        .get(CIString(headerName))
        // first header value
        .map(_.head.value)
        // is decoded to AuthData
        .map(decodeAuthData)
        .getOrElse(
          IO.raiseError(
            new NotAuthenticatedError(s"Missing AuthData in $headerName")
          )
        )

      for {
        data <- maybeKey
        _ <- local.set(Some(data))
        response <- inputApp(request)
      } yield response
    }
  }

  def apply(local: IOLocal[Option[AuthData]]): ServerEndpointMiddleware[IO] =
    new ServerEndpointMiddleware.Simple[IO] {

      def prepareWithHints(
          serviceHints: Hints,
          endpointHints: Hints
      ): HttpApp[IO] => HttpApp[IO] = {
        val hint = endpointHints
          .get[smithy.api.HttpApiKeyAuth]
          .orElse(serviceHints.get[smithy.api.HttpApiKeyAuth])
        hint match {
          case Some(authHint) => middleware(authHint.name.toString, local)
          case None           => identity
        }
      }

    }
}

object AuthzMiddleware {
  import AuthMiddleware.*

  private def middleware(
      serviceName: String,
      permissions: Set[Permission],
      local: IOLocal[Option[AuthData]]
  ): HttpApp[IO] => HttpApp[IO] = { inputApp =>
    HttpApp[IO] { request => // 3
      for {
        authDataLocal <- local.get
        authData <- IO.fromOption(authDataLocal)(
          new NotAuthorizedError("request not authorized without credentials")
        )
        requestServicePermissions = authData.permissions.getOrElse(
          serviceName,
          Set.empty
        )
        _ <- IO
          .raiseWhen(permissions.intersect(requestServicePermissions).isEmpty)(
            new NotAuthorizedError(
              "request not authorized with given permissions"
            )
          )
        response <- inputApp(request)
      } yield response
    }
  }

  def apply(
      local: IOLocal[Option[AuthData]],
      scopeName: Option[String] = None
  ): ServerEndpointMiddleware[IO] =
    new Standard[HttpApp[IO]] {
      def prepare(
          serviceId: ShapeId,
          endpointId: ShapeId,
          serviceHints: Hints,
          endpointHints: Hints
      ): HttpApp[IO] => HttpApp[IO] = {
        // deduce the required scope prefix from the service name in smithy definition
        // e.g. for service Sheet, the scope prefix is sheet
        val serviceName = scopeName.getOrElse(serviceId.name).toLowerCase
        val hint = endpointHints
          .get[Authorization]
          .orElse(serviceHints.get[Authorization])
        hint match {
          case Some(hint) =>
            middleware(serviceName, hint.allow.toSet, local)
          case None => identity
        }
      }
    }
}

object ClientApiKeyMiddleware {

  private def middleware(key: String): Client[IO] => Client[IO] = {
    inputClient =>
      Client[IO] { request =>
        val newRequest =
          request.putHeaders(Header.Raw(CIString("X-myapiz-key"), key))
        inputClient.run(newRequest)
      }
  }

  def apply(key: String): ClientEndpointMiddleware[IO] = // 3
    new ClientEndpointMiddleware.Simple[IO] {
      private val mid = middleware(key)
      def prepareWithHints(
          serviceHints: Hints,
          endpointHints: Hints
      ): Client[IO] => Client[IO] = {
        val hint = endpointHints
          .get[smithy.api.HttpApiKeyAuth]
          .orElse(serviceHints.get[smithy.api.HttpApiKeyAuth])
        hint match {
          case Some(authHint) => mid
          case None           => identity
        }
      }
    }

}

object Http4sMiddleware {

  def apply(
      middleware: HttpApp[IO] => HttpApp[IO]
  ): ServerEndpointMiddleware[IO] = // 3
    new ServerEndpointMiddleware.Simple[IO] {
      def prepareWithHints(
          serviceHints: Hints,
          endpointHints: Hints
      ): HttpApp[IO] => HttpApp[IO] = { app =>
        middleware(app)
      }
    }
}
