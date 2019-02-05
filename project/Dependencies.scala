import sbt._

object WellcomeDependencies {
  private lazy val versions = new {
    val fixtures = "1.0.0"
  }

  val fixturesLibrary: Seq[ModuleID] = Seq(
    "uk.ac.wellcome" % "fixtures_2.12" % versions.fixtures % "test",
    "uk.ac.wellcome" % "fixtures_2.12" % versions.fixtures % "test" classifier "tests"
  )
}

object Dependencies {

  lazy val versions = new {
    val akka = "2.5.9"
    val aws = "1.11.225"
    val grizzled = "1.3.2"
    val logback = "1.1.8"
    val mockito = "1.9.5"
    val scalatest = "3.0.1"
  }

  val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % versions.scalatest % Test,
    "org.mockito" % "mockito-core" % versions.mockito % Test,
    "com.typesafe.akka" %% "akka-actor" % versions.akka % Test,
    "com.typesafe.akka" %% "akka-stream" % versions.akka % Test
  )

  val loggingDependencies = Seq(
    "org.clapper" %% "grizzled-slf4j" % versions.grizzled,
    "ch.qos.logback" % "logback-classic" % versions.logback,
    "org.slf4j" % "slf4j-api" % "1.7.25"
  )

  val akkaDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % versions.akka,
    "com.typesafe.akka" %% "akka-stream" % versions.akka
  )

  val libraryDependencies: Seq[ModuleID] = Seq(
    "com.amazonaws" % "aws-java-sdk-cloudwatch" % versions.aws
  ) ++
    loggingDependencies ++
    testDependencies ++
    akkaDependencies ++
    WellcomeDependencies.fixturesLibrary
}
