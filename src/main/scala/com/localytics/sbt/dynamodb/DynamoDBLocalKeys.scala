package com.localytics.sbt.dynamodb

import java.io.File

import sbt._

import scala.concurrent.duration._

object DynamoDBLocalKeys {
  lazy val dynamoDBLocalVersion = settingKey[String]("DynamoDB Local version to download. Defaults to latest.")
  lazy val dynamoDBLocalDownloadUrl = settingKey[Option[String]]("DynamoDB Local URL to download jar from (optional).")
  lazy val dynamoDBLocalDownloadDir = settingKey[File]("The directory the DynamoDB Local jar will be downloaded to. Defaults to dynamodb-local.")
  lazy val dynamoDBLocalDownloadIfOlderThan = settingKey[Duration]("Re-download the jar if the existing one is older than this. Defaults to 2 days.")
  lazy val dynamoDBLocalHeapSize = settingKey[Option[Int]]("The size of the heap for DynamoDB Local. Defaults to the JVM default.")
  lazy val dynamoDBLocalPort = settingKey[Int]("The port number that DynamoDB Local will use to communicate with your application. Defaults to 8000.")
  lazy val dynamoDBLocalDBPath = settingKey[Option[String]]("The directory where DynamoDB Local will write its database file. Defaults to the current directory.")
  lazy val dynamoDBLocalInMemory = settingKey[Boolean]("Instead of using a database file, DynamoDB Local will run in memory. When you stop DynamoDB Local, none of the data will be saved.")
  lazy val dynamoDBLocalSharedDB = settingKey[Boolean]("DynamoDB Local will use a single, shared database file. All clients will interact with the same set of tables regardless of their region and credential configuration.")
  lazy val dynamoDBLocalCleanAfterStop = settingKey[Boolean]("Clean the local data directory after DynamoDB Local shutdown. Defaults to true.")

  lazy val deployDynamoDBLocal = TaskKey[File]("deploy-dynamodb-local")
  lazy val startDynamoDBLocal = TaskKey[String]("start-dynamodb-local")
  lazy val stopDynamoDBLocal = TaskKey[Unit]("stop-dynamodb-local")
}
