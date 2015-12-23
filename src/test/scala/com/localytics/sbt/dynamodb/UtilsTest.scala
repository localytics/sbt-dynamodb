package com.localytics.sbt.dynamodb

import org.scalatest.mock.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class UtilsTest extends WordSpec with MustMatchers with MockitoSugar {

  "Utils" should {

    "extract PID correctly" in {
      val jpsOutput =
        """
          |16706 QuorumPeerMain
          |60405 Boot
          |59022 DynamoDBLocal.jar
          |60479 Jps
          |51449
        """.stripMargin

      Utils.extractDyanmoDBPid(jpsOutput) must equal(Some("59022"))
    }

  }

}
