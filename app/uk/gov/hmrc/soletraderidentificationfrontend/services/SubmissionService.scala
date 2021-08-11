/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(journeyService: JourneyService,
                                  soleTraderMatchingService: SoleTraderMatchingService,
                                  soleTraderIdentificationService: SoleTraderIdentificationService,
                                  businessVerificationService: BusinessVerificationService,
                                  auditService: AuditService) {

  def submit(journeyId: String)(implicit hc: HeaderCarrier,
                                ec: ExecutionContext): Future[SubmissionResponse] =
    for {
      journeyConfig <- journeyService.getJourneyConfig(journeyId)
      optIndividualDetails <- soleTraderIdentificationService.retrieveIndividualDetails(journeyId)
      individualDetails = optIndividualDetails.getOrElse(
        throw new InternalServerException(s"Details could not be retrieved from the database for $journeyId")
      )
      matchingResult <- soleTraderMatchingService.matchSoleTraderDetails(journeyId, individualDetails, journeyConfig)
      identifiersMatch = matchingResult match {
        case Right(_) => true
        case _ => false
      }
      _ <- soleTraderIdentificationService.storeIdentifiersMatch(journeyId, identifiersMatch)
      response <- matchingResult match {
        case Right(IndividualDetails(_, _, _, _, Some(sautr))) if individualDetails.optSautr.contains(sautr) =>
          businessVerificationService.createBusinessVerificationJourney(journeyId, sautr).flatMap {
            case Right(BusinessVerificationJourneyCreated(businessVerificationUrl)) =>
              Future.successful(StartBusinessVerification(businessVerificationUrl))
            case _ =>
              for {
                _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
              } yield {
                JourneyCompleted(journeyConfig.continueUrl)
              }
          }
        case Right(_) =>
          for {
            _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
            _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
          } yield {
            JourneyCompleted(journeyConfig.continueUrl)
          }
        case Left(failureReason) =>
          for {
            _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
            _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
          } yield
            SoleTraderDetailsMismatch(failureReason)
      }
    } yield {
      response
    }
}
