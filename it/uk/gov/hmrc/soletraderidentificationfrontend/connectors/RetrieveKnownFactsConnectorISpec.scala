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

import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.KnownFactsStub
import uk.gov.hmrc.soletraderidentificationfrontend.models.{KnownFactsNoContentError, KnownFactsResponse}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.KnownFactsStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class RetrieveKnownFactsConnectorISpec extends ComponentSpecHelper with KnownFactsStub {

  private val retrieveKnownFactsConnector: RetrieveKnownFactsConnector = app.injector.instanceOf[RetrieveKnownFactsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "retrieveKnownFacts" when {
    "the StubKnownFacts is enabled" should {
      "return KnownFacts" when {
        "a nino is not there" in {
          enable(KnownFactsStub)
          stubGetEacdKnownFactsFromStub(testSautr)(OK, testKnownFactsResponse)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Right(KnownFactsResponse(Some(testSaPostcode), None, None))

          verifyGetEacdKnownFactsFromStub(testSautr)
        }
        "the isAbroad flag is not there" in {
          enable(KnownFactsStub)
          stubGetEacdKnownFactsFromStub(testSautr)(OK, testKnownFactsResponseNino)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Right(KnownFactsResponse(Some(testSaPostcode), None, Some(testNino)))

          verifyGetEacdKnownFactsFromStub(testSautr)
        }
        "the isAbroad flag is there" in {
          enable(KnownFactsStub)
          stubGetEacdKnownFactsFromStub(testSautr)(OK, testKnownFactsResponseIsAbroad())

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Right(KnownFactsResponse(None, Some(true), None))

          verifyGetEacdKnownFactsFromStub(testSautr)
        }
      }
      "return a known facts error" when {
        "the known facts service returns no content for a SA Utr" in {
          enable(KnownFactsStub)
          stubGetEacdKnownFactsFromStub(testSautr)(NO_CONTENT)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Left(KnownFactsNoContentError)

          verifyGetEacdKnownFactsFromStub(testSautr)
        }
      }
      "throw an exception" in {
        enable(KnownFactsStub)
        stubGetEacdKnownFactsFromStub(testSautr)(INTERNAL_SERVER_ERROR)

        intercept[InternalServerException](await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr)))
      }
    }
    "the StubKnownFacts is disabled" should {
      "return KnownFacts" when {
        "a nino is not there" in {
          disable(KnownFactsStub)
          stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponse)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Right(KnownFactsResponse(Some(testSaPostcode), None, None))

          verifyGetEacdKnownFacts(testSautr)
        }
        "the isAbroad flag is not there" in {
          disable(KnownFactsStub)
          stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponseNino)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Right(KnownFactsResponse(Some(testSaPostcode), None, Some(testNino)))

          verifyGetEacdKnownFacts(testSautr)
        }
        "the isAbroad flag is there" in {
          disable(KnownFactsStub)
          stubGetEacdKnownFacts(testSautr)(OK, testKnownFactsResponseIsAbroad())

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Right(KnownFactsResponse(None, Some(true), None))

          verifyGetEacdKnownFacts(testSautr)
        }
      }
      "return a known facts error" when {
        "the known facts service returns no content for a SA Utr" in {
          disable(KnownFactsStub)
          stubGetEacdKnownFacts(testSautr)(NO_CONTENT)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe Left(KnownFactsNoContentError)

          verifyGetEacdKnownFacts(testSautr)
        }
      }
      "throw an exception" in {
        disable(KnownFactsStub)
        stubGetEacdKnownFacts(testSautr)(INTERNAL_SERVER_ERROR)

        intercept[InternalServerException](await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr)))
      }
    }
  }

}
