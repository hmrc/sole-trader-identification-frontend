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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers.{CREATED, INTERNAL_SERVER_ERROR, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.{testAddress, testAddressWrongPostcodeFormat, testDateOfBirth, testFullName, testFullNameLowerCase, testNonUKAddress, testTrn}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.CreateTrnStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class CreateTrnConnectorISpec extends ComponentSpecHelper with CreateTrnStub {

  private val createTrnConnector = app.injector.instanceOf[CreateTrnConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "createTrn" should {
    "return the TRN" when {
      "there is a UK address" in {
        stubCreateTrn(testDateOfBirth, testFullName, testAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))

        val result = await(createTrnConnector.createTrn(testDateOfBirth, testFullName, testAddress))

        result mustBe testTrn
      }
      "there is a non-uk address" in {
        stubCreateTrn(testDateOfBirth, testFullName, testNonUKAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))

        val result = await(createTrnConnector.createTrn(testDateOfBirth, testFullName, testNonUKAddress))

        result mustBe testTrn
      }
      "the user gives a postcode with the wrong format" in {
        stubCreateTrn(testDateOfBirth, testFullName, testAddressWrongPostcodeFormat)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))

        val result = await(createTrnConnector.createTrn(testDateOfBirth, testFullName, testAddressWrongPostcodeFormat))

        result mustBe testTrn
      }
      "the user gives a full name with lowercase letters" in {
        stubCreateTrn(testDateOfBirth, testFullNameLowerCase, testAddress)(CREATED, Json.obj("temporaryReferenceNumber" -> testTrn))

        val result = await(createTrnConnector.createTrn(testDateOfBirth, testFullNameLowerCase, testAddress))

        result mustBe testTrn
      }
    }
    "throw an exception" when {
      "any other status is received" in {
        stubCreateTrn(testDateOfBirth, testFullName, testAddress)(INTERNAL_SERVER_ERROR, Json.obj())

        intercept[InternalServerException](await(createTrnConnector.createTrn(testDateOfBirth, testFullName, testAddress)))
      }
    }
  }

}
