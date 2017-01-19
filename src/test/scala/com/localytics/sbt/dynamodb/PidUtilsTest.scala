package com.localytics.sbt.dynamodb

import java.io.File

import org.scalatest.FunSpec
import org.scalatest.Matchers

class PidUtilsTest extends FunSpec with Matchers {

  describe("PidUtils") {

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
      val jar = new File("/Users/person/code/repository/aws-mocks/dynamo-db/DynamoDBLocal.jar")
      PidUtils.extractPid(jpsOutput, 8000, jar) should equal(Some("92431"))
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
      val jar = new File("/Users/person/code/repository/aws-mocks/dynamo-db/DynamoDBLocal.jar")
      PidUtils.extractPid(jpsOutput, 8000, jar) should equal(Some("92431"))

    }

  }

}
