/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, JsString, Json}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{NotEnoughInformationToMatch, SuccessfulMatch}
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
          case Some(SuccessfulMatch) =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
    } yield {
      (optSoleTraderRecord, optES20Response, optIdentifiersMatch, optAuthenticatorResponse) match {
        case (Some(soleTraderRecord), optES20Response, Some(identifiersMatch), optAuthenticatorResponse) =>

          val callingService: String = journeyConfig.pageConfig.labels
            .flatMap(_.optEnglishServiceName)
            .getOrElse(journeyConfig.pageConfig.optServiceName
              .getOrElse(appConfig.defaultServiceName)
            )

          val registrationStatusBlock =
            soleTraderRecord.registrationStatus match {
              case Some(registrationStatus) => registrationStatus match {
                case Registered(_) => Json.obj("RegisterApiStatus" -> "success")
                case RegistrationFailed(_) => Json.obj("RegisterApiStatus" -> "fail")
                case RegistrationNotCalled => Json.obj("RegisterApiStatus" -> "not called")
              }
              case _ => Json.obj()
            }

          val sautrBlock =
            soleTraderRecord.optSautr match {
              case Some(sautr) => Json.obj("userSAUTR" -> sautr)
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

          val overseasIdentifiersBlock: JsObject = (soleTraderRecord.optOverseasTaxIdentifier, soleTraderRecord.optOverseasTaxIdentifierCountry) match {
            case (Some(taxIdentifier), Some(country)) => Json.obj(
              "overseasTaxIdentifier" -> taxIdentifier,
              "overseasTaxIdentifierCountry" -> country
            )
            case (None, None) => Json.obj()
            case _ => throw new InternalServerException("Error: Invalid tax identifier and country")
          }

          val businessVerificationStatus: String =
            if (!journeyConfig.businessVerificationCheck) "not requested"
            else {
              soleTraderRecord.businessVerification match {
                case Some(BusinessVerificationPass) => "success"
                case Some(BusinessVerificationFail) => "fail"
                case Some(BusinessVerificationNotEnoughInformationToCallBV) | None => "Not Enough Information to call BV"
                case Some(BusinessVerificationNotEnoughInformationToChallenge) => "Not Enough Information to challenge"
                case Some(SaEnrolled) => "Enrolled"
              }
            }

          val identifiersMatchStatus: String = identifiersMatch match {
            case SuccessfulMatch => "true"
            case NotEnoughInformationToMatch => "unmatchable"
            case _ => "false"
          }

          val reputationBlock = soleTraderRecord.optNinoInsights match {
            case Some(insights) => Json.obj("ninoReputation" -> insights)
            case None => Json.obj()
          }

          Json.obj(
            "callingService" -> JsString(callingService),
            "businessType" -> "Sole Trader",
            "firstName" -> soleTraderRecord.fullName.firstName,
            "lastName" -> soleTraderRecord.fullName.lastName,
            "dateOfBirth" -> soleTraderRecord.dateOfBirth,
            "isMatch" -> identifiersMatchStatus,
            "VerificationStatus" -> businessVerificationStatus
          ) ++ registrationStatusBlock ++
            sautrBlock ++
            ninoBlock(soleTraderRecord.optNino) ++
            addressBlock ++
            saPostCodeBlock ++
            overseasIdentifiersBlock ++
            trnBlock ++
            eS20Block ++
            authenticatorResponseBlock ++
            reputationBlock
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

    val callingService: String = journeyConfig.pageConfig.labels
      .flatMap(_.optEnglishServiceName)
      .getOrElse(journeyConfig.pageConfig.optServiceName
        .getOrElse(appConfig.defaultServiceName)
      )

    val individualAudit: Future[JsObject] = for {
      optIndividualDetails <- soleTraderIdentificationService.retrieveIndividualDetails(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorResponse <-
        (optIndividualDetails, optIdentifiersMatch) match {
          case (Some(_), Some(SuccessfulMatch)) =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case (Some(individualDetails), Some(_)) if individualDetails.optNino.isEmpty => Future.successful(None)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
      optNinoInsights <- soleTraderIdentificationService.retrieveInsights(journeyId)
    } yield {
      val authenticatorResponseBlock =
        optAuthenticatorResponse match {
          case Some(authenticatorDetails: IndividualDetails) => Json.obj("authenticatorResponse" -> Json.toJson(authenticatorDetails))
          case Some(authenticatorFailureResponse: String) => Json.obj("authenticatorResponse" -> authenticatorFailureResponse)
          case _ => Json.obj()
        }
      val identifiersMatchStatus: String = optIdentifiersMatch match {
        case Some(SuccessfulMatch) => "true"
        case Some(NotEnoughInformationToMatch) => "unmatchable"
        case _ => "false"
      }

      val reputationBlock = optNinoInsights match {
        case Some(insights) => Json.obj("ninoReputation" -> insights)
        case None => Json.obj()
      }

      optIndividualDetails match {
        case Some(IndividualDetails(firstName, lastName, dateOfBirth, optNino, None)) =>
          Json.obj(
            "callingService" -> JsString(callingService),
            "firstName" -> firstName,
            "lastName" -> lastName,
            "dateOfBirth" -> dateOfBirth,
            "isMatch" -> identifiersMatchStatus
          ) ++ authenticatorResponseBlock ++
            ninoBlock(optNino) ++
            reputationBlock
        case _ =>
          throw new InternalServerException(s"Not enough information to audit individual journey for Journey ID $journeyId")
      }
    }

    individualAudit.map {
      individualAuditJson => auditConnector.sendExplicitAudit(auditType = "IndividualIdentification", detail = individualAuditJson)
    }
  }

  private def ninoBlock(optNino: Option[String]): JsObject = optNino match {
    case Some(nino) => Json.obj("nino" -> nino)
    case _ => Json.obj()
  }

}
