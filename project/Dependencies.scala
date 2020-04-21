import sbt._

object WellcomeDependencies {
  private lazy val versions = new {
    val fixtures = "1.2.0"
    val typesafe = "2.0.0"
  }

  val fixturesLibrary: Seq[ModuleID] = Seq[ModuleID](
    "uk.ac.wellcome" % "fixtures_2.12" % versions.fixtures,
    "uk.ac.wellcome" % "fixtures_2.12" % versions.fixtures % "test" classifier "tests",
  )

  val typesafeLibrary: Seq[ModuleID] = Seq[ModuleID](
    "uk.ac.wellcome" % "typesafe-app_2.12" % versions.typesafe,
    "uk.ac.wellcome" % "typesafe-app_2.12" % versions.typesafe % "test" classifier "tests",
  )
}

object Dependencies {
  lazy val versions = new {
    val aws = "2.11.14"
    val mockito = "1.10.19"
    val scalatest = "3.1.1"
    val scalatestplusMockito = "3.1.0.0"
  }

  val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % versions.scalatest % Test,
    "org.scalatestplus" %% "mockito-1-10" % versions.scalatestplusMockito % Test,
    "org.mockito" % "mockito-core" % versions.mockito % Test
  )

  val libraryDependencies: Seq[ModuleID] = Seq(
    "software.amazon.awssdk" % "cloudwatch" % versions.aws
  ) ++
    testDependencies ++
    WellcomeDependencies.fixturesLibrary ++
    WellcomeDependencies.typesafeLibrary
}
