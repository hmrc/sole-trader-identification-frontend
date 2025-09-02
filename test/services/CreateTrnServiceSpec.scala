/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.mocks.MockCreateTrnConnector
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.services.CreateTrnService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateTrnServiceSpec extends AnyWordSpec with Matchers with MockSoleTraderIdentificationService with MockCreateTrnConnector {

  object TestService extends CreateTrnService(mockSoleTraderIdentificationService, mockCreateTrnConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "createTrn" should {
    "return SuccessfulCreation" in {
      mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
      mockRetrieveAddress(testJourneyId)(Future.successful(Some(testAddress)))
      mockStoreTrn(testJourneyId, testTrn)(Future.successful(SuccessfullyStored))
      mockCreateTrn(testDateOfBirth, testFullName, testAddress)(Future.successful(testTrn))

      val result = await(TestService.createTrn(testJourneyId))

      result mustBe testTrn

      verifyStoreTrn(testJourneyId, testTrn)
      verifyCreateTrn(testDateOfBirth, testFullName, testAddress)
    }
    "return SuccessfulCreation with wrong postcode format" in {
      mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
      mockRetrieveAddress(testJourneyId)(Future.successful(Some(testAddressWrongPostcodeFormat)))
      mockStoreTrn(testJourneyId, testTrn)(Future.successful(SuccessfullyStored))
      mockCreateTrn(testDateOfBirth, testFullName, testAddress)(Future.successful(testTrn))

      val result = await(TestService.createTrn(testJourneyId))

      result mustBe testTrn

      verifyStoreTrn(testJourneyId, testTrn)
      verifyCreateTrn(testDateOfBirth, testFullName, testAddress)
    }
    "return SuccessfulCreation with full name lowercase" in {
      mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
      mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullNameLowecase)))
      mockRetrieveAddress(testJourneyId)(Future.successful(Some(testAddress)))
      mockStoreTrn(testJourneyId, testTrn)(Future.successful(SuccessfullyStored))
      mockCreateTrn(testDateOfBirth, testFullName, testAddress)(Future.successful(testTrn))

      val result = await(TestService.createTrn(testJourneyId))

      result mustBe testTrn

      verifyStoreTrn(testJourneyId, testTrn)
      verifyCreateTrn(testDateOfBirth, testFullName, testAddress)
    }
    "throw an exception" when {
      "there is not enough data to create a TRN" in {
        mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
        mockRetrieveFullName(testJourneyId)(Future.successful(None))
        mockRetrieveAddress(testJourneyId)(Future.successful(None))

        intercept[InternalServerException](
          await(TestService.createTrn(testJourneyId))
        )
      }
    }

  }
}
