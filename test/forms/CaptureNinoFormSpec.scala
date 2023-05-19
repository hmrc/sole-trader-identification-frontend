/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.Form
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureNinoForm.{form => captureNinoForm}

class CaptureNinoFormSpec extends AnyWordSpec with Matchers {

  val ninoForm: Form[_] = captureNinoForm

  "CaptureNinoForm.value" should {
    for (nino <- ninoWithInvalidPrefixSeq ++ ninoWithInvalidSuffixSeq ++ testInvalidNinoAdditionalSeq)
      s"return None for an invalid nino: $nino" in {
        ninoForm.bind(Map("nino" -> nino)).value mustBe None
      }

    for (nino <- ninoWithValidPrefixSeq ++ testValidNinoAdditionalSeq)
      s"return Some($nino) for a valid nino" in {
        ninoForm.bind(Map("nino" -> nino)).value mustBe Some(nino.trim)
      }

    for (nino <- ninoWithInvalidPrefixSeq.map(_.grouped(2).mkString(" ")) ++ ninoWithInvalidSuffixSeq.map(_.grouped(2).mkString(" ")))
      s"return None for an invalid (spaced) nino: $nino" in {
        ninoForm.bind(Map("nino" -> nino)).value mustBe None
      }

    for (nino <- ninoWithValidPrefixSeq.map(_.grouped(2).mkString(" "))) {
      s"return Some($nino) for a valid (spaced) nino" in {
        ninoForm.bind(Map("nino" -> nino)).value mustBe Some(nino)
      }
    }

    "trim the whitespaces from nino and return Some(nino)" in {
      ninoForm.bind(Map("nino" -> " AA123456A ")).value mustBe Some("AA123456A")
    }

    "trim the whitespaces from (spaced) nino and return Some(nino)" in {
      ninoForm.bind(Map("nino" -> " AA 12 34 56 A ")).value mustBe Some("AA 12 34 56 A")
    }
  }
}
