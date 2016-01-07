package com.localytics.sbt.dynamodb

import scala.util.Try

private[dynamodb] object DynamoDBLocalUtils {

  private val ProcessIDRegex = """\d+ DynamoDBLocal\.jar""".r

  def extractDynamoDBPid(input: String): Option[String] = ProcessIDRegex.findFirstIn(input).map(_.split(" ")(0))

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

  def killPidCommand(pid: String): String = {
    val osName = System.getProperty("os.name") match {
      case n: String if !n.isEmpty => n
      case _ => System.getProperty("os")
    }
    if (osName.toLowerCase.contains("windows")) {
      s"Taskkill /PID $pid /F"
    } else {
      s"kill $pid"
    }
  }

}
