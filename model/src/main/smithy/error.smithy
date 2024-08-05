$version: "2"

namespace com.myapiz.smithy.error

use alloy#simpleRestJson

string URI

string URIReference

// error type mixing is defined based on Problem Details for HTTP APIs https://datatracker.ietf.org/doc/rfc7807/
@mixin
structure Error7807 {
    @documentation("A URI reference [RFC3986] that identifies the
                                  problem type.  This specification encourages that, when
                                  dereferenced, it provide human-readable documentation for the
                                  problem type (e.g., using HTML [W3C.REC-html5-20141028]).  When
                                  this member is not present, its value is assumed to be
                                  about:blank")
    type: URI
    @required
    @documentation("A short, human-readable summary of the problem
                                      type.  It SHOULD NOT change from occurrence to occurrence of the
                                      problem, except for purposes of localization (e.g., using
                                      proactive content negotiation; see [RFC7231], Section 3.4).")
    title: String
    @required
    @documentation("The HTTP status code ([RFC7231], Section 6) generated by the origin server for this occurrence of the problem.")
    status: Integer
    @documentation("A human-readable explanation specific to this occurrence of the problem.")
    detail: String
    @documentation("A URI reference that identifies the specific occurrence of the problem.  It may or may not yield further information if dereferenced.")
    instance: URIReference
}

@error("client")
@httpError(401)
structure NotAuthorizedError with [Error7807] {
    status: Integer = 401
}

@error("client")
@httpError(403)
structure NotAuthenticatedError with [Error7807] {
    status: Integer = 403
}

@error("client")
@httpError(404)
structure NotFoundError with [Error7807] {
    status: Integer = 404
}

@error("server")
@httpError(500)
structure UnexpectedServerError with [Error7807] {
    status: Integer = 500
}
