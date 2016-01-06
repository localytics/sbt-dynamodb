package com.localytics.sbt.dynamodb

import java.io.File
import java.net.URL

import sbt.Keys._
import sbt._

import scala.concurrent.duration._

object DynamoDBLocal extends AutoPlugin {

  private val DefaultDynamoDBLocalUrlTemplate = { version: String =>
    s"http://dynamodb-local.s3-website-us-west-2.amazonaws.com/dynamodb_local_$version.tar.gz"
  }
  private val DefaultDynamoDBLocalDownloadFileNameTemplate = { version: String =>
    s"dynamodb_local_$version.tar.gz"
  }
  private val DefaultDynamoDBLocalVersion = "latest"
  private val DefaultDynamoDBLocalDownloadDir = file("dynamodb-local")
  private val DynamoDBLocalLibDir = "DynamoDBLocal_lib"
  private val DynamoDBLocalJar = "DynamoDBLocal.jar"
  private val DefaultPort = 8000
  private val DefaultDynamoDBLocalDownloadIfOlderThan = 2.days

  // auto enable plugin http://www.scala-sbt.org/0.13/docs/Plugins.html#Root+plugins+and+triggered+plugins
  override def trigger = allRequirements

  // inject project keys http://www.scala-sbt.org/0.13/docs/Plugins.html#Controlling+the+import+with+autoImport
  object autoImport {
    val dynamoDBLocalVersion = settingKey[String]("DynamoDB Local version to download. Defaults to latest.")
    val dynamoDBLocalDownloadUrl = settingKey[Option[String]]("DynamoDB Local URL to download jar from (optional).")
    val dynamoDBLocalDownloadDir = settingKey[File]("The directory the DynamoDB Local jar will be downloaded to. Defaults to dynamodb-local.")
    val dynamoDBLocalDownloadIfOlderThan = settingKey[Duration]("Re-download the jar if the existing one is older than this. Defaults to 2 days.")
    val dynamoDBLocalPort = settingKey[Int]("The port number that DynamoDB Local will use to communicate with your application. Defaults to 8000.")
    val dynamoDBLocalDBPath = settingKey[Option[String]]("The directory where DynamoDB Local will write its database file. Defaults to the current directory.")
    val dynamoDBLocalInMemory = settingKey[Boolean]("Instead of using a database file, DynamoDB Local will run in memory. When you stop DynamoDB Local, none of the data will be saved.")
    val dynamoDBLocalSharedDB = settingKey[Boolean]("DynamoDB Local will use a single, shared database file. All clients will interact with the same set of tables regardless of their region and credential configuration.")
    val cleanDynamoDBLocalAfterStop = SettingKey[Boolean]("clean-dynamodb-local-after-stop")

    val deployDynamoDBLocal = TaskKey[File]("deploy-dynamodb-local")
    val startDynamoDBLocal = TaskKey[String]("start-dynamodb-local")
    val stopDynamoDBLocal = TaskKey[Unit]("stop-dynamodb-local")
  }

  import autoImport._

  // inject project settings http://www.scala-sbt.org/0.13/docs/Plugins.html#projectSettings+and+buildSettings
  override lazy val projectSettings = Seq(
    dynamoDBLocalVersion := DefaultDynamoDBLocalVersion,
    dynamoDBLocalDownloadUrl := None,
    dynamoDBLocalDownloadDir := DefaultDynamoDBLocalDownloadDir,
    dynamoDBLocalDownloadIfOlderThan := DefaultDynamoDBLocalDownloadIfOlderThan,
    dynamoDBLocalPort := DefaultPort,
    dynamoDBLocalDBPath := None,
    dynamoDBLocalInMemory := true,
    dynamoDBLocalSharedDB := false,
    cleanDynamoDBLocalAfterStop := true,
    deployDynamoDBLocal <<= (dynamoDBLocalVersion, dynamoDBLocalDownloadUrl, dynamoDBLocalDownloadDir, dynamoDBLocalDownloadIfOlderThan, streams) map {
      case (ver, url, targetDir, downloadIfOlderThan, streamz) =>
        import sys.process._
        val outputFile = new File(targetDir, DefaultDynamoDBLocalDownloadFileNameTemplate(ver))
        if (!targetDir.exists()) {
          streamz.log.info(s"Creating DynamoDB Local directory $targetDir:")
          targetDir.mkdirs()
        }
        if (!outputFile.exists() || ((ver == "latest") && (System.currentTimeMillis - outputFile.lastModified() > downloadIfOlderThan.toMillis))) {
          val remoteFile = url.getOrElse(DefaultDynamoDBLocalUrlTemplate(ver))
          streamz.log.info(s"Downloading DynamoDB Local from [$remoteFile] to [${outputFile.getAbsolutePath}]")
          (new URL(remoteFile) #> outputFile).!!
        }
        if (outputFile.exists()) {
          streamz.log.info(s"Extracting file: [${outputFile.getAbsolutePath}]")
          Process(Seq("tar", "xzf", outputFile.getAbsolutePath), targetDir).!
          outputFile
        } else {
          sys.error(s"Cannot to find DynamoDB Local jar at [${outputFile.getAbsolutePath}].")
        }
    },
    startDynamoDBLocal <<= (deployDynamoDBLocal, dynamoDBLocalDownloadDir, dynamoDBLocalPort, dynamoDBLocalDBPath, dynamoDBLocalInMemory, dynamoDBLocalSharedDB, streams) map {
      case (dynamoDBHome, baseDir, port, dbPath, inMem, shared, streamz) =>
        val args = Seq("java", s"-Djava.library.path=${new File(baseDir, DynamoDBLocalLibDir).getAbsolutePath}") ++
          Seq("-jar", new File(baseDir, DynamoDBLocalJar).getAbsolutePath) ++
          Seq("-port", port.toString) ++
          dbPath.map(db => Seq("-dbPath", db)).getOrElse(Nil) ++
          (if (inMem) Seq("-inMemory") else Nil) ++
          (if (shared) Seq("-sharedDb") else Nil)

        if (Utils.isDynamoDBLocalRunning(port)) {
          streamz.log.warn(s"dynamodb local is already running on port $port")
        } else {
          streamz.log.info("Starting dynamodb local:")
          Process(args).run()
          streamz.log.info("Waiting for dynamodb local:")
          Utils.waitForDynamoDBLocal(port, (s: String) => streamz.log.info(s))
        }
        Utils.extractDynamoDBPid("jps".!!).getOrElse {
          sys.error(s"Cannot find dynamodb local PID.")
        }
    },
    stopDynamoDBLocal <<= (streams, dynamoDBLocalDBPath, cleanDynamoDBLocalAfterStop) map {
      case (streamz, dbPathOpt, clean) =>
        Utils.extractDynamoDBPid("jps".!!) match {
          case Some(pid) =>
            streamz.log.info("Stopping dynamodb local:")
            Utils.killPidCommand(pid).!
          case None =>
            streamz.log.warn("Cannot find dynamodb local PID.")
        }
        if (clean) dbPathOpt.foreach { dbPath =>
          streamz.log.info("Cleaning dynamodb local:")
          val dir = new File(dbPath)
          if (dir.exists()) sbt.IO.delete(dir)
        }
    }
  )
}
