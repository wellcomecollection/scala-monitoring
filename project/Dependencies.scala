import sbt._

object WellcomeDependencies {
  private lazy val versions = new {
    val typesafe = "1.1.0"
  }

  val typesafeLibrary: Seq[ModuleID] = Seq[ModuleID](
    "uk.ac.wellcome" % "typesafe-app_2.12" % versions.typesafe,
    "uk.ac.wellcome" % "typesafe-app_2.12" % versions.typesafe % "test" classifier "tests",
  )
}

object Dependencies {

  lazy val versions = new {
    val aws = "1.11.225"
    val mockito = "1.9.5"
    val scalatest = "3.0.1"
  }

  val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % versions.scalatest % Test,
    "org.mockito" % "mockito-core" % versions.mockito % Test
  )

  val libraryDependencies: Seq[ModuleID] = Seq(
    "com.amazonaws" % "aws-java-sdk-cloudwatch" % versions.aws
  ) ++
    testDependencies ++
    WellcomeDependencies.typesafeLibrary
}
