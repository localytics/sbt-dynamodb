package com.localytics.sbt.dynamodb

import java.io.File

import scala.util.Try

private[dynamodb] object Utils {

  private val ProcessIDRegex = """\d+ DynamoDBLocal\.jar""".r

  def extractDynamoDBPid(input: String): Option[String] = ProcessIDRegex.findFirstIn(input).map(_.split(" ")(0))

  def cleanDynamoDBLocal(clean: Boolean, dataDir: Option[String], pid: String) = {
    if (clean && dataDir.exists(d => new File(d).exists())) sbt.IO.delete(new File(dataDir.get))
  }

  def isDynamoDBLocalRunning(port: Int): Boolean = {
    Try {
      val socket = new java.net.Socket("localhost", port)
      socket.close()
    }.isSuccess
  }

  def waitForDynamoDBLocal(port: Int, infoPrintFunc: String => Unit): Unit = {
    var continue = false
    while (!continue) {
      continue = true
      Try {
        val socket = new java.net.Socket("localhost", port)
        socket.close()
      }.recover {
        case e: Exception =>
          infoPrintFunc(s"Waiting for dynamodb local to boot on port $port")
          Thread.sleep(500)
          continue = false
      }
    }
  }

}
