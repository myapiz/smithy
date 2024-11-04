# Smithy Project

myAPIz relevant Smithy files and language dependent libraries
that give you the recommended way to build your APIs.

Currently supported languages:

- Scala

## Getting Started

### Prerequisites

- Install SBT (Scala Build Tool)
- Install [Smithy CLI](https://smithy.io/2.0/guides/smithy-cli/cli_installation.html)

### Validating the myAPIz Model

```bash
smithy validate
```

### Using the myAPIz Model

```bash
sbt package
```

This will generate a model.jar target folder which contains
the smithy model and then can be included and referenced
in your API implementations.

In your `api.smithy` file, you can reference the model like this:

```smithy
use smithy.api#required
use alloy#simpleRestJson

use com.myapiz.smithy.error#NotFoundError
use com.myapiz.smithy.error#NotAuthenticatedError
use com.myapiz.smithy.error#NotAuthorizedError
use com.myapiz.smithy.auth#authorization
```

### Scala implementations

The Smithy model can be used to generate Scala implementations for your APIs.

Directly use the Scala implementations in your code:

```scala
  // add the smithy plugin to your build.sbt
  .enablePlugins(Smithy4sCodegenPlugin)

  // add the dependencies to your build.sbt
  "com.myapiz" % "smithy4s_3" % versions.myapiz,
```

For a quick start, see the [smithy4s-project](https://github.com/myapiz/smithy4s-project) project template.

## Other Resources

- [Smithy](https://smithy.io/)

## License

This project is licensed under the Apache-2.0 License.
