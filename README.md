sbt-dynamodb-plugin
===============

Support for running [DynamoDB Local](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html) for use in integration tests.

Based on the [Maven Plugin for DynamoDB](https://github.com/jcabi/jcabi-dynamodb-maven-plugin).

Installation
------------
Add the following to your `project/plugins.sbt` file:

```scala
addSbtPlugin("com.teambytes.sbt" % "sbt-dynamodb" % "0.1")
```

Configuration
-------------
The following represents the minimum amount of code required in a `build.sbt` to use [sbt-dynamodb](https://github.com/grahamar/sbt-dynamodb)

To use the dynamodb settings in your project, add `DynamoDB.settings` to your build.

```
DynamoDB.settings
```
