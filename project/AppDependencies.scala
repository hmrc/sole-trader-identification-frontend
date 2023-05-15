import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "7.14.0" // later version breaks the play's binding in this project as of 2023.05 up to version 7.15.0
  private val mongoVersion = "0.74.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % mongoVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "6.8.0-play-28", // later version breaks nonce-CSP as of 2023.05 up to version 7.7.0
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"             % "mockito-core"            % "5.2.0"              % "test",
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0"           % "test",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"              % "test,it",
    "org.scalatest"          %% "scalatest"               % "3.2.15"             % "test,it",
    "org.jsoup"               % "jsoup"                   % "1.15.4"             % "test,it",
    "com.typesafe.play"      %% "play-test"               % current              % "test,it",
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.0"             % "test,it",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % mongoVersion         % "test,it",
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapPlayVersion % "test,it",
    "com.github.tomakehurst"  % "wiremock"                % "2.27.2"             % "it"
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
