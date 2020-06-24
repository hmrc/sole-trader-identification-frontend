import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.8.0",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.38.0-play-26"
  )

  def commonTestDependencies(scope: Configuration): Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.8" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "com.typesafe.play" %% "play-test" % current % scope
  )

  val test: Seq[ModuleID] = Seq(
    // Bootstrap import needed to access stubMessagesControllerComponents in unit tests
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.8.0" % Test classifier "tests",
    "org.mockito" % "mockito-core" % "3.3.3" % Test
  ) ++ commonTestDependencies(Test)

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % IntegrationTest
  ) ++ commonTestDependencies(IntegrationTest)

  def apply(): Seq[ModuleID] = compile ++ test ++ it

}
