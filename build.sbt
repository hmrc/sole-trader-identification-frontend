import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "sole-trader-identification-frontend"

lazy val scoverageSettings = {
  val exclusionList: List[String] = List(
    "<empty>",
    ".*Routes.*",
    ".*Reverse.*",
    "app.*",
    "prod.*",
    "config.*",
    "com.kenshoo.play.metrics.*",
    "testOnlyDoNotUseInAppConf.*",
    "uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.api.*",
    "uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.frontend.*",
    "uk.gov.hmrc.soletraderidentificationfrontend.testonly.*",
    "uk.gov.hmrc.soletraderidentificationfrontend.views.html.*"
  )
  Seq(
    ScoverageKeys.coverageExcludedPackages := exclusionList.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.12"

Test / Keys.fork := true
Test / javaOptions += "-Dlogger.resource=logback-test.xml"
Test / parallelExecution := true

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scoverageSettings)
  .settings(
    scalafmtOnCompile := true,
    libraryDependencies ++= AppDependencies(),
    resolvers += Resolver.jcenterRepo,
    PlayKeys.playDefaultPort := 9717,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s"
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
