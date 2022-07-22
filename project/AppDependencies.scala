
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.18.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.18.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.67.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "1.22.0-play-28",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3"
  )

  def commonTestDependencies(scope: Configuration): Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
    "org.scalatest" %% "scalatest" % "3.2.9" % scope,
    "org.jsoup" % "jsoup" % "1.14.1" % scope,
    "com.typesafe.play" %% "play-test" % current % scope,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % "0.67.0" % scope

  )

  val test: Seq[ModuleID] = Seq(
    // Bootstrap import needed to access stubMessagesControllerComponents in unit tests
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.18.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.18.0",
    "org.mockito" % "mockito-core" % "3.11.2" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0" % Test
  ) ++ commonTestDependencies(Test)

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.29.1" % IntegrationTest,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % "0.67.0" % IntegrationTest
  ) ++ commonTestDependencies(IntegrationTest)

  def apply(): Seq[ModuleID] = compile ++ test ++ it

}
