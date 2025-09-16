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
    ScoverageKeys.coverageMinimumStmtTotal := 80, // Reduce threshold owing to changes to Scala compiler
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "3.3.6"

Test / Keys.fork := true
Test / javaOptions += "-Dlogger.resource=logback-test.xml"
Test / parallelExecution := true

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scoverageSettings)
  .settings(
    scalafmtOnCompile := true,
    libraryDependencies ++= AppDependencies(),
    PlayKeys.playDefaultPort := 9717,
    // Configure Scala compiler to suppress warnings
    scalacOptions ++= Seq(
      "-Wconf:msg=Flag.*repeatedly:s",
      "-Wconf:src=routes/.*&msg=unused import:silent",
      "-Wconf:src=routes/.*&msg=unused private member:silent",
      "-Wconf:msg=unused import&src=html/.*:s"
      //scalacOptions += "-explain" Useful new compiler feature for Scala 3
    )
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:msg=Flag.*repeatedly:s"
      //"-explain"
    )
  )
