import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import scoverage.ScoverageKeys

val appName = "sole-trader-identification-frontend"

val silencerVersion = "1.7.12"

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

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies()
  )
  .settings(scoverageSettings)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort := 9717)
  .settings(scalafmtOnCompile := true)
  .settings(
    // ***************
    // Use the silencer plugin to suppress warnings
    // You may turn it on for `views` too to suppress warnings from unused imports in compiled twirl templates, but this will hide other warnings.
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._"
)

Test / Keys.fork := true
Test / javaOptions += "-Dlogger.resource=logback-test.xml"
Test / parallelExecution := true

IntegrationTest / Keys.fork := true
IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value
IntegrationTest / javaOptions += "-Dlogger.resource=logback-test.xml"
addTestReportOption(IntegrationTest, "int-test-reports")
IntegrationTest / parallelExecution := false
majorVersion := 1

scalaVersion := "2.13.8"
