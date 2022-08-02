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

package services

import helpers.TestConstants.{testEnrolments, testSautr}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.soletraderidentificationfrontend.services.EnrolmentService

class EnrolmentServiceSpec extends AnyWordSpec
  with Matchers {

  object TestService extends EnrolmentService

  "checkSaEnrolment" should {
    "return true" when {
      "an IR-SA enrolment is found with a matching sautr" in {
        TestService.checkSaEnrolmentMatch(testEnrolments, testSautr) mustBe true
      }
    }
    "return false" when {
      "an IR-SA enrolment is found but the sautr doesn't match" in {
        val testSautr = "2234567890"
        TestService.checkSaEnrolmentMatch(testEnrolments, testSautr) mustBe false
      }
      "no IR-SA enrolment is found" in {
        TestService.checkSaEnrolmentMatch(Enrolments(Set.empty), testSautr) mustBe false
      }
    }
  }
}
