sbt-dynamodb
===============

Support for running [DynamoDB Local](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html) in tests.

[![MIT license](https://img.shields.io/badge/license-MIT%20License-blue.svg)](LICENSE) 

Installation
------------
Add the following to your `project/plugins.sbt` file:

```
addSbtPlugin("com.localytics" % "sbt-dynamodb" % "1.3.0")
```

sbt 0.13.6+ is supported, 0.13.5 should work with the right bintray resolvers

Usage
-----

To use DynamoDB Local in your project you can call `start-dynamodb-local` and `stop-dynamodb-local` directly in `sbt`.

Configuration
-------------

To have DynamoDB Local automatically start and stop around your tests

```
startDynamoDBLocal <<= startDynamoDBLocal.dependsOn(compile in Test)
test in Test <<= (test in Test).dependsOn(startDynamoDBLocal)
test in Test <<= (test in Test, stopDynamoDBLocal) { (test, stop) => test doFinally stop }
```

To download the DynamoDB Local jar to a specific location ("dynamodb-local" is the default)

```
dynamoDBLocalDownloadDir := file("my-dir")
```

To use a specific version ("latest" is the default DynamoDB version to download and run)

```
dynamoDBLocalVersion := "2014-10-07"
```

If the "latest" version is being used, specify how old the downloaded copy should be before attempting a new download (default is 2 days)

```
import scala.concurrent.duration._
dynamoDBLocalDownloadIfOlderThan := 2.days
```

To specify a port other than the default `8000`

```
dynamoDBLocalPort := 8080
```

To override the default JVM heap size (specified in MB)

```
dynamoDBLocalHeapSize := Some(1024)
```

The default for the DynamoDB Local instance is to run in "in-memory" mode. To use a persistent file based mode you need to set both the data path & turn off in-memory.

```
dynamoDBLocalInMemory := false
dynamoDBLocalDBPath := Some("some/directory/here")
```

The default for DynamoDB Local instance is to use a separate file for each credential and region. To allow all all DynamoDB clients to interact with the same set of tables regardless of their region and credentials enable "shared db" mode.

```
dynamoDBLocalSharedDB := true
```

The default on stop is to cleanup any data directory if specified. This can be changed using

```
dynamoDBLocalCleanAfterStop := false
```

Thanks
-----

This work is based on the [Maven Plugin for DynamoDB](https://github.com/jcabi/jcabi-dynamodb-maven-plugin).

The [initial implementation](https://github.com/grahamar/sbt-dynamodb) was developed by [Graham Rhodes](https://github.com/grahamar). 
