val projectName = "monitoring"
val projectVersion = "1.2.1"



// Everything below this line is generic boilerplate that should be reusable,
// unmodified, in all of our Scala libraries that have a "core" and a "typesafe"
// version.

val settings: Seq[Def.Setting[_]] = Seq(
  scalaVersion := "2.12.6",
  organization := "uk.ac.wellcome",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-encoding",
    "UTF-8",
    "-Xlint",
    "-Xverify",

    // Currently commented out because we have tests for a deprecated method,
    // and I CBA how to say "this deprecation warning is okay".
    // "-Xfatal-warnings",

    "-feature",
    "-language:postfixOps"
  ),
  parallelExecution in Test := false,

  resolvers ++= Seq(
    "S3 releases" at "s3://releases.mvn-repo.wellcomecollection.org/"
  ),

  publishMavenStyle := true,
  publishTo := Some(
    "S3 releases" at "s3://releases.mvn-repo.wellcomecollection.org/"
  ),
  publishArtifact in Test := true,

  version := projectVersion
)

lazy val lib =
  project
    .withId(projectName)
    .in(new File(projectName))
    .settings(settings)
    .settings(libraryDependencies ++= Dependencies.libraryDependencies)

lazy val lib_typesafe =
  project
    .withId(s"${projectName}_typesafe")
    .in(new File(s"${projectName}_typesafe"))
    .settings(settings)
    .dependsOn(lib % "compile->compile;test->test")
    .settings(libraryDependencies ++= Dependencies.typesafeDependencies)

lazy val root = (project in file(".")).aggregate(lib, lib_typesafe)
