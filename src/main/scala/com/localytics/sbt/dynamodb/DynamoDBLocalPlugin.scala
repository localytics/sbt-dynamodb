package com.localytics.sbt.dynamodb

import sbt._

object DynamoDBLocalPlugin extends AutoPlugin {

  // auto enable plugin http://www.scala-sbt.org/0.13/docs/Plugins.html#Root+plugins+and+triggered+plugins
  override val trigger = allRequirements

  // inject project keys http://www.scala-sbt.org/0.13/docs/Plugins.html#Controlling+the+import+with+autoImport
  val autoImport = DynamoDBLocalKeys

  // inject project settings http://www.scala-sbt.org/0.13/docs/Plugins.html#projectSettings+and+buildSettings
  override lazy val projectSettings = DynamoDBLocalKeys.baseDynamoDBSettings
}
