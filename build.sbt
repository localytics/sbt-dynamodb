name := "sbt-dynamodb"

description := "Support for running DynamoDB Local in your integration tests"

organization := "com.localytics"

// Sane set of compiler flags
scalacOptions ++= Seq(
  "-deprecation",         // Emit warning and location for usages of deprecated APIs
  "-encoding", "UTF-8",   // Specify character encoding used by source files (yes, this is 2 args)
  "-feature",             // Emit warning and location for usages of features that should be imported explicitly
  "-unchecked",           // Enable detailed unchecked (erasure) warnings
  "-Xfatal-warnings",     // Fail the compilation if there are any warnings
  "-Xlint",               // Enable recommended additional warnings
  "-Xfuture",             // Turn on future language features.
  "-Yno-adapted-args"     // Do not adapt an argument list (by inserting () or creating a tuple) to match the receiver
)

// This is an auto plugin
// http://www.scala-sbt.org/0.13/docs/Plugins.html#Creating+an+auto+plugin
sbtPlugin := true

// Generate a POM
// http://www.scala-sbt.org/0.13/docs/Publishing.html#Modifying+the+generated+POM
publishMavenStyle := false

// MIT License for bintray
// https://github.com/softprops/bintray-sbt#licenses
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

// Publish to the Localytics organization
// https://github.com/softprops/bintray-sbt#publishing
bintrayOrganization := Some("localytics")

// Split staging from publishing
// https://github.com/softprops/bintray-sbt#staging-optional
bintrayReleaseOnPublish in ThisBuild := false

// Bintray labels
// https://github.com/softprops/bintray-sbt#labels
bintrayPackageLabels := Seq("localytics", "sbt", "aws", "dynamodb", "test", "testing")

// Error on conflicting dependencies
// http://www.scala-sbt.org/0.13/docs/Library-Management.html#Conflict+Management
conflictManager := ConflictManager.strict

// Error on circular dependencies
// http://www.scala-sbt.org/0.13/docs/sbt-0.13-Tech-Previews.html#Circular+dependency
updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
