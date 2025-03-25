import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "9.11.0"
  private val mongoVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"      % "12.0.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.3"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"             % "mockito-core"            % "5.16.1"             % Test,
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0"           % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.1"              % Test,
    "org.jsoup"               % "jsoup"                   % "1.19.1"             % Test,
    "org.playframework"      %% "play-test"               % current              % Test,
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.8"             % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % mongoVersion         % Test,
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapPlayVersion % Test,
  )

  val it: Seq[ModuleID] = Seq(
    "org.wiremock" % "wiremock" % "3.12.1" % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
