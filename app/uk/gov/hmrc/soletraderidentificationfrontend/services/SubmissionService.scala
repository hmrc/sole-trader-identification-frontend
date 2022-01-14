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
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(soleTraderMatchingService: SoleTraderMatchingService,
                                  soleTraderIdentificationService: SoleTraderIdentificationService,
                                  businessVerificationService: BusinessVerificationService,
                                  createTrnService: CreateTrnService,
                                  registrationOrchestrationService: RegistrationOrchestrationService) extends FeatureSwitching {

  type MatchingResult = Either[SoleTraderDetailsMatching.SoleTraderDetailsMatchFailure, Boolean]

  def submit(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier,
                                                              ec: ExecutionContext): Future[SubmissionResponse] =
    for {
      individualDetails <- soleTraderIdentificationService
        .retrieveIndividualDetails(journeyId)
        .map(_.getOrElse(noDataServerException(journeyId)))

      _ <- checkThatNinoPreconditionsAreMet(individualDetails.optNino)

      matchingResult <- calculateMatchingResult(journeyId, journeyConfig, individualDetails)

      response <- calculateSubmissionResponse(
        journeyId,
        calculateBusinessScenario(journeyConfig),
        matchingResult,
        journeyConfig,
        individualDetails
      )
    } yield
      response

  private def noDataServerException(journeyId: String): Nothing =
    throw new InternalServerException(s"Details could not be retrieved from the database for $journeyId")

  private def calculateBusinessScenario(journeyConfig: JourneyConfig): BusinessScenario =
    if (!journeyConfig.pageConfig.enableSautrCheck) IndividualJourney
    else {
      if (journeyConfig.businessVerificationCheck) SoleTraderJourneyWithBusinessVerification
      else SoleTraderJourneyWithoutBusinessVerification
    }


  private def calculateSubmissionResponse(journeyId: String,
                                          businessScenario: BusinessScenario,
                                          matchingResult: MatchingResult,
                                          journeyConfig: JourneyConfig,
                                          individualDetails: IndividualDetails)(implicit hc: HeaderCarrier,
                                                                                ec: ExecutionContext): Future[SubmissionResponse] = businessScenario match {
    case SoleTraderJourneyWithoutBusinessVerification =>
      handleSoleTraderJourneySkippingBVCheck(journeyId, matchingResult, journeyConfig.continueUrl, individualDetails.optSautr, individualDetails.optNino)
    case SoleTraderJourneyWithBusinessVerification =>
      handleSoleTraderJourneyWithBVCheck(journeyId, matchingResult, journeyConfig, individualDetails)
    case IndividualJourney =>
      handleIndividualJourney(journeyId, matchingResult, journeyConfig.continueUrl)
  }

  private def handleSoleTraderJourneySkippingBVCheck(journeyId: String,
                                                     matchingResult: MatchingResult,
                                                     continueUrl: String,
                                                     optSaUtr: Option[String],
                                                     optNino: Option[String]
                                                    )
                                                    (implicit hc: HeaderCarrier,
                                                     ec: ExecutionContext): Future[SubmissionResponse] = matchingResult match {
    case Right(true) if optSaUtr.isDefined =>
      registrationOrchestrationService
        .registerWithoutBusinessVerification(journeyId, optNino, optSaUtr.getOrElse(throwASaUtrNotDefinedException))
        .map(_ => JourneyCompleted(continueUrl))

    case Right(true | false) if optNino.isEmpty =>
      for {
        _ <- createTrnService.createTrn(journeyId)
        _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
      } yield
        JourneyCompleted(continueUrl)

    case Right(true | false) =>
      soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
        .map(_ => JourneyCompleted(continueUrl))

    case Left(failure) =>
      soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
        .map(_ => SoleTraderDetailsMismatch(failure))

  }

  private def handleSoleTraderJourneyWithBVCheck(journeyId: String,
                                                 matchingResult: MatchingResult,
                                                 journeyConfig: JourneyConfig,
                                                 individualDetails: IndividualDetails)
                                                (implicit hc: HeaderCarrier,
                                                 ec: ExecutionContext): Future[SubmissionResponse] = matchingResult match {
    case Right(true) if individualDetails.optSautr.nonEmpty =>
      businessVerificationService.createBusinessVerificationJourney(journeyId, individualDetails.optSautr.getOrElse(throwASaUtrNotDefinedException)).flatMap {
        case Right(BusinessVerificationJourneyCreated(businessVerificationUrl)) =>
          Future.successful(StartBusinessVerification(businessVerificationUrl))
        case _ =>
          for {
            _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
          } yield
            JourneyCompleted(journeyConfig.continueUrl)
      }

    case Right(_) if individualDetails.optNino.isEmpty => for {
      _ <- createTrnService.createTrn(journeyId)
      _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
      _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
    } yield
      JourneyCompleted(journeyConfig.continueUrl)

    case Right(_) => for {
      _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
      _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
    } yield
      JourneyCompleted(journeyConfig.continueUrl)

    case Left(failureReason) =>
      for {
        _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
        _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
      } yield
        SoleTraderDetailsMismatch(failureReason)
  }

  private def throwASaUtrNotDefinedException: Nothing =
    throw new IllegalStateException("Error: SA UTR is not defined")

  private def handleIndividualJourney(journeyId: String,
                                      matchingResult: MatchingResult,
                                      continueUrl: String): Future[SubmissionResponse] = matchingResult match {
    case Right(true | false) =>
      Future.successful(JourneyCompleted(continueUrl))

    case Left(failureReason) =>
      Future.successful(SoleTraderDetailsMismatch(failureReason))
  }

  private def calculateMatchingResult(journeyId: String, journeyConfig: JourneyConfig, individualDetails: IndividualDetails)
                                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MatchingResult] =
    if (individualDetails.optNino.isEmpty)
      soleTraderMatchingService.matchSoleTraderDetailsNoNino(journeyId, individualDetails)
    else
      soleTraderMatchingService.matchSoleTraderDetails(journeyId, individualDetails, journeyConfig)

  private def checkThatNinoPreconditionsAreMet(optNino: Option[String]): Future[Unit] =
    if (optNino.isEmpty && !isEnabled(EnableOptionalNinoJourney))
      Future.failed(throw new IllegalStateException(s"This cannot be because Nino is empty and EnableOptionalNinoJourney is false"))
    else
      Future.successful(())

  private sealed trait BusinessScenario

  private case object SoleTraderJourneyWithoutBusinessVerification extends BusinessScenario

  private case object SoleTraderJourneyWithBusinessVerification extends BusinessScenario

  private case object IndividualJourney extends BusinessScenario

}
