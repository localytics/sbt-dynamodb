import bintray.Keys._
import com.typesafe.sbt.SbtGit._

name := "sbt-dynamodb"

organization := "com.teambytes"

sbtPlugin := true

scalacOptions ++= List(
  "-unchecked"
)

versionWithGit

git.baseVersion := "0.1"

publishMavenStyle := false

bintrayPublishSettings

repository in bintry := "sbt-plugins"

bintryOrganization in bintry := None

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

