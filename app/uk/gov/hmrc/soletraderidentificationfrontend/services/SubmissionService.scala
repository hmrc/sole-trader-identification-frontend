/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector.BusinessVerificationJourneyCreated
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateNinoIVJourneyConnector.{JourneyCreated, NotEnoughEvidence}
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{EnableNinoIVJourney, EnableNoNinoJourney => EnableOptionalNinoJourney, FeatureSwitching}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{NotEnoughInformationToMatch, SoleTraderDetailsMatchFailure, SoleTraderDetailsMatchResult, SuccessfulMatch}
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject() (soleTraderMatchingService: SoleTraderMatchingService,
                                   soleTraderIdentificationService: SoleTraderIdentificationService,
                                   businessVerificationService: BusinessVerificationService,
                                   createTrnService: CreateTrnService,
                                   registrationOrchestrationService: RegistrationOrchestrationService,
                                   enrolmentService: EnrolmentService,
                                   ninoInsightsService: NinoInsightsService,
                                   ninoIVService: NinoIVService
                                  )
    extends FeatureSwitching {

  def submit(journeyId: String, journeyConfig: JourneyConfig, enrolments: Enrolments)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[SubmissionResponse] =
    soleTraderIdentificationService.retrieveIndividualDetails(journeyId).flatMap {
      case Some(individualDetails: IndividualDetails) =>
        for {
          _ <- if (individualDetails.optNino.nonEmpty) ninoInsightsService.ninoInsights(journeyId, individualDetails.optNino.get)
               else soleTraderIdentificationService.removeInsights(journeyId)
          matchingResult <-
            if (individualDetails.optNino.isEmpty && !isEnabled(EnableOptionalNinoJourney))
              Future.failed(new IllegalStateException("[Submission Service] Unexpected state of Nino"))
            else if (individualDetails.optNino.isEmpty)
              soleTraderMatchingService.matchSoleTraderDetailsNoNino(journeyId, individualDetails)
            else
              soleTraderMatchingService.matchSoleTraderDetails(journeyId, individualDetails, journeyConfig)
          response <-
            if (journeyConfig.pageConfig.enableSautrCheck)
              handleSoleTraderJourney(journeyId, matchingResult, journeyConfig, individualDetails, enrolments)
            else handleIndividualJourney(matchingResult, journeyConfig.continueUrl)
        } yield response
      case None =>
        throw new InternalServerException(s"Details could not be retrieved from the database for $journeyId")
    }

  private def handleSoleTraderJourney(journeyId: String,
                                      matchingResult: SoleTraderDetailsMatchResult,
                                      journeyConfig: JourneyConfig,
                                      individualDetails: IndividualDetails,
                                      enrolments: Enrolments
                                     )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SubmissionResponse] = matchingResult match {
    case SuccessfulMatch if !journeyConfig.businessVerificationCheck =>
      registrationOrchestrationService
        .registerWithoutBusinessVerification(journeyId, individualDetails.optNino, individualDetails.optSautr, journeyConfig)
        .map(_ => JourneyCompleted(journeyConfig.continueUrl))

    case SuccessfulMatch =>
      (individualDetails.optSautr, individualDetails.optNino) match {
        case (Some(sautr), _) if enrolmentService.checkSaEnrolmentMatch(enrolments, sautr) =>
          for {
            _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, SaEnrolled)
            _ <-
              registrationOrchestrationService
                .registerWithoutBusinessVerification(journeyId, individualDetails.optNino, individualDetails.optSautr, journeyConfig)
          } yield JourneyCompleted(journeyConfig.continueUrl)
        case (optSautr, Some(nino)) if isEnabled(EnableNinoIVJourney) =>
          ninoIVService.createNinoIVJourney(journeyId, nino, journeyConfig).flatMap {
            case Right(JourneyCreated(businessVerificationUrl)) =>
              Future.successful(StartBusinessVerification(businessVerificationUrl))
            case Left(NotEnoughEvidence) if optSautr.isDefined =>
              businessVerificationService.createBusinessVerificationJourney(journeyId, optSautr.get, journeyConfig).flatMap {
                case Right(BusinessVerificationJourneyCreated(businessVerificationUrl)) =>
                  Future.successful(StartBusinessVerification(businessVerificationUrl))
                case _ =>
                  soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled).map { _ =>
                    JourneyCompleted(journeyConfig.continueUrl)
                  }
              }
            case _ =>
              soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled).map { _ =>
                JourneyCompleted(journeyConfig.continueUrl)
              }
          }
        case (Some(sautr), _) =>
          businessVerificationService.createBusinessVerificationJourney(journeyId, sautr, journeyConfig).flatMap {
            case Right(BusinessVerificationJourneyCreated(businessVerificationUrl)) =>
              Future.successful(StartBusinessVerification(businessVerificationUrl))
            case _ =>
              soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled).map { _ =>
                JourneyCompleted(journeyConfig.continueUrl)
              }
          }
        case _ =>
          for {
            _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
            _ <-
              registrationOrchestrationService
                .registerWithoutBusinessVerification(journeyId, individualDetails.optNino, individualDetails.optSautr, journeyConfig)
          } yield JourneyCompleted(journeyConfig.continueUrl)
      }

    case NotEnoughInformationToMatch =>
      for {
        _ <- if (individualDetails.optNino.isEmpty) createTrnService.createTrn(journeyId) else Future.successful(())
        _ <- if (journeyConfig.businessVerificationCheck)
               soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
             else Future.successful(())
        _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
      } yield JourneyCompleted(journeyConfig.continueUrl)

    case failure: SoleTraderDetailsMatchFailure =>
      for {
        _ <- if (journeyConfig.businessVerificationCheck)
               soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
             else Future.successful(())
        _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
      } yield SoleTraderDetailsMismatch(failure)
  }

  private def handleIndividualJourney(matchingResult: SoleTraderDetailsMatchResult, continueUrl: String): Future[SubmissionResponse] =
    matchingResult match {
      case SuccessfulMatch | NotEnoughInformationToMatch =>
        Future.successful(JourneyCompleted(continueUrl))

      case failure: SoleTraderDetailsMatchFailure =>
        Future.successful(SoleTraderDetailsMismatch(failure))
    }
}
