package com.teambytes.sbt.dynamodb

import sbt._
import sbt.Keys._

import java.net.URL
import java.io.File

object DynamoDBLocal extends AutoPlugin {

  //http://dynamodb-local.s3-website-us-west-2.amazonaws.com/dynamodb_local_latest.tar.gz
  private val DefaultDynamoDBLocalUrlTemplate = { version: String =>
    s"http://dynamodb-local.s3-website-us-west-2.amazonaws.com/dynamodb_local_$version.tar.gz"
  }
  private val DefaultDynamoDBLocalVersion = "latest"
  private val DynamoDBLocalLibDir = "DynamoDBLocal_lib"
  private val DynamoDBLocalJar = "DynamoDBLocal.jar"
  private val DefaultPort = 8000

  object Keys {
    val dynamoDBLocalVersion = settingKey[String]("DynamoDB Local version to download.")
    val dynamoDBLocalDownloadUrl = settingKey[Option[String]]("DynamoDB Local URL to download jar from (optional).")
    val dynamoDBLocalDownloadDirectory = settingKey[File]("The directory DynamoDB Local jar will be downloaded to.")
    val dynamoDBLocalPort = settingKey[Option[Int]]("The port number that DynamoDB Local will use to communicate with your application. If you do not specify this option, the default port is 8000.")
    val dynamoDBLocalDBPath = settingKey[Option[String]]("The directory where DynamoDB Local will write its database file. If you do not specify this option, the file will be written to the current directory.")
    val dynamoDBLocalInMemory = settingKey[Boolean]("Instead of using a database file, DynamoDB Local will run in memory. When you stop DynamoDB Local, none of the data will be saved.")
    val deployDynamoDBLocal = TaskKey[File]("deploy-dynamodb-local")
    val startDynamoDBLocal = TaskKey[String]("start-dynamodb-local")
    val dynamoDBLocalPid = TaskKey[String]("dynamodb-local-pid")
    val stopDynamoDBLocalAfterTests = SettingKey[Boolean]("stop-dynamodb-local-after-tests")
    val cleanDynamoDBLocalAfterStop = SettingKey[Boolean]("clean-dynamodb-local-after-tests")
    val stopDynamoDBLocal = TaskKey[Unit]("stop-dynamodb-local")
  }

  import Keys._

  def settings: Seq[Setting[_]] = Seq(
    dynamoDBLocalVersion := DefaultDynamoDBLocalVersion,
    dynamoDBLocalDownloadUrl := None,
    dynamoDBLocalPort := Some(DefaultPort),
    dynamoDBLocalDBPath := None,
    dynamoDBLocalInMemory := true,
    stopDynamoDBLocalAfterTests := true,
    cleanDynamoDBLocalAfterStop := true,
    deployDynamoDBLocal <<= (dynamoDBLocalVersion, dynamoDBLocalDownloadUrl, dynamoDBLocalDownloadDirectory, streams) map {
      case (ver, url, targetDir, streamz) =>
        import sys.process._
        val outputFile = new File(targetDir, s"dynamodb_local_$ver.tar.gz")
        if(!targetDir.exists()){
          streamz.log.info(s"Creating DynamoDB Local directory $targetDir:")
          targetDir.mkdirs()
        }
        if(!outputFile.exists() || ver == "latest") {
          val remoteFile= url.getOrElse(DefaultDynamoDBLocalUrlTemplate(ver))
          streamz.log.info(s"Downloading DynamoDB Local from [$remoteFile] to [${outputFile.getAbsolutePath}]")
          (new URL(remoteFile) #> outputFile).!!
        }
        if(outputFile.exists()) {
          streamz.log.info(s"Extracting file: [${outputFile.getAbsolutePath}]")
          Process(Seq("tar", "xzf", outputFile.getAbsolutePath), targetDir).!
          outputFile
        } else {
          streamz.log.error(s"Unable to find DyanmoDB Local jar at [${outputFile.getAbsolutePath}]")
          sys.exit(1)
        }
    },
    startDynamoDBLocal <<= (deployDynamoDBLocal, dynamoDBLocalDownloadDirectory, dynamoDBLocalPort, dynamoDBLocalDBPath, dynamoDBLocalInMemory, streams) map {
      case (dyanmoHome, baseDir, port, dbPath, inMem, streamz) =>
        val args = Seq("java", s"-Djava.library.path=${new File(baseDir, DynamoDBLocalLibDir).getAbsolutePath}", "-jar", new File(baseDir, DynamoDBLocalJar).getAbsolutePath) ++
          port.map(p => Seq("-port", p.toString)).getOrElse(Nil) ++
          dbPath.map(db => Seq("-dbPath", db)).getOrElse(Nil) ++
          (if(inMem) Seq("-inMemory") else Nil)

        if(!Utils.isDynamoDBLocalRunning(port.getOrElse(DefaultPort))) {
          streamz.log.info("Starting dyanmodb local:")
          Process(args).run()
          streamz.log.info("Waiting for dyanmodb local:")
          Utils.waitForDynamoDBLocal(port.getOrElse(DefaultPort), (s: String) => streamz.log.info(s))
        } else {
          streamz.log.warn(s"dynamodb local is already running on port ${port.getOrElse(DefaultPort)}")
        }
        getDynamoDBLocalPid.map { pid =>
          dynamoDBLocalPid := pid
          pid
        }.getOrElse {
          streamz.log.error(s"Cannot find dynamodb local PID")
          sys.exit(1)
        }
    },
    //if compilation of test classes fails, dyanmodb should not be invoked. (moreover, Test.Cleanup won't execute to stop it...)
    startDynamoDBLocal <<= startDynamoDBLocal.dependsOn(compile in Test),
    dynamoDBLocalPid <<= streams map {
      case (streamz) =>
        getDynamoDBLocalPid.map { pid =>
          dynamoDBLocalPid := pid
          pid
        }.getOrElse {
          streamz.log.error(s"Cannot find dynamodb local PID")
          sys.exit(1)
        }
    },
    stopDynamoDBLocal <<= (dynamoDBLocalPid, dynamoDBLocalDBPath, cleanDynamoDBLocalAfterStop) map {
      case (pid, dbPath, cln) =>
        killDynamoDBLocal(cln, dbPath, pid)
    },
    //make sure to Stop DynamoDB Local when tests are done.
    testOptions in Test <+= (dynamoDBLocalPid, stopDynamoDBLocalAfterTests, cleanDynamoDBLocalAfterStop, dynamoDBLocalDBPath) map {
      case (pid, stop, cln, dbPath) => Tests.Cleanup(() => {
        if(stop) killDynamoDBLocal(cln, dbPath, pid)
      })
    }
  )

  private[this] def killDynamoDBLocal(clean: Boolean, dataDir: Option[String], pid: String) = {
    val osName= System.getProperty("os.name") match {
      case n: String if !n.isEmpty => n
      case _ => System.getProperty("os")
    }
    if(osName.toLowerCase.contains("windows")) {
      s"Taskkill /PID $pid /F".!
    } else {
      s"kill $pid".!
    }
    Utils.cleanDynamoDBLocal(clean, dataDir, pid)
  }

  private[this] def getDynamoDBLocalPid = Utils.extractDyanmoDBPid("jps".!!)

}
