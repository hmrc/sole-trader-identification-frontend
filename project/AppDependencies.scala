
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % "7.1.0",
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28"  % "7.1.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % "0.71.0",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "3.23.0-play-28",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.13.3"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"            % "mockito-core"             % "3.11.2"   % "test",
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"    % "test,it",
    "org.scalatest"          %% "scalatest"               % "3.1.1"    % "test,it",
    "org.jsoup"              % "jsoup"                    % "1.14.1"   % "test,it",
    "com.typesafe.play"      %% "play-test"               % current    % "test,it",
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.36.8"   % "test,it",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % "0.71.0"   % "test,it",
    "com.github.tomakehurst" % "wiremock-jre8"            % "2.29.1"   % "it"
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
