package com.localytics.sbt.dynamodb

import java.io.File
import java.net.URL

import com.localytics.sbt.dynamodb.DynamoDBLocalKeys._
import com.localytics.sbt.dynamodb.DynamoDBLocalUtils._
import sbt.Keys._
import sbt._

object DynamoDBLocalTasks {

  def deployDynamoDBLocalTask = (dynamoDBLocalVersion, dynamoDBLocalDownloadUrl, dynamoDBLocalDownloadDir, dynamoDBLocalDownloadIfOlderThan, streams) map {
    case (ver, url, targetDir, downloadIfOlderThan, streamz) =>
      import sys.process._
      val outputFile = new File(targetDir, s"dynamodb_local_$ver.tar.gz")
      if (!targetDir.exists()) {
        streamz.log.info(s"Creating DynamoDB Local directory $targetDir:")
        targetDir.mkdirs()
      }
      if (!outputFile.exists() || ((ver == "latest") && (System.currentTimeMillis - outputFile.lastModified() > downloadIfOlderThan.toMillis))) {
        val remoteFile = url.getOrElse(s"http://dynamodb-local.s3-website-us-west-2.amazonaws.com/dynamodb_local_$ver.tar.gz")
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
  }

  def startDynamoDBLocalTask = (deployDynamoDBLocal, dynamoDBLocalDownloadDir, dynamoDBLocalPort, dynamoDBLocalDBPath, dynamoDBLocalInMemory, dynamoDBLocalSharedDB, streams) map {
    case (dynamoDBHome, baseDir, port, dbPath, inMem, shared, streamz) =>
      val args = Seq("java", s"-Djava.library.path=${new File(baseDir, "DynamoDBLocal_lib").getAbsolutePath}") ++
        Seq("-jar", new File(baseDir, "DynamoDBLocal.jar").getAbsolutePath) ++
        Seq("-port", port.toString) ++
        dbPath.map(db => Seq("-dbPath", db)).getOrElse(Nil) ++
        (if (inMem) Seq("-inMemory") else Nil) ++
        (if (shared) Seq("-sharedDb") else Nil)

      if (isDynamoDBLocalRunning(port)) {
        streamz.log.warn(s"dynamodb local is already running on port $port")
      } else {
        streamz.log.info("Starting dynamodb local:")
        Process(args).run()
        streamz.log.info("Waiting for dynamodb local:")
        waitForDynamoDBLocal(port, (s: String) => streamz.log.info(s))
      }
      extractDynamoDBPid("jps".!!).getOrElse {
        sys.error(s"Cannot find dynamodb local PID.")
      }
  }

  def stopDynamoDBLocalTask = (streams, dynamoDBLocalDBPath, dynamoDBLocalCleanAfterStop) map {
    case (streamz, dbPathOpt, clean) =>
      extractDynamoDBPid("jps".!!) match {
        case Some(pid) =>
          streamz.log.info("Stopping dynamodb local:")
          killPidCommand(pid).!
        case None =>
          streamz.log.warn("Cannot find dynamodb local PID.")
      }
      if (clean) dbPathOpt.foreach { dbPath =>
        streamz.log.info("Cleaning dynamodb local:")
        val dir = new File(dbPath)
        if (dir.exists()) sbt.IO.delete(dir)
      }
  }

}
