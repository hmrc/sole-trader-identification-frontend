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

package utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfigUrlAllowed, JourneyConfigUrlNotAllowed}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.UrlHelper

class UrlHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object TestUrlHelper extends UrlHelper(appConfig)

  val relativeUrl: String = "/test"
  val localhostUrl: String = "http://localhost:9000/test"
  val localHostUrlWithAtSymbol: String = "http://localhost:9000/symbol/@"
  val absoluteUrl: String = "http://some-host:9000/test"

  "UrlHelper" should {

    "allow relative urls" in {

      TestUrlHelper.isAValidUrl(relativeUrl) mustBe JourneyConfigUrlAllowed

    }

    "allow an absolute Url if the host is localhost" in {

      TestUrlHelper.isAValidUrl(localhostUrl) mustBe JourneyConfigUrlAllowed

    }

    "not allow an absolute localhost url containing the '@' symbol" in {

      TestUrlHelper.isAValidUrl(localHostUrlWithAtSymbol) mustBe JourneyConfigUrlNotAllowed

    }

    "not allow an absolute url where the host is not localhost (i.e. the host is not in the allowed hosts list)" in {

      TestUrlHelper.isAValidUrl(absoluteUrl) mustBe JourneyConfigUrlNotAllowed

    }

  }

}
