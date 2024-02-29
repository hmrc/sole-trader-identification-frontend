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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateNinoIVJourneyConnector._
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.{CreateNinoIVJourneyConnector, RetrieveNinoIVStatusConnector}
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NinoIVService @Inject() (createNinoIVJourneyConnector: CreateNinoIVJourneyConnector,
                               retrieveNinoIVStatusConnector: RetrieveNinoIVStatusConnector,
                               soleTraderIdentificationService: SoleTraderIdentificationService
                              )(implicit val executionContext: ExecutionContext) {

  def createNinoIVJourney(journeyId: String, nino: String, journeyConfig: JourneyConfig)(implicit
    hc: HeaderCarrier
  ): Future[NinoIVJourneyCreationResponse] =
    createNinoIVJourneyConnector.createNinoIdentityVerificationJourney(journeyId, nino, journeyConfig).flatMap {
      case success @ Right(JourneyCreated(_)) =>
        Future.successful(success)
      case Left(NotEnoughEvidence) =>
        soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToChallenge).map { _ =>
          Left(NotEnoughEvidence)
        }
      case Left(UserLockedOut) =>
        soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationFail).map { _ =>
          Left(UserLockedOut)
        }
    }

  def retrieveNinoIVStatus(IVJourneyId: String)(implicit hc: HeaderCarrier): Future[BusinessVerificationStatus] =
    retrieveNinoIVStatusConnector.retrieveNinoIVStatus(IVJourneyId)

}
