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

import connectors.mocks.MockRegistrationConnector
import helpers.TestConstants._
import org.mockito.Mockito.verifyNoInteractions
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import services.mocks.{MockAuditService, MockCreateTrnService, MockSoleTraderIdentificationService}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.services.RegistrationOrchestrationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationOrchestrationServiceSpec extends AnyWordSpec
  with Matchers
  with MockSoleTraderIdentificationService
  with MockRegistrationConnector
  with MockCreateTrnService
  with MockAuditService {

  object TestService extends RegistrationOrchestrationService(
    mockSoleTraderIdentificationService,
    mockRegistrationConnector,
    mockCreateTrnService,
    mockAuditService
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "register" when {
    "when the user has a nino" should {
      "store the registration response" when {
        "the business entity is successfully verified and then registered" in {
          mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegister(testNino, Some(testSautr), testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig)) mustBe {
            Registered(testSafeId)
          }
          verifyRegistration(testNino, Some(testSautr), testRegime)
          verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
        }

        "when the business entity is verified but fails to register" in {
          mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegister(testNino, Some(testSautr), testRegime)(Future.successful(RegistrationFailed(testRegistrationFailure)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))(Future.successful(SuccessfullyStored))

          val actualRegistrationStatus: RegistrationStatus = await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig))
          actualRegistrationStatus mustBe RegistrationFailed(testRegistrationFailure)

          verifyRegistration(testNino, Some(testSautr), testRegime)
          verifyStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))
        }

        "the business has an IR-SA enrolment and then registers" in {
          mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(SaEnrolled)))
          mockRegister(testNino, Some(testSautr), testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig)) mustBe {
            Registered(testSafeId)
          }
          verifyRegistration(testNino, Some(testSautr), testRegime)
          verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
        }
      }

      "store a registration state of registration not called" when {
        "the business entity did not pass verification" in {
          mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
        }
        "the business entity was not challenged to verify" in {
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
        }
      }

      "throw an Internal Server Exception" when {
        "there is no business verification response in the database" in {
          mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig))
          )
        }

        "there is nothing in the database" in {
          mockRetrieveNino(testJourneyId)(Future.successful(None))
          mockRetrieveSautr(testJourneyId)(Future.successful(None))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig))
          )
        }
      }
    }
    "when the user does not have a nino" should {
      "store the registration response" when {
        "the business entity is successfully verified and then registered" in {
          mockRetrieveNino(testJourneyId)(Future.successful(None))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockCreateTrn(testJourneyId)(Future.successful(testTrn))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegisterWithTrn(testTrn, testSautr, testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig)) mustBe {
            Registered(testSafeId)
          }
          verifyRegistrationWithTrn(testTrn, testSautr, testRegime)
          verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
        }

        "when the business entity is verified but fails to register" in {
          mockRetrieveNino(testJourneyId)(Future.successful(None))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockCreateTrn(testJourneyId)(Future.successful(testTrn))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationPass)))
          mockRegisterWithTrn(testTrn, testSautr, testRegime)(Future.successful(RegistrationFailed(testRegistrationFailure)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))(Future.successful(SuccessfullyStored))

          val actualRegistrationStatus: RegistrationStatus = await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig))
          actualRegistrationStatus mustBe RegistrationFailed(testRegistrationFailure)

          verifyRegistrationWithTrn(testTrn, testSautr, testRegime)
          verifyStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))
        }
      }
      "store a registration state of registration not called" when {
        "the business entity did not pass verification" in {
          mockRetrieveNino(testJourneyId)(Future.successful(None))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationFail)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
        }

        "the business entity was not challenged to verify" in {
          mockRetrieveNino(testJourneyId)(Future.successful(None))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(Some(BusinessVerificationNotEnoughInformationToCallBV)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

          await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig)) mustBe {
            RegistrationNotCalled
          }
          verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
        }
      }
      "throw an Internal Server Exception" when {
        "there is no business verification response in the database" in {
          mockRetrieveNino(testJourneyId)(Future.successful(None))
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveBusinessVerificationStatus(testJourneyId)(Future.successful(None))

          intercept[InternalServerException](
            await(TestService.registerAfterBusinessVerification(testJourneyId, testSoleTraderJourneyConfig))
          )
        }
      }
    }
  }

  "register without business verification (No audit happens)" when {

    "the user has a nino" should {
      "register and then store the registration response" in {
        mockRegister(testNino, Some(testSautr), testRegime)(Future.successful(Registered(testSafeId)))
        mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

        await(TestService.registerWithoutBusinessVerification(testJourneyId, Some(testNino), Some(testSautr), testSoleTraderJourneyConfig)) mustBe {
          Registered(testSafeId)
        }

        verifyRegistration(testNino, Some(testSautr), testRegime)
        verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
        verifyNoInteractions(mockAuditService)
      }
    }

    "the user has no nino and no sautr" should {
      "store the registration response" when {
        "the business entity is successfully registered" in {
          mockRegister(testNino, None, testRegime)(Future.successful(Registered(testSafeId)))
          mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

          await(TestService.registerWithoutBusinessVerification(testJourneyId, Some(testNino), None, testSoleTraderJourneyConfig)) mustBe {
            Registered(testSafeId)
          }

          verifyRegistration(testNino, None, testRegime)
          verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
          verifyNoInteractions(mockAuditService)
        }
        "the business entity is not successfully registered" in {
          mockRegister(testNino, None, testRegime)(Future.successful(RegistrationFailed(testRegistrationFailure)))
          mockStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))(Future.successful(SuccessfullyStored))

          val actualRegistrationStatus: RegistrationStatus = await(TestService.registerWithoutBusinessVerification(testJourneyId, Some(testNino), None, testSoleTraderJourneyConfig))
          actualRegistrationStatus mustBe RegistrationFailed(testRegistrationFailure)

          verifyRegistration(testNino, None, testRegime)
          verifyStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))
          verifyNoInteractions(mockAuditService)
        }
      }
    }

    "the user does not have a nino" should {
      "create a trn, register with it and then store the registration response" in {
        mockCreateTrn(testJourneyId)(Future.successful(testTrn))
        mockRegisterWithTrn(testTrn, testSautr, testRegime)(Future.successful(Registered(testSafeId)))
        mockStoreRegistrationResponse(testJourneyId, Registered(testSafeId))(Future.successful(SuccessfullyStored))

        await(TestService.registerWithoutBusinessVerification(testJourneyId, optNino = None, saUtr = Some(testSautr), testSoleTraderJourneyConfig)) mustBe {
          Registered(testSafeId)
        }

        verifyRegistrationWithTrn(testTrn, testSautr, testRegime)
        verifyStoreRegistrationResponse(testJourneyId, Registered(testSafeId))
        verifyNoInteractions(mockAuditService)
      }

      "return RegistrationFailed if fails to register with Trn" in {
        mockCreateTrn(testJourneyId)(Future.successful(testTrn))
        mockRegisterWithTrn(testTrn, testSautr, testRegime)(Future.successful(RegistrationFailed(testRegistrationFailure)))
        mockStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))(Future.successful(SuccessfullyStored))

        val actualRegistrationStatus: RegistrationStatus = await(TestService.registerWithoutBusinessVerification(testJourneyId, optNino = None, saUtr = Some(testSautr), testSoleTraderJourneyConfig))
        actualRegistrationStatus mustBe RegistrationFailed(testRegistrationFailure)

        verifyRegistrationWithTrn(testTrn, testSautr, testRegime)
        verifyStoreRegistrationResponse(testJourneyId, RegistrationFailed(testRegistrationFailure))
        verifyNoInteractions(mockAuditService)
      }
    }
  }
}
