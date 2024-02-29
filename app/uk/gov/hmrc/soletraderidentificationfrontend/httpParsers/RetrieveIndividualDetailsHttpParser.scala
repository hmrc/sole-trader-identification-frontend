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

package uk.gov.hmrc.soletraderidentificationfrontend.httpParsers

import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsPath, JsSuccess, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.models.IndividualDetails

import java.time.LocalDate

object RetrieveIndividualDetailsHttpParser {

  implicit object RetrieveIndividualDetailsHttpReads extends HttpReads[Option[IndividualDetails]] {
    override def read(method: String, url: String, response: HttpResponse): Option[IndividualDetails] = {
      response.status match {
        case OK =>
          response.json.validate[IndividualDetails](individualDetailsReads) match {
            case JsSuccess(individualDetails, _) => Some(individualDetails)
            case JsError(errors) =>
              throw new InternalServerException(s"`Failed to read Individual Details with the following error/s: $errors")
          }
        case NOT_FOUND =>
          None
        case status =>
          throw new InternalServerException(s"Unexpected status from Individual Details retrieval. Status returned - $status")
      }
    }
  }

  private val FullNameKey = "fullName"
  private val FirstNameKey = "firstName"
  private val LastNameKey = "lastName"
  private val NinoKey = "nino"
  private val SautrKey = "sautr"
  private val DateOfBirthKey = "dateOfBirth"

  val individualDetailsReads: Reads[IndividualDetails] = (
    (JsPath \ FullNameKey \ FirstNameKey).read[String] and
      (JsPath \ FullNameKey \ LastNameKey).read[String] and
      (JsPath \ DateOfBirthKey).read[LocalDate] and
      (JsPath \ NinoKey).readNullable[String] and
      (JsPath \ SautrKey).readNullable[String]
  )(IndividualDetails.apply _)

}
