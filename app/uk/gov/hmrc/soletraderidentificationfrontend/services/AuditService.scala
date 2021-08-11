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

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.IndividualDetails

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(auditConnector: AuditConnector, soleTraderIdentificationService: SoleTraderIdentificationService) {

  def auditIndividualJourney(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      optFullName <- soleTraderIdentificationService.retrieveFullName(journeyId)
      optDateOfBirth <- soleTraderIdentificationService.retrieveDateOfBirth(journeyId)
      optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorResponse <-
        optIdentifiersMatch match {
          case Some(true) =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
    } yield {
      (optFullName, optDateOfBirth, optNino, optIdentifiersMatch, optAuthenticatorResponse) match {
        case (Some(fullName), Some(dateOfBirth), Some(nino), Some(identifiersMatch), Some(authenticatorDetails: IndividualDetails)) =>
          Json.obj(
            "firstName" -> fullName.firstName,
            "lastName" -> fullName.lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "identifiersMatch" -> identifiersMatch,
            "authenticatorResponse" -> Json.toJson(authenticatorDetails)
          )
        case (Some(fullName), Some(dateOfBirth), Some(nino), Some(identifiersMatch), Some(authenticatorFailureResponse: String)) =>
          Json.obj(
            "firstName" -> fullName.firstName,
            "lastName" -> fullName.lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "identifiersMatch" -> identifiersMatch,
            "authenticatorResponse" -> authenticatorFailureResponse
          )
        case _ =>
          throw new InternalServerException(s"Not enough information to audit individual journey for Journey ID $journeyId")
      }
    }
  }.map {
    auditJson =>
      auditConnector.sendExplicitAudit(
        auditType = "IndividualIdentification",
        detail = auditJson
      )
  }

  def auditSoleTraderJourney(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      optFullName <- soleTraderIdentificationService.retrieveFullName(journeyId)
      optDateOfBirth <- soleTraderIdentificationService.retrieveDateOfBirth(journeyId)
      optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
      optSautr <- soleTraderIdentificationService.retrieveSautr(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorResponse <-
        optIdentifiersMatch match {
          case Some(true) =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
      optBusinessVerificationStatus <- soleTraderIdentificationService.retrieveBusinessVerificationStatus(journeyId)
      optRegistrationStatus <- soleTraderIdentificationService.retrieveRegistrationStatus(journeyId)
    } yield {
      (optFullName, optDateOfBirth, optNino, optSautr, optIdentifiersMatch, optAuthenticatorResponse, optBusinessVerificationStatus, optRegistrationStatus) match {
        case (Some(fullName), Some(dateOfBirth), Some(nino), optSautr, Some(identifiersMatch), Some(authenticatorResponse), Some(businessVerificationStatus), Some(registrationStatus)) =>
          val sautrBlock =
            optSautr match {
              case Some(sautr) => Json.obj("userSAUTR" -> sautr)
              case _ => Json.obj()
            }

          val authenticatorResponseBlock =
            authenticatorResponse match {
              case authenticatorDetails: IndividualDetails => Json.obj("authenticatorResponse" -> Json.toJson(authenticatorDetails))
              case authenticatorFailureResponse: String => Json.obj("authenticatorResponse" -> authenticatorFailureResponse)
            }

          Json.obj(
            "businessType" -> "Sole Trader",
            "firstName" -> fullName.firstName,
            "lastName" -> fullName.lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "sautrMatch" -> identifiersMatch,
            "VerificationStatus" -> businessVerificationStatus,
            "RegisterApiStatus" -> registrationStatus
          ) ++ sautrBlock ++ authenticatorResponseBlock
        case _ =>
          throw new InternalServerException(s"Not enough information to audit sole trader journey for Journey ID $journeyId")
      }
    }
  }.map {
    auditJson =>
      auditConnector.sendExplicitAudit(
        auditType = "SoleTraderRegistration",
        detail = auditJson
      )
  }

}
