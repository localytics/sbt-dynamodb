package com.localytics.sbt.dynamodb

import java.io.File

import org.scalatest.FunSpec
import org.scalatest.Matchers

class PidUtilsTest extends FunSpec with Matchers {

  describe("PidUtils") {
    val jarPath = if (PidUtils.osName.toLowerCase.contains("windows"))
      "C:\\Users\\person\\code\\repository\\aws-mocks\\.dynamo-db\\DynamoDBLocal.jar"
    else
      "/Users/person/code/repository/aws-mocks/.dynamo-db/DynamoDBLocal.jar"

    it("should extract PID correctly") {
      val jpsOutput =
        s"""
           |92433 sun.tools.jps.Jps -ml
           |88818 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
           |88421 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
           |5895 com.intellij.database.remote.RemoteJdbcServer com.mysql.jdbc.Driver
           |92431 $jarPath -port 8000 -inMemory -sharedDb
           |74414 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
        """.stripMargin
      val jar = new File(jarPath)
      PidUtils.extractPid(jpsOutput, 8000, jar) should equal(Some("92431"))
    }

    it("should resolve multiple local dynamodb instances") {
      val jpsOutput =
        s"""
           |92433 sun.tools.jps.Jps -ml
           |92430 $jarPath -port 8001 -inMemory -sharedDb
           |92433 $jarPath -port 8002 -inMemory -sharedDb
           |5895 com.intellij.database.remote.RemoteJdbcServer com.mysql.jdbc.Driver
           |92431 $jarPath -port 8000 -inMemory -sharedDb
           |74414 /usr/local/Cellar/sbt/0.13.7/libexec/sbt-launch.jar
        """.stripMargin
      val jar = new File(jarPath)
      PidUtils.extractPid(jpsOutput, 8000, jar) should equal(Some("92431"))

    }

  }

}
