package com.localytics.sbt.dynamodb

import java.io.File
import java.util.regex.Pattern

object PidUtils {

  def extractPid(input: String, port: Int, jar: File): Option[String] = {
    val pidPortRegex = s"\\d+ ${Pattern.quote(jar.getAbsolutePath)} -port $port".r
    pidPortRegex.findFirstIn(input).map(_.split(" ")(0))
  }

  def osName: String = System.getProperty("os.name") match {
    case n: String if !n.isEmpty => n
    case _ => System.getProperty("os")
  }

  def killPidCommand(pid: String): String =
    if (osName.toLowerCase.contains("windows")) s"Taskkill /PID $pid /F" else s"kill $pid"

}
