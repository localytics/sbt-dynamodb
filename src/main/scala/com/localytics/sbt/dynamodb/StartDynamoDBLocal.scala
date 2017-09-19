package com.localytics.sbt.dynamodb

import java.net.Socket

import sbt.File
import sbt.Keys._

import scala.sys.process._
import scala.util.Try

object StartDynamoDBLocal {

  private[dynamodb] def isDynamoDBLocalRunning(port: Int): Boolean = Try(new Socket("localhost", port).close()).isSuccess

  def apply(baseDir: File, port: Int, heapSize: Option[Int], dbPath: Option[String], inMem: Boolean, shared: Boolean, streamz: TaskStreams): String = {
    val jar = new File(baseDir, "DynamoDBLocal.jar")
    val lib = new File(baseDir, "DynamoDBLocal_lib")

    val args = Seq("java", s"-Djava.library.path=${lib.getAbsolutePath}") ++
      heapSize.map(mb => Seq(s"-Xms${mb}m", s"-Xmx${mb}m")).getOrElse(Nil) ++
      Seq("-jar", jar.getAbsolutePath) ++
      Seq("-port", port.toString) ++
      dbPath.map(db => Seq("-dbPath", db)).getOrElse(Nil) ++
      (if (inMem) Seq("-inMemory") else Nil) ++
      (if (shared) Seq("-sharedDb") else Nil)

    if (isDynamoDBLocalRunning(port)) {
      streamz.log.warn(s"dynamodb local is already running on port $port")
    } else {
      streamz.log.info("Starting dynamodb local")
      Process(args).run()
      do {
        streamz.log.info(s"Waiting for dynamodb local to boot on port $port")
        Thread.sleep(500)
      } while (!isDynamoDBLocalRunning(port))
    }
    PidUtils.extractPid("jps -ml".!!, port, jar).getOrElse {
      sys.error(s"Cannot find dynamodb local PID running on $port")
    }
  }

}
