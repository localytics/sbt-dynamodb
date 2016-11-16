package com.localytics.sbt.dynamodb

import java.io.File

import org.scalatest.FunSpec
import org.scalatest.Matchers

class DeployDynamoDBLocalTest extends FunSpec with Matchers {

  describe("DeployDynamoDBLocal") {

    it("should identify a valid jar") {
      DeployDynamoDBLocal.validJar(new File(getClass.getResource("/valid.jar").getFile)) should be(true)
    }

    it("should identify an invalid jar") {
      DeployDynamoDBLocal.validJar(new File(getClass.getResource("/invalid.jar").getFile)) should be(false)
    }

    it("should identify a valid gz") {
      DeployDynamoDBLocal.validGzip(new File(getClass.getResource("/valid.tar.gz").getFile)) should be(true)
    }

    it("should identify an invalid gz") {
      DeployDynamoDBLocal.validGzip(new File(getClass.getResource("/invalid.tar.gz").getFile)) should be(false)
    }
  }

}
