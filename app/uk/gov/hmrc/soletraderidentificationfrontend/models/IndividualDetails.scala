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

package uk.gov.hmrc.soletraderidentificationfrontend.models

import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.{JsPath, OFormat, OWrites, Reads}

import java.time.LocalDate

case class IndividualDetails(firstName: String, lastName: String, dateOfBirth: LocalDate, optNino: Option[String], optSautr: Option[String])

object IndividualDetails {

  private val FirstNameKey = "firstName"
  private val LastNameKey = "lastName"
  private val NinoKey = "nino"
  private val SautrKey = "sautr"
  private val DateOfBirthKey = "dateOfBirth"

  implicit val reads: Reads[IndividualDetails] = (
    (JsPath \ FirstNameKey).read[String] and
      (JsPath \ LastNameKey).read[String] and
      (JsPath \ DateOfBirthKey).read[LocalDate] and
      (JsPath \ NinoKey).readNullable[String] and
      (JsPath \ SautrKey).readNullable[String]
  )(IndividualDetails.apply _)

  implicit val writes: OWrites[IndividualDetails] = (
    (JsPath \ FirstNameKey).write[String] and
      (JsPath \ LastNameKey).write[String] and
      (JsPath \ DateOfBirthKey).write[LocalDate] and
      (JsPath \ NinoKey).writeNullable[String] and
      (JsPath \ SautrKey).writeNullable[String]
  )(unlift(IndividualDetails.unapply))

  val format: OFormat[IndividualDetails] = OFormat(reads, writes)

}
