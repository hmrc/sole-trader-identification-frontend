/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OFormat, OWrites, Reads}

case class KnownFactsResponse(postcode: Option[String], isAbroad: Option[Boolean], nino: Option[String])

object KnownFactsResponse {

  private val PostcodeKey = "postcode"
  private val IsAbroadKey = "isAbroad"
  private val NinoKey = "nino"

  implicit val reads: Reads[KnownFactsResponse] = (
    (JsPath \ PostcodeKey).readNullable[String] and
      (JsPath \ IsAbroadKey).readNullable[Boolean] and
      (JsPath \ NinoKey).readNullable[String]
  )(KnownFactsResponse.apply _)

  implicit val writes: OWrites[KnownFactsResponse] = (
    (JsPath \ PostcodeKey).writeNullable[String] and
      (JsPath \ IsAbroadKey).writeNullable[Boolean] and
      (JsPath \ NinoKey).writeNullable[String]
  )(unlift(KnownFactsResponse.unapply))

  val format: OFormat[KnownFactsResponse] = OFormat(reads, writes)

}

sealed trait KnownFactsResponseError

case object KnownFactsNoContentError extends KnownFactsResponseError
