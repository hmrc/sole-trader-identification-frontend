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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector.BusinessVerificationJourneyCreated
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{FeatureSwitching, EnableNoNinoJourney => EnableOptionalNinoJourney}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{NotEnoughInformationToMatch, SoleTraderDetailsMatchFailure, SoleTraderDetailsMatchResult, SuccessfulMatch}
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(soleTraderMatchingService: SoleTraderMatchingService,
                                  soleTraderIdentificationService: SoleTraderIdentificationService,
                                  businessVerificationService: BusinessVerificationService,
                                  createTrnService: CreateTrnService,
                                  registrationOrchestrationService: RegistrationOrchestrationService) extends FeatureSwitching {

  def submit(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier,
                                                              ec: ExecutionContext): Future[SubmissionResponse] =
    soleTraderIdentificationService.retrieveIndividualDetails(journeyId).flatMap {
      case Some(individualDetails: IndividualDetails) =>
        for {
          matchingResult <-
            if (individualDetails.optNino.isEmpty && !isEnabled(EnableOptionalNinoJourney))
              Future.failed(throw new IllegalStateException("[Submission Service] Unexpected state of Nino"))
            else if (individualDetails.optNino.isEmpty) soleTraderMatchingService.matchSoleTraderDetailsNoNino(journeyId, individualDetails)
            else
              soleTraderMatchingService.matchSoleTraderDetails(journeyId, individualDetails, journeyConfig)
          response <-
            if (journeyConfig.pageConfig.enableSautrCheck) {
              if (journeyConfig.businessVerificationCheck) {
                handleSoleTraderJourneyWithBVCheck(journeyId, matchingResult, journeyConfig, individualDetails)
              } else {
                handleSoleTraderJourneySkippingBVCheck(
                  journeyId,
                  matchingResult,
                  journeyConfig,
                  individualDetails)
              }
            } else {
              handleIndividualJourney(matchingResult, journeyConfig.continueUrl)
            }
        } yield response
      case None =>
        throw new InternalServerException(s"Details could not be retrieved from the database for $journeyId")
    }

  private def handleSoleTraderJourneySkippingBVCheck(journeyId: String,
                                                     matchingResult: SoleTraderDetailsMatchResult,
                                                     journeyConfig: JourneyConfig,
                                                     individualDetails: IndividualDetails)
                                                    (implicit hc: HeaderCarrier,
                                                     ec: ExecutionContext): Future[SubmissionResponse] = matchingResult match {
    case SuccessfulMatch =>
      registrationOrchestrationService
        .registerWithoutBusinessVerification(journeyId, individualDetails.optNino, individualDetails.optSautr, journeyConfig)
        .map(_ => JourneyCompleted(journeyConfig.continueUrl))

    case NotEnoughInformationToMatch =>
      for {
        _ <-
          if (individualDetails.optNino.isEmpty) createTrnService.createTrn(journeyId)
          else Future.successful(())
        _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
      } yield
        JourneyCompleted(journeyConfig.continueUrl)

    case failure: SoleTraderDetailsMatchFailure =>
      soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
        .map(_ => SoleTraderDetailsMismatch(failure))
  }

  private def handleSoleTraderJourneyWithBVCheck(journeyId: String,
                                                 matchingResult: SoleTraderDetailsMatchResult,
                                                 journeyConfig: JourneyConfig,
                                                 individualDetails: IndividualDetails)
                                                (implicit hc: HeaderCarrier,
                                                 ec: ExecutionContext): Future[SubmissionResponse] = matchingResult match {
    case SuccessfulMatch if individualDetails.optSautr.nonEmpty =>
      businessVerificationService.createBusinessVerificationJourney(journeyId, individualDetails.optSautr.getOrElse(throwASaUtrNotDefinedException)).flatMap {
        case Right(BusinessVerificationJourneyCreated(businessVerificationUrl)) =>
          Future.successful(StartBusinessVerification(businessVerificationUrl))
        case _ =>
          soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled).map {
            _ => JourneyCompleted(journeyConfig.continueUrl)
          }
      }

    case SuccessfulMatch => for {
      _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
      _ <- registrationOrchestrationService.registerWithoutBusinessVerification(journeyId, individualDetails.optNino, individualDetails.optSautr, journeyConfig)
    } yield
      JourneyCompleted(journeyConfig.continueUrl)

    case NotEnoughInformationToMatch => for {
      _ <-
        if (individualDetails.optNino.isEmpty) createTrnService.createTrn(journeyId)
        else Future.successful((): Unit)
      _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
      _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
    } yield JourneyCompleted(journeyConfig.continueUrl)

    case failure: SoleTraderDetailsMatchFailure => for {
      _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
      _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
    } yield
      SoleTraderDetailsMismatch(failure)
  }

  private def throwASaUtrNotDefinedException: Nothing =
    throw new IllegalStateException("Error: SA UTR is not defined")

  private def handleIndividualJourney(matchingResult: SoleTraderDetailsMatchResult,
                                      continueUrl: String): Future[SubmissionResponse] = matchingResult match {
    case SuccessfulMatch | NotEnoughInformationToMatch =>
      Future.successful(JourneyCompleted(continueUrl))

    case failure: SoleTraderDetailsMatchFailure =>
      Future.successful(SoleTraderDetailsMismatch(failure))
  }

}
