/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.models.FeatureSwitch

import javax.inject.Singleton

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches = Seq(AuthenticatorStub, BusinessVerificationStub, EnableNoNinoJourney, KnownFactsStub, EnableNinoIVJourney, StubNinoIvJourney)

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[FeatureSwitchRegistry].to(this).eagerly()
    )
  }
}

case object AuthenticatorStub extends FeatureSwitch {
  override val configName: String = "feature-switch.authenticator-stub"
  override val displayName: String = "Use stub for Authenticator API"
}

case object BusinessVerificationStub extends FeatureSwitch {
  override val configName: String = "feature-switch.business-verification-stub"
  override val displayName: String = "Use stub for Business Verification flow"
}

case object KnownFactsStub extends FeatureSwitch {
  override val configName: String = "feature-switch.known-facts-stub"
  override val displayName: String = "Use stub for Known Facts API"
}

case object EnableNoNinoJourney extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-no-nino-journey"
  override val displayName: String = "Enable no nino journey"
}

case object EnableNinoIVJourney extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-nino-iv-journey"
  override val displayName: String = "Enable Nino IV journey"
}

case object StubNinoIvJourney extends FeatureSwitch {
  override val configName: String = "feature-switch.stub-nino-iv-journey"
  override val displayName: String = "Stub Nino IV journey"
}