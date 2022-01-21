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
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationPass, RegistrationNotCalled, RegistrationStatus, SaEnrolled}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(soleTraderIdentificationService: SoleTraderIdentificationService,
                                                 registrationConnector: RegistrationConnector,
                                                 trnService: CreateTrnService,
                                                 auditService: AuditService
                                                )(implicit ec: ExecutionContext) {

  def register(journeyId: String, regime: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = for {
    registrationStatus <- soleTraderIdentificationService.retrieveBusinessVerificationStatus(journeyId).flatMap {
      case Some(BusinessVerificationPass) | Some(SaEnrolled) => for {
        optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
        optSautr <- soleTraderIdentificationService.retrieveSautr(journeyId)
        registrationStatus <- register(journeyId, optNino, optSautr, regime)
      } yield registrationStatus
      case Some(_) =>
        Future.successful(RegistrationNotCalled)
      case None =>
        throw new InternalServerException(s"Missing business verification state in database for $journeyId")
    }
    _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, registrationStatus)
  } yield {
    auditService.auditSoleTraderJourney(journeyId)
    registrationStatus
  }

  def registerWithoutBusinessVerification(journeyId: String, optNino: Option[String], saUtr: Option[String], regime: String)
                                         (implicit hc: HeaderCarrier): Future[RegistrationStatus] = for {
    registrationStatus <- register(journeyId, optNino, saUtr, regime)
    _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, registrationStatus)
  } yield {
    auditService.auditSoleTraderJourney(journeyId)
    registrationStatus
  }

  private def register(journeyId: String,
                       optNino: Option[String],
                       optSautr: Option[String],
                       regime: String
                      )(implicit hc: HeaderCarrier): Future[RegistrationStatus] =
    (optNino, optSautr, regime) match {
      case (Some(nino), optSautr, regime) =>
        registrationConnector.registerWithNino(nino, optSautr, regime)
      case (None, optSautr, regime) =>
        trnService.createTrn(journeyId) flatMap {
          trn =>
            registrationConnector.registerWithTrn(
              trn,
              optSautr.getOrElse(throw new InternalServerException(s"Missing sautr required for Register call with TRN for $journeyId")),
              regime
            )
        }
    }
}
