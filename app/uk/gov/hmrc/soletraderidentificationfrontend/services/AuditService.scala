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

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(appConfig: AppConfig,
                             auditConnector: AuditConnector,
                             soleTraderIdentificationService: SoleTraderIdentificationService) {

  def auditJourney(journeyId: String, journeyConfig: JourneyConfig)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = if (journeyConfig.pageConfig.enableSautrCheck)
    auditSoleTraderJourney(journeyId, journeyConfig)
  else
    auditIndividualJourney(journeyId, journeyConfig)

  private def auditSoleTraderJourney(journeyId: String, journeyConfig: JourneyConfig)
                                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val soleTraderAudit: Future[JsObject] = for {
      optSoleTraderRecord <- soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId)
      optES20Response <- soleTraderIdentificationService.retrieveES20Details(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorResponse <-
        optIdentifiersMatch match {
          case Some(_) if optSoleTraderRecord.exists(details => details.optNino.isEmpty) => Future.successful(None)
          case Some(identifiersMatch) if identifiersMatch =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
    } yield {
      (optSoleTraderRecord, optES20Response, optIdentifiersMatch, optAuthenticatorResponse) match {
        case (Some(soleTraderRecord), optES20Response, Some(identifiersMatch), optAuthenticatorResponse) =>

          val callingService: String = journeyConfig.pageConfig.optServiceName.getOrElse(appConfig.defaultServiceName)

          val registrationStatusBlock =
            soleTraderRecord.registrationStatus match {
              case Some(registrationStatus) => registrationStatus match {
                case Registered(_) => Json.obj("RegisterApiStatus" -> "success")
                case RegistrationFailed => Json.obj("RegisterApiStatus" -> "fail")
                case RegistrationNotCalled => Json.obj("RegisterApiStatus" -> "not called")
              }
              case _ => Json.obj()
            }

          val sautrBlock =
            soleTraderRecord.optSautr match {
              case Some(sautr) => Json.obj("userSAUTR" -> sautr)
              case _ => Json.obj()
            }

          val ninoBlock =
            soleTraderRecord.optNino match {
              case Some(nino) => Json.obj("nino" -> nino)
              case _ => Json.obj()
            }

          val addressBlock =
            soleTraderRecord.address match {
              case Some(address) => Json.obj("address" -> Json.toJson(address))
              case _ => Json.obj()
            }

          val trnBlock =
            soleTraderRecord.optTrn match {
              case Some(trn) => Json.obj("TempNI" -> trn)
              case _ => Json.obj()
            }

          val saPostCodeBlock =
            soleTraderRecord.optSaPostcode match {
              case Some(postcode) => Json.obj("SAPostcode" -> postcode)
              case _ => Json.obj()
            }

          val eS20Block =
            optES20Response match {
              case Some(eSReponse) => Json.obj("ES20Response" -> eSReponse)
              case _ => Json.obj()
            }

          val authenticatorResponseBlock =
            optAuthenticatorResponse match {
              case Some(authenticatorDetails: IndividualDetails) => Json.obj("authenticatorResponse" -> Json.toJson(authenticatorDetails))
              case Some(authenticatorFailureResponse: String) => Json.obj("authenticatorResponse" -> authenticatorFailureResponse)
              case _ => Json.obj()
            }

          val overseasIdentifiersBlock =
            soleTraderRecord.optOverseas match {
              case Some(overseas) => Json.obj(
                "overseasTaxIdentifier" -> overseas.taxIdentifier,
                "overseasTaxIdentifierCountry" -> overseas.country)
              case _ => Json.obj()
            }

          val businessVerificationStatus: String =
            if (!journeyConfig.businessVerificationCheck) "not requested"
            else {
              soleTraderRecord.businessVerification match {
                case Some(BusinessVerificationPass) => "success"
                case Some(BusinessVerificationFail) => "fail"
                case Some(BusinessVerificationNotEnoughInformationToCallBV) | None => "Not Enough Information to call BV"
                case Some(BusinessVerificationNotEnoughInformationToChallenge) => "Not Enough Information to challenge"
              }
            }

          Json.obj(
            "callingService" -> callingService,
            "businessType" -> "Sole Trader",
            "firstName" -> soleTraderRecord.fullName.firstName,
            "lastName" -> soleTraderRecord.fullName.lastName,
            "dateOfBirth" -> soleTraderRecord.dateOfBirth,
            "isMatch" -> identifiersMatch.toString,
            "VerificationStatus" -> businessVerificationStatus
          ) ++ registrationStatusBlock ++ sautrBlock ++ ninoBlock ++ addressBlock ++ saPostCodeBlock ++ overseasIdentifiersBlock ++ trnBlock ++ eS20Block ++ authenticatorResponseBlock
        case _ =>
          throw new InternalServerException(s"Not enough information to audit sole trader journey for Journey ID $journeyId")
      }
    }

    soleTraderAudit.map {
      soleTraderAuditJson => auditConnector.sendExplicitAudit(auditType = "SoleTraderRegistration", detail = soleTraderAuditJson)
    }
  }

  private def auditIndividualJourney(journeyId: String, journeyConfig: JourneyConfig)
                                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val callingService: String = journeyConfig.pageConfig.optServiceName.getOrElse(appConfig.defaultServiceName)

    val individualAudit: Future[JsObject] = for {
      optIndividualDetails <- soleTraderIdentificationService.retrieveIndividualDetails(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorResponse <-
        (optIndividualDetails, optIdentifiersMatch) match {
          case (Some(_), Some(true)) =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case (Some(individualDetails), Some(_)) if individualDetails.optNino.isEmpty => Future.successful(None)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
    } yield {
      val authenticatorResponseBlock =
        optAuthenticatorResponse match {
          case Some(authenticatorDetails: IndividualDetails) => Json.obj("authenticatorResponse" -> Json.toJson(authenticatorDetails))
          case Some(authenticatorFailureResponse: String) => Json.obj("authenticatorResponse" -> authenticatorFailureResponse)
          case _ => Json.obj()
        }
      (optIndividualDetails, optIdentifiersMatch) match {
        case (Some(IndividualDetails(firstName, lastName, dateOfBirth, Some(nino), None)), Some(identifiersMatch)) =>
          Json.obj(
            "callingService" -> callingService,
            "firstName" -> firstName,
            "lastName" -> lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "isMatch" -> identifiersMatch.toString
          ) ++ authenticatorResponseBlock
        case _ =>
          throw new InternalServerException(s"Not enough information to audit individual journey for Journey ID $journeyId")
      }
    }

    individualAudit.map {
      individualAuditJson => auditConnector.sendExplicitAudit(auditType = "IndividualIdentification", detail = individualAuditJson)
    }
  }

}
