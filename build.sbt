name := "sbt-dynamodb"

description := "Support for running DynamoDB Local in your integration tests"

organization := "com.localytics"

scalaVersion := "2.12.6"

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
// http://www.scala-sbt.org/1.x/docs/Plugins.html#Creating+an+auto+plugin
sbtPlugin := true

// Generate a POM
// http://www.scala-sbt.org/1.x/docs/Publishing.html#Modifying+the+generated+POM
publishMavenStyle := false

// MIT License for bintray
// https://github.com/softprops/bintray-sbt#licenses
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

// Publish to the Localytics organization
// https://github.com/softprops/bintray-sbt#publishing
bintrayOrganization := Some("localytics")

// Bintray labels
// https://github.com/softprops/bintray-sbt#labels
bintrayPackageLabels := Seq("localytics", "sbt", "aws", "dynamodb", "test", "testing")

// Error on conflicting dependencies
// http://www.scala-sbt.org/1.x/docs/Library-Management.html#Conflict+Management
conflictManager := ConflictManager.strict

// Error on circular dependencies
// http://www.scala-sbt.org/1.x/docs/sbt-0.13-Tech-Previews.html#Circular+dependency
updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error)

libraryDependencies +=
  ( "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    // Scalatest 3.0.5 is slightly behind sbt and Scala dependencies for these two modules, so they must be excluded
    // while Strict dependency checking is enabled.
    exclude("org.scala-lang.modules", s"scala-xml_${scalaBinaryVersion.value}")
    exclude("org.scala-lang.modules", s"scala-parser-combinators_${scalaBinaryVersion.value}") )
