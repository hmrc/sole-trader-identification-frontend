import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "10.8.0"
  private val mongoVersion = "2.14.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"      % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"       % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"              % mongoVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"      % "13.14.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.20.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapPlayVersion,
    "org.mockito"             % "mockito-core"             % "5.24.0",
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.10.0",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "7.0.2",
    "org.jsoup"               % "jsoup"                    % "1.23.2",
    "org.playframework"      %% "play-test"                % current,
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.64.8",
  ).map(_ % "test")

  val it: Seq[ModuleID] = Seq(
    "org.wiremock" % "wiremock" % "3.13.2" % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
