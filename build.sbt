import bintray.Keys._
import com.typesafe.sbt.SbtGit._
import sbtrelease.ReleasePlugin._

name := "sbt-dynamodb"

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

versionWithGit

releaseSettings

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

// Error on conflicting dependencies
// http://www.scala-sbt.org/0.13/docs/Library-Management.html#Conflict+Management
conflictManager := ConflictManager.strict

// Error on circular dependencies
// http://www.scala-sbt.org/0.13/docs/sbt-0.13-Tech-Previews.html#Circular+dependency
updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % "test"
