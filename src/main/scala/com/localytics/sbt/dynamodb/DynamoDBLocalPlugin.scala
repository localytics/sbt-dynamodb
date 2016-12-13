package com.localytics.sbt.dynamodb

import com.localytics.sbt.dynamodb.DynamoDBLocalKeys._
import sbt.Keys._
import sbt._

import scala.concurrent.duration._

object DynamoDBLocalPlugin extends AutoPlugin {

  // auto enable plugin http://www.scala-sbt.org/0.13/docs/Plugins.html#Root+plugins+and+triggered+plugins
  override val trigger = allRequirements

  // inject project keys http://www.scala-sbt.org/0.13/docs/Plugins.html#Controlling+the+import+with+autoImport
  val autoImport = DynamoDBLocalKeys

  // inject project settings http://www.scala-sbt.org/0.13/docs/Plugins.html#projectSettings+and+buildSettings
  override lazy val projectSettings = Seq(
    dynamoDBLocalVersion := "latest",
    dynamoDBLocalDownloadUrl := None,
    dynamoDBLocalDownloadDir := file("dynamodb-local"),
    dynamoDBLocalDownloadIfOlderThan := 2.days,
    dynamoDBLocalPort := 8000,
    dynamoDBLocalDBPath := None,
    dynamoDBLocalHeapSize := None,
    dynamoDBLocalInMemory := true,
    dynamoDBLocalSharedDB := false,
    dynamoDBLocalCleanAfterStop := true,
    deployDynamoDBLocal := DeployDynamoDBLocal(
      dynamoDBLocalVersion.value,
      dynamoDBLocalDownloadUrl.value,
      dynamoDBLocalDownloadDir.value,
      dynamoDBLocalDownloadIfOlderThan.value,
      streams.value),
    startDynamoDBLocal := StartDynamoDBLocal(
      dynamoDBLocalDownloadDir.value,
      dynamoDBLocalPort.value,
      dynamoDBLocalHeapSize.value,
      dynamoDBLocalDBPath.value,
      dynamoDBLocalInMemory.value,
      dynamoDBLocalSharedDB.value,
      streams.value),
    stopDynamoDBLocal := StopDynamoDBLocal(
      dynamoDBLocalDBPath.value,
      dynamoDBLocalCleanAfterStop.value,
      dynamoDBLocalPort.value,
      dynamoDBLocalDownloadDir.value,
      streams.value),
    dynamoDBLocalTestCleanup := Tests.Cleanup(() =>
      StopDynamoDBLocal(dynamoDBLocalDBPath.value,
        dynamoDBLocalCleanAfterStop.value,
        dynamoDBLocalPort.value,
        dynamoDBLocalDownloadDir.value,
        streams.value)),
    startDynamoDBLocal := startDynamoDBLocal
      .dependsOn(deployDynamoDBLocal)
      .value
  )
}
