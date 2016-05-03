package com.localytics.sbt.dynamodb

import java.io.File
import scala.util.Try

object DynamoDBLocalUtils {

  def extractDynamoDBPid(input: String, port: Int, baseDir: File): Option[String] = {
    val jarPath = (new File(baseDir, "DynamoDBLocal.jar")).getAbsolutePath
    val pidPortRegex = s"\\d+ ${jarPath} -port ${port}".r
    pidPortRegex.findFirstIn(input).map(_.split(" ")(0))
  }

  def isDynamoDBLocalRunning(port: Int): Boolean =
    Try {
      val socket = new java.net.Socket("localhost", port)
      socket.close()
    }.isSuccess

  def osName: String = System.getProperty("os.name") match {
    case n: String if !n.isEmpty => n
    case _ => System.getProperty("os")
  }

  def killPidCommand(pid: String): String =
    if (osName.toLowerCase.contains("windows")) s"Taskkill /PID $pid /F" else s"kill $pid"

}
