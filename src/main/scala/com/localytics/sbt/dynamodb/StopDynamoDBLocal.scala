package com.localytics.sbt.dynamodb

import com.localytics.sbt.PidUtils
import sbt.File
import sbt.Keys._
import sbt._

object StopDynamoDBLocal {

  def apply(dbPathOpt: Option[String], clean: Boolean, port: Int, baseDir: File, streamz: TaskStreams): Unit = {
    PidUtils.extractPid("jps -ml".!!, port, baseDir) match {
      case Some(pid) =>
        streamz.log.info("Stopping dynamodb local")
        PidUtils.killPidCommand(pid).!
      case None =>
        streamz.log.warn("Cannot find dynamodb local PID")
    }
    if (clean) dbPathOpt.foreach { dbPath =>
      streamz.log.info("Cleaning dynamodb local")
      val dir = new File(dbPath)
      if (dir.exists()) sbt.IO.delete(dir)
    }
  }

}
