/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.testonly.connectors.TestCreateJourneyConnector
import uk.gov.hmrc.soletraderidentificationfrontend.testonly.forms.TestCreateJourneyForm

class UtilsSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "defaultPageConfig" should {
    "use the configured accessibility statement" in {
      Utils.defaultPageConfig(appConfig).accessibilityUrl mustBe None
    }
  }

  "journeyConfigWriter" should {
    "omit the accessibility URL when no override is configured" in {
      val pageConfig = Utils.defaultPageConfig(appConfig)
      val journeyConfig = Utils.defaultJourneyConfig(appConfig, pageConfig, regime = "VATC")

      (TestCreateJourneyConnector.journeyConfigWriter.writes(journeyConfig) \ "accessibilityUrl").toOption mustBe None
    }
  }

  "test journey form" should {
    "bind a blank accessibility URL as no override" in {
      val pageConfig = Utils.defaultPageConfig(appConfig)
      val journeyConfig = Utils.defaultJourneyConfig(appConfig, pageConfig, regime = "VATC")
      val form = TestCreateJourneyForm.form(enableSautrCheck = true)

      val boundForm = form.bind(form.fill(journeyConfig).data + ("accessibilityUrl" -> ""))

      boundForm.errors mustBe empty
      boundForm.value.map(_.pageConfig.accessibilityUrl) mustBe Some(None)
    }
  }
}
