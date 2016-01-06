package com.localytics.sbt.dynamodb

import java.io.File
import java.net.URL

import sbt.Keys._
import sbt._

import scala.concurrent.duration._

object DynamoDBLocal extends AutoPlugin {

  //http://dynamodb-local.s3-website-us-west-2.amazonaws.com/dynamodb_local_latest.tar.gz
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

  object Keys {
    val dynamoDBLocalVersion = settingKey[String]("DynamoDB Local version to download. Defaults to latest.")
    val dynamoDBLocalDownloadUrl = settingKey[Option[String]]("DynamoDB Local URL to download jar from (optional).")
    val dynamoDBLocalDownloadDir = settingKey[File]("The directory the DynamoDB Local jar will be downloaded to. Defaults to dynamodb-local.")
    val dynamoDBLocalDownloadIfOlderThan = settingKey[Duration]("Re-download the jar if the existing one is older than this. Defaults to 2 days.")
    val dynamoDBLocalPort = settingKey[Int]("The port number that DynamoDB Local will use to communicate with your application. Defaults to 8000.")
    val dynamoDBLocalDBPath = settingKey[Option[String]]("The directory where DynamoDB Local will write its database file. Defaults to the current directory.")
    val dynamoDBLocalInMemory = settingKey[Boolean]("Instead of using a database file, DynamoDB Local will run in memory. When you stop DynamoDB Local, none of the data will be saved.")
    val dynamoDBLocalSharedDB = settingKey[Boolean]("DynamoDB Local will use a single, shared database file. All clients will interact with the same set of tables regardless of their region and credential configuration.")
    val stopDynamoDBLocalAfterTests = SettingKey[Boolean]("stop-dynamodb-local-after-tests")
    val cleanDynamoDBLocalAfterStop = SettingKey[Boolean]("clean-dynamodb-local-after-tests")

    val dynamoDBLocalPid = TaskKey[String]("dynamodb-local-pid")
    val deployDynamoDBLocal = TaskKey[File]("deploy-dynamodb-local")
    val startDynamoDBLocal = TaskKey[String]("start-dynamodb-local")
    val stopDynamoDBLocal = TaskKey[Unit]("stop-dynamodb-local")
  }

  import Keys._

  def settings: Seq[Setting[_]] = Seq(
    dynamoDBLocalVersion := DefaultDynamoDBLocalVersion,
    dynamoDBLocalDownloadUrl := None,
    dynamoDBLocalDownloadDir := DefaultDynamoDBLocalDownloadDir,
    dynamoDBLocalDownloadIfOlderThan := DefaultDynamoDBLocalDownloadIfOlderThan,
    dynamoDBLocalPort := DefaultPort,
    dynamoDBLocalDBPath := None,
    dynamoDBLocalInMemory := true,
    dynamoDBLocalSharedDB := false,
    stopDynamoDBLocalAfterTests := true,
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
          streamz.log.error(s"Unable to find DynamoDB Local jar at [${outputFile.getAbsolutePath}]")
          sys.exit(1)
        }
    },
    startDynamoDBLocal <<= (deployDynamoDBLocal, dynamoDBLocalDownloadDir, dynamoDBLocalPort, dynamoDBLocalDBPath, dynamoDBLocalInMemory, dynamoDBLocalSharedDB, streams) map {
      case (dyanmoHome, baseDir, port, dbPath, inMem, shared, streamz) =>
        val args = Seq("java", s"-Djava.library.path=${new File(baseDir, DynamoDBLocalLibDir).getAbsolutePath}", "-jar", new File(baseDir, DynamoDBLocalJar).getAbsolutePath) ++
          Seq("-port", port.toString) ++
          dbPath.map(db => Seq("-dbPath", db)).getOrElse(Nil) ++
          (if (inMem) Seq("-inMemory") else Nil) ++
          (if (shared) Seq("-sharedDb") else Nil)

        if (!Utils.isDynamoDBLocalRunning(port)) {
          streamz.log.info("Starting dynamodb local:")
          Process(args).run()
          streamz.log.info("Waiting for dynamodb local:")
          Utils.waitForDynamoDBLocal(port, (s: String) => streamz.log.info(s))
        } else {
          streamz.log.warn(s"dynamodb local is already running on port $port")
        }
        getDynamoDBLocalPid.map { pid =>
          dynamoDBLocalPid := pid
          pid
        }.getOrElse {
          streamz.log.error(s"Cannot find dynamodb local PID")
          sys.exit(1)
        }
    },
    //if compilation of test classes fails, dynamodb should not be invoked. (moreover, Test.Cleanup won't execute to stop it...)
    startDynamoDBLocal <<= startDynamoDBLocal.dependsOn(compile in Test),
    dynamoDBLocalPid <<= streams map {
      case (streamz) =>
        getDynamoDBLocalPid.map { pid =>
          dynamoDBLocalPid := pid
          pid
        }.getOrElse {
          // This is ok - it just means that DynamoDB isn't running, most likely because it was never started. :-)
          streamz.log.info(s"Cannot find dynamodb local PID")
          "0"
        }
    },
    stopDynamoDBLocal <<= (dynamoDBLocalPid, dynamoDBLocalDBPath, cleanDynamoDBLocalAfterStop) map {
      case (pid, dbPath, cln) =>
        killDynamoDBLocal(cln, dbPath, pid)
    },
    //make sure to Stop DynamoDB Local when tests are done.
    testOptions in Test <+= (dynamoDBLocalPid, stopDynamoDBLocalAfterTests, cleanDynamoDBLocalAfterStop, dynamoDBLocalDBPath) map {
      case (pid, stop, cln, dbPath) => Tests.Cleanup(() => {
        if (stop && pid != "0") killDynamoDBLocal(cln, dbPath, pid)
      })
    }
  )

  private[this] def killDynamoDBLocal(clean: Boolean, dataDir: Option[String], pid: String) = {
    val osName = System.getProperty("os.name") match {
      case n: String if !n.isEmpty => n
      case _ => System.getProperty("os")
    }
    if (osName.toLowerCase.contains("windows")) {
      s"Taskkill /PID $pid /F".!
    } else {
      s"kill $pid".!
    }
    Utils.cleanDynamoDBLocal(clean, dataDir, pid)
  }

  private[this] def getDynamoDBLocalPid = Utils.extractDynamoDBPid("jps".!!)

}
