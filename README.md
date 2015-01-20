sbt-dynamodb
===============

Support for running [DynamoDB Local](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html) for use in integration tests.

Based on the [Maven Plugin for DynamoDB](https://github.com/jcabi/jcabi-dynamodb-maven-plugin).

Installation
------------
Add the following to your `project/plugins.sbt` file:

```
addSbtPlugin("com.teambytes.sbt" % "sbt-dynamodb" % "1.0")
```

Configuration
-------------
The following represents the minimum amount of code required in a `build.sbt` to use [sbt-dynamodb](https://github.com/grahamar/sbt-dynamodb)

To use the dynamodb settings in your project, add `DynamoDBLocal.settings` to your build, set the directory to use for the DynamoDB Local jar and have your tests depend on starting the DynamoDB Local instance.

```
DynamoDBLocal.settings

DynamoDBLocal.Keys.dynamoDBLocalDownloadDirectory := file("dynamodb-local")

test in Test <<= (test in Test).dependsOn(DynamoDBLocal.Keys.startDynamoDBLocal)
```

To use a specific version ("latest" is the default DynamoDB version to download and run)

```
dynamoDBLocalVersion := "2014-10-07"
```

To specify a port other than the default `8000`

```
dynamoDBLocalPort := Some(8080)
```

The default for the DynamoDB Local instance is to run in "in-memory" mode. To use a persistent file based mode you need to set both the data path & turn off in-memory.

```
dynamoDBLocalInMemory := false

dynamoDBLocalDBPath := Some("some/directory/here")
```

The default regarding tests is to both stop & cleanup any data directory if specified. This can be changed using the below settings.

```
stopDynamoDBLocalAfterTests := false

cleanDynamoDBLocalAfterStop := false
```

##### Note: Regarding stopping the DynamoDB Local instance after the tests. To ensure the instance is stopped, please make sure not to override ```testOptions in Test``` and instead append (e.g. use `+=` instead of `:=`).
