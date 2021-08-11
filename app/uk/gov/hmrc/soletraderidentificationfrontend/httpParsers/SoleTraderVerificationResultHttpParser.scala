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

package uk.gov.hmrc.soletraderidentificationfrontend.httpParsers

import play.api.http.Status.{FAILED_DEPENDENCY, OK, UNAUTHORIZED}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsPath, JsSuccess, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.models.IndividualDetails
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching._

import java.time.LocalDate

object SoleTraderVerificationResultHttpParser {
  private val notFoundError: String = "CID returned no record"
  private val FirstNameKey = "firstName"
  private val LastNameKey = "lastName"
  private val NinoKey = "nino"
  private val SautrKey = "saUtr"
  private val DateOfBirthKey = "dateOfBirth"

  private val authenticatorReads: Reads[IndividualDetails] = (
    (JsPath \ FirstNameKey).read[String] and
      (JsPath \ LastNameKey).read[String] and
      (JsPath \ DateOfBirthKey).read[LocalDate] and
      (JsPath \ NinoKey).read[String] and
      (JsPath \ SautrKey).readNullable[String]
    ) (IndividualDetails.apply _)

  implicit object SoleTraderVerificationResultReads extends HttpReads[AuthenticatorResponse] {
    override def read(method: String, url: String, response: HttpResponse): AuthenticatorResponse =
      response.status match {
        case OK =>
          response.json.validate[IndividualDetails](authenticatorReads) match {
            case JsSuccess(details, _) =>
              Right(details)
            case JsError(errors) =>
              throw new InternalServerException(s"Invalid JSON returned from authenticator. Errors - $errors")
          }
        case FAILED_DEPENDENCY => Left(DeceasedCitizensDetails)
        case UNAUTHORIZED =>
          val errors = (response.json \ "errors").toString

          if (errors.contains(notFoundError)) {
            Left(NinoNotFound)
          } else {
            Left(DetailsMismatch)
          }
        case status =>
          throw new InternalServerException(s"Invalid status received from authenticator#match API: $status")
      }
  }

}
