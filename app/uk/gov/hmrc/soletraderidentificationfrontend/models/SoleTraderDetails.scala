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

package uk.gov.hmrc.soletraderidentificationfrontend.models

import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.soletraderidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{SoleTraderDetailsMatchResult, SuccessfulMatchKey}

import java.time.LocalDate

case class SoleTraderDetails(fullName: FullName,
                             dateOfBirth: LocalDate,
                             optNino: Option[String],
                             address: Option[Address],
                             optSaPostcode: Option[String],
                             optSautr: Option[String],
                             identifiersMatch: SoleTraderDetailsMatchResult,
                             businessVerification: Option[BusinessVerificationStatus],
                             registrationStatus: Option[RegistrationStatus],
                             optTrn: Option[String],
                             optOverseasTaxIdentifier: Option[String],
                             optOverseasTaxIdentifierCountry: Option[String],
                             optNinoInsights: Option[JsObject])

object SoleTraderDetails {

  private val FullNameKey = "fullName"
  private val NinoKey = "nino"
  private val AddressKey = "address"
  private val SaPostcodeKey = "saPostcode"
  private val SautrKey = "sautr"
  private val DateOfBirthKey = "dateOfBirth"
  private val IdentifiersMatchKey = "identifiersMatch"
  private val BusinessVerificationKey = "businessVerification"
  private val RegistrationKey = "registration"
  private val TrnKey = "trn"
  private val OverseasIdentifiersKey = "overseas"
  private val BusinessVerificationUnchallengedKey = "UNCHALLENGED"
  private val ReputationKey = "reputation"
  private val CorrelationIdKey = "ninoInsightsCorrelationId"
  private val OverseasTaxIdentifierKey: String = "overseasTaxIdentifiers"
  private val OverseasCountryKey: String = "country"


  implicit val format: OFormat[SoleTraderDetails] = new OFormat[SoleTraderDetails] {
    override def reads(json: JsValue): JsResult[SoleTraderDetails] =
      for {
        fullName <- (json \ FullNameKey).validate[FullName]
        dateOfBirthKey <- (json \ DateOfBirthKey).validate[LocalDate]
        optNino <- (json \ NinoKey).validateOpt[String]
        optAddress <- (json \ AddressKey).validateOpt[Address]
        optSaPostcode <- (json \ SaPostcodeKey).validateOpt[String]
        optSaUtr <- (json \ SautrKey).validateOpt[String]
        identifiersMatch <- (json \ IdentifiersMatchKey).validate[SoleTraderDetailsMatchResult]
        businessVerification <- (json \ BusinessVerificationKey).validateOpt[BusinessVerificationStatus]
        registrationStatus <- (json \ RegistrationKey).validateOpt[RegistrationStatus]
        optTrnKey <- (json \ TrnKey).validateOpt[String]
        optOverseas <- (json \ OverseasIdentifiersKey).validateOpt[Overseas]
        optOverseasTaxId <- (json \ OverseasTaxIdentifierKey).validateOpt[String]
        optOverseasTaxIdCountry <- (json \ OverseasCountryKey).validateOpt[String]
        reputationKey <- (json \ ReputationKey).validateOpt[JsObject]
      } yield {
        val (overseasTaxId, overseasTaxIdCountry) = determineOverseasTaxIdentifierDetails(
          optOverseasTaxId,
          optOverseasTaxIdCountry,
          optOverseas
        )
        SoleTraderDetails(fullName, dateOfBirthKey, optNino, optAddress, optSaPostcode,
          optSaUtr, identifiersMatch, businessVerification, registrationStatus, optTrnKey,
          overseasTaxId, overseasTaxIdCountry, reputationKey)
      }

    override def writes(soleTraderDetails: SoleTraderDetails): JsObject =
      Json.obj(
        FullNameKey -> soleTraderDetails.fullName,
        DateOfBirthKey -> soleTraderDetails.dateOfBirth,
        IdentifiersMatchKey -> soleTraderDetails.identifiersMatch.toString
      ) ++ {
        soleTraderDetails.registrationStatus match {
          case Some(registration) => Json.obj(RegistrationKey -> registration)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.optNino match {
          case Some(nino) => Json.obj(NinoKey -> nino)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.address match {
          case Some(address) => Json.obj(AddressKey -> address)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.optSaPostcode match {
          case Some(postcode) => Json.obj(SaPostcodeKey -> postcode)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.optSautr match {
          case Some(sautr) => Json.obj(SautrKey -> sautr)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.businessVerification match {
          case Some(businessVerification) => Json.obj(BusinessVerificationKey -> businessVerification)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.optTrn match {
          case Some(trn) => Json.obj(TrnKey -> trn)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.optNinoInsights match {
          case Some(reputation) => Json.obj(ReputationKey -> reputation)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.optOverseasTaxIdentifier match {
          case Some(overseasTaxId) => Json.obj(OverseasTaxIdentifierKey -> overseasTaxId)
          case None => Json.obj()
        }
      } ++ {
        soleTraderDetails.optOverseasTaxIdentifierCountry match {
          case Some(overseasTaxIdCountry) => Json.obj(OverseasCountryKey -> overseasTaxIdCountry)
          case None => Json.obj()
        }
      }
  }

  def jsonWriterForCallingServices(soleTraderDetails: SoleTraderDetails): JsObject = {
    val formattedBusinessVerification = soleTraderDetails.businessVerification
      .map(businessVerification => {
        val businessVerificationStatusForCallingServices: String = businessVerification match {
          case BusinessVerificationNotEnoughInformationToCallBV |
               BusinessVerificationNotEnoughInformationToChallenge => BusinessVerificationUnchallengedKey
          case BusinessVerificationPass => BusinessVerificationPassKey
          case BusinessVerificationFail => BusinessVerificationFailKey
          case SaEnrolled => BusinessVerificationSaEnrolledKey
        }
        Json.obj(BusinessVerificationKey -> Json.obj(BusinessVerificationStatusKey -> businessVerificationStatusForCallingServices))
      })
      .getOrElse(Json.obj())

    val formattedIdentifiersMatch = Json.obj(IdentifiersMatchKey -> soleTraderDetails.identifiersMatch.toString.contains(SuccessfulMatchKey))

    val formattedNinoInsights =
      soleTraderDetails.optNinoInsights match {
        case Some(ninoInsights) =>
          val result = ninoInsights - CorrelationIdKey
          Json.obj(ReputationKey -> result)
        case None => Json.obj()
      }

    val overseasTaxIdBlock: JsObject = (soleTraderDetails.optOverseasTaxIdentifier, soleTraderDetails.optOverseasTaxIdentifierCountry) match {
      case (Some(overseasTaxId), Some(country)) => Json.obj(
        "overseas" -> Json.obj(
          "taxIdentifier" -> overseasTaxId,
          "country" -> country
        )
      )
      case (None, None) => Json.obj()
      case _ => throw new InternalServerException("Error: Invalid combination of tax identifier and country")
    }


    format.writes(soleTraderDetails) - OverseasTaxIdentifierKey - OverseasCountryKey ++ formattedBusinessVerification ++ formattedIdentifiersMatch ++ formattedNinoInsights ++ overseasTaxIdBlock
  }

  private def determineOverseasTaxIdentifierDetails(optOverseasTaxId: Option[String],
                                                    optOverseasTaxIdCountry: Option[String],
                                                    optOverseas: Option[Overseas]): (Option[String], Option[String]) =
    (optOverseasTaxId, optOverseasTaxIdCountry) match {
      case (Some(identifier), Some(country)) => (Some(identifier), Some(country))
      case (None, None) => optOverseas match {
        case Some(overseas) => (Some(overseas.taxIdentifier), Some(overseas.country))
        case None => (None, None)
      }
      case _ => throw new InternalServerException("Error: Invalid combination of tax identifier and country")
    }

}
