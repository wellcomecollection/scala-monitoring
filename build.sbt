val name = "monitoring"
val version = "1.2.1"



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

  version := version
)

lazy val typesafeDependencies = Seq[ModuleID](
  "uk.ac.wellcome" % "typesafe-app_2.12" % "1.0.0"
)

lazy val lib =
  project
    .withId(name)
    .in(new File(name))
    .settings(settings)
    .settings(libraryDependencies ++= Dependencies.libraryDependencies)

lazy val lib_typesafe =
  project
    .withId(s"${name}_typesafe")
    .in(new File(s"${name}_typesafe"))
    .settings(settings)
    .dependsOn(lib % "compile->compile;test->test")
    .settings(libraryDependencies ++= typesafeDependencies)

lazy val root = (project in file(".")).aggregate(lib, lib_typesafe)
