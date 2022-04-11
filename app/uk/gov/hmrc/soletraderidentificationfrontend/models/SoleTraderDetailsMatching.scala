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

object SoleTraderDetailsMatching {

  sealed trait SoleTraderDetailsMatchResult

  type AuthenticatorResponse = Either[SoleTraderDetailsMatchFailure, IndividualDetails]

  sealed trait SoleTraderDetailsMatchFailure extends SoleTraderDetailsMatchResult

  case object SuccessfulMatch extends SoleTraderDetailsMatchResult

  case object NotEnoughInformationToMatch extends SoleTraderDetailsMatchResult

  case object DetailsMismatch extends SoleTraderDetailsMatchFailure

  case object NinoNotDeclaredButFound extends SoleTraderDetailsMatchFailure

  case object NinoNotFound extends SoleTraderDetailsMatchFailure

  case object DeceasedCitizensDetails extends SoleTraderDetailsMatchFailure

  val KnownFactsMatchingResultKey = "identifiersMatch"
  val SuccessfulMatchKey = "SuccessfulMatch"
  val NotEnoughInfoToMatchKey = "NotEnoughInformationToMatch"
  val DetailsMismatchKey = "DetailsMismatch"
  val NinoNotDeclaredButFoundKey = "NinoNotDeclaredButFound"
  val NinoNotFoundKey = "NinoNotFound"
  val DeceasedCitizensDetailsKey = "DeceasedCitizensDetails"

  implicit val format: Format[SoleTraderDetailsMatchResult] = new Format[SoleTraderDetailsMatchResult] {
    override def writes(soleTraderMatchingResult: SoleTraderDetailsMatchResult): JsValue = {
      val knownFactsMatchingResultString = soleTraderMatchingResult match {
        case SuccessfulMatch => SuccessfulMatchKey
        case NotEnoughInformationToMatch => NotEnoughInfoToMatchKey
        case DetailsMismatch => DetailsMismatchKey
        case NinoNotDeclaredButFound => NinoNotDeclaredButFoundKey
        case NinoNotFound => NinoNotFoundKey
        case DeceasedCitizensDetails => DeceasedCitizensDetailsKey
      }

      JsString(knownFactsMatchingResultString)
    }

    override def reads(json: JsValue): JsResult[SoleTraderDetailsMatchResult] =
    //      json.validate[String].collect(JsonValidationError("Invalid Sole Trader Matching Result")) {
    //        case SuccessfulMatchKey => SuccessfulMatch
    //        case NotEnoughInfoToMatchKey => NotEnoughInformationToMatch
    //        case DetailsMismatchKey => DetailsMismatch
    //        case NinoNotDeclaredButFoundKey => NinoNotDeclaredButFound
    //        case NinoNotFoundKey => NinoNotFound
    //        case DeceasedCitizensDetailsKey => DeceasedCitizensDetails
    //      }
      json.validate[String] match {
        case JsSuccess(identifiersMatchString, _) => identifiersMatchString match {
          case SuccessfulMatchKey => JsSuccess(SuccessfulMatch)
          case NotEnoughInfoToMatchKey => JsSuccess(NotEnoughInformationToMatch)
          case DetailsMismatchKey => JsSuccess(DetailsMismatch)
          case NinoNotDeclaredButFoundKey => JsSuccess(NinoNotDeclaredButFound)
          case NinoNotFoundKey => JsSuccess(NinoNotFound)
          case DeceasedCitizensDetailsKey => JsSuccess(DeceasedCitizensDetails)
        }
        case JsError(_) =>
          json.validate[Boolean] match {
            case JsSuccess(identifiersMatchBoolean, _) =>
              if (identifiersMatchBoolean) JsSuccess(SuccessfulMatch)
              else JsSuccess(DetailsMismatch)
            case JsError(_) => throw new InternalServerException("Invalid Sole Trader Matching Result")
          }
      }
  }
}
