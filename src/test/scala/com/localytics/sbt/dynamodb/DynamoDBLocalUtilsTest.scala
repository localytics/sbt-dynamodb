package com.localytics.sbt.dynamodb

import java.io.File

import org.scalatest.{FunSpec, Matchers}

import scala.io.Source

class DynamoDBLocalUtilsTest extends FunSpec with Matchers {

  describe("DynamoDBLocalUtils") {

    it("should extract PID correctly") {
      val jpsOutput =
        """
          |92433 sun.tools.jps.Jps -ml
          |88818 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
          |88421 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
          |5895 com.intellij.database.remote.RemoteJdbcServer com.mysql.jdbc.Driver
          |92431 /Users/person/code/repository/aws-mocks/dynamo-db/DynamoDBLocal.jar -port 8000 -inMemory -sharedDb
          |74414 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
        """.stripMargin
      val baseDir = new File("/Users/person/code/repository/aws-mocks/dynamo-db/")
      DynamoDBLocalUtils.extractDynamoDBPid(jpsOutput, 8000, baseDir) should equal(Some("92431"))
    }

    it("should resolve multiple local dynamodb instances") {
      val jpsOutput =
        """
          |92433 sun.tools.jps.Jps -ml
          |92430 /Users/person/code/macro/repository/aws-mocks/dynamo-db/DynamoDBLocal.jar -port 8001 -inMemory -sharedDb
          |92433 /Users/person/code/mono/repository/aws-mocks/dynamo-db/DynamoDBLocal.jar -port 8002 -inMemory -sharedDb
          |5895 com.intellij.database.remote.RemoteJdbcServer com.mysql.jdbc.Driver
          |92431 /Users/person/code/repository/aws-mocks/dynamo-db/DynamoDBLocal.jar -port 8000 -inMemory -sharedDb
          |74414 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
        """.stripMargin
      val baseDir = new File("/Users/person/code/repository/aws-mocks/dynamo-db/")
      DynamoDBLocalUtils.extractDynamoDBPid(jpsOutput, 8000, baseDir) should equal(Some("92431"))

    }

    it("should identify a valid jar") {
      DynamoDBLocalUtils.validJar(new File(getClass.getResource("/valid.jar").getFile)) should be(true)
    }

    it("should identify an invalid jar") {
      DynamoDBLocalUtils.validJar(new File(getClass.getResource("/invalid.jar").getFile)) should be(false)
    }

    it("should identify a valid gz") {
      DynamoDBLocalUtils.validGzip(new File(getClass.getResource("/valid.tar.gz").getFile)) should be(true)
    }

    it("should identify an invalid gz") {
      DynamoDBLocalUtils.validGzip(new File(getClass.getResource("/invalid.tar.gz").getFile)) should be(false)
    }
  }

}
