package com.localytics.sbt.dynamodb

import java.io.FileInputStream
import java.net.URL
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile

import sbt.File
import sbt.Keys._

import scala.concurrent.duration.Duration
import scala.sys.process._
import scala.util.Try

object DeployDynamoDBLocal {

  private[dynamodb] def validJar(file: File): Boolean = Try(new ZipFile(file)).isSuccess

  private[dynamodb] def validGzip(file: File): Boolean = Try(new GZIPInputStream(new FileInputStream(file)).read()).isSuccess

  def apply(ver: String, url: Option[String], targetDir: File, downloadIfOlderThan: Duration, streamz: TaskStreams): File = {
    val targz = new File(targetDir, s"dynamodb_local_$ver.tar.gz")
    val jar = new File(targetDir, "DynamoDBLocal.jar")

    def isStale(file: File) = ver == "latest" && System.currentTimeMillis - file.lastModified() > downloadIfOlderThan.toMillis

    if (!targetDir.exists()) {
      streamz.log.info(s"Creating DynamoDB Local directory $targetDir")
      targetDir.mkdirs()
    }
    if (!targz.exists() || isStale(targz) || !validGzip(targz)) {
      val remoteFile = url.getOrElse(s"https://s3-us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_$ver.tar.gz")
      streamz.log.info(s"Downloading targz from [$remoteFile] to [${targz.getAbsolutePath}]")
      (new URL(remoteFile) #> targz).!!
    }
    if (!validGzip(targz)) sys.error(s"Invalid gzip file at [${targz.getAbsolutePath}]")
    if (!jar.exists() || !validJar(jar)) {
      streamz.log.info(s"Extracting jar from [${targz.getAbsolutePath}] to [${jar.getAbsolutePath}]")
      Process(Seq("tar", "xzf", targz.getAbsolutePath), targetDir).!!
    }
    if (!validJar(jar)) sys.error(s"Invalid jar file at [${jar.getAbsolutePath}]")
    jar
  }

}
