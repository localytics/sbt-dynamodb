import bintray.Keys._
import com.typesafe.sbt.SbtGit._

name := "sbt-dynamodb"

organization := "com.teambytes.sbt"

sbtPlugin := true

scalacOptions ++= List(
  "-unchecked"
)

versionWithGit

git.baseVersion := "0.1"

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)
