name    := "monitoring"
version := "1.0.1"

organization := "uk.ac.wellcome"
scalaVersion := "2.12.6"

libraryDependencies := Dependencies.libraryDependencies

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding",
  "UTF-8",
  "-Xlint",
  "-Xverify",

  // Commented out because I can't find a way to have fatal warnings and
  // emit deprecations in code we test.  :sob:
  // "-Xfatal-warnings",

  "-feature",
  "-language:postfixOps"
)

publishMavenStyle := true

publishTo := Some(
  "S3 releases" at "s3://releases.mvn-repo.wellcomecollection.org/"
)

publishArtifact in Test := true
