import bintray.Keys._
import com.typesafe.sbt.SbtGit._
import sbtrelease.ReleasePlugin._

name := "sbt-dynamodb"

organization := "com.localytics"

sbtPlugin := true

scalacOptions ++= List(
  "-unchecked"
)

versionWithGit

releaseSettings

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)
