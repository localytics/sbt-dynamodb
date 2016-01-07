package com.localytics.sbt.dynamodb

import org.scalatest.{FunSpec, Matchers}

class DynamoDBLocalUtils extends FunSpec with Matchers {

  describe("DynamoDBLocalUtils") {

    it("should extract PID correctly") {
      val jpsOutput =
        """
          |16706 QuorumPeerMain
          |60405 Boot
          |59022 DynamoDBLocal.jar
          |60479 Jps
          |51449
        """.stripMargin

      DynamoDBLocalUtils.extractDynamoDBPid(jpsOutput) should equal(Some("59022"))
    }

  }

}
