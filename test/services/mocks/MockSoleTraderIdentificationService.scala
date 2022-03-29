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

package services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.SoleTraderDetailsMatchFailure
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderIdentificationService

import java.time.LocalDate
import scala.concurrent.Future

trait MockSoleTraderIdentificationService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockSoleTraderIdentificationService: SoleTraderIdentificationService = mock[SoleTraderIdentificationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSoleTraderIdentificationService)
  }

  def mockRetrieveSautr(journeyId: String)
                       (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveSautr(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveNino(journeyId: String)
                      (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveNino(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveFullName(journeyId: String)
                          (response: Future[Option[FullName]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveFullName(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveDateOfBirth(journeyId: String)
                             (response: Future[Option[LocalDate]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveDateOfBirth(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveIsMatch(journeyId: String)
                                  (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveIsMatch(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveAuthenticatorDetails(journeyId: String)
                                      (response: Future[Option[IndividualDetails]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveAuthenticatorDetails(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveIndividualDetails(journeyId: String)
                                   (response: Future[Option[IndividualDetails]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveIndividualDetails(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveSoleTraderDetails(journeyId: String)
                                   (response: Future[Option[SoleTraderDetails]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveSoleTraderDetails(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveAuthenticatorFailureResponse(journeyId: String)
                                              (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveAuthenticatorFailureResponse(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveBusinessVerificationStatus(journeyId: String)
                                            (response: Future[Option[BusinessVerificationStatus]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveRegistrationResponse(journeyId: String)
                                      (response: Future[Option[RegistrationStatus]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveRegistrationStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveAddress(journeyId: String)
                         (response: Future[Option[Address]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveAddress(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveSaPostcode(journeyId: String)
                            (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveSaPostcode(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveES20Response(journeyId: String)
                              (response: Future[Option[KnownFactsResponse]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveES20Details(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveTrn(journeyId: String)
                     (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveTrn(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreRegistrationResponse(journeyId: String, registrationStatus: RegistrationStatus)
                                   (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus)
                                         (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(businessVerificationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreIsMatch(journeyId: String, isMatch: String)
                               (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeIsMatch(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(isMatch)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreAuthenticatorFailureResponse(journeyId: String, authenticatorFailureResponse: SoleTraderDetailsMatchFailure)
                                           (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeAuthenticatorFailureResponse(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authenticatorFailureResponse)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreAuthenticatorDetails(journeyId: String, authenticatorDetails: IndividualDetails)
                                   (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeAuthenticatorDetails(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authenticatorDetails)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreES20Details(journeyId: String, es20Details: KnownFactsResponse)
                          (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeES20Details(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(es20Details)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreTrn(journeyId: String, trn: String)
                  (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeTrn(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(trn)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def verifyStoreRegistrationResponse(journeyId: String, registrationStatus: RegistrationStatus): Unit =
    verify(mockSoleTraderIdentificationService).storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])

  def verifyStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus): Unit =
    verify(mockSoleTraderIdentificationService).storeBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(businessVerificationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])

  def verifyStoreIsMatch(journeyId: String, isMatch: String): Unit =
    verify(mockSoleTraderIdentificationService).storeIsMatch(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(isMatch)
    )(ArgumentMatchers.any[HeaderCarrier])

  def verifyStoreTrn(journeyId: String, trn: String): Unit =
    verify(mockSoleTraderIdentificationService).storeTrn(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(trn)
    )(ArgumentMatchers.any[HeaderCarrier])

  def verifyStoreAuthenticatorDetails(journeyId: String, authenticatorDetails: IndividualDetails): Unit =
    verify(mockSoleTraderIdentificationService).storeAuthenticatorDetails(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authenticatorDetails)
    )(ArgumentMatchers.any[HeaderCarrier])

  def verifyStoryAuthenticatorFailureResponse(journeyId: String, authenticatorFailureResponse: SoleTraderDetailsMatchFailure): Unit =
    verify(mockSoleTraderIdentificationService).storeAuthenticatorFailureResponse(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authenticatorFailureResponse)
    )(ArgumentMatchers.any[HeaderCarrier])

}
