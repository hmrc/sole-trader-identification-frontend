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

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.soletraderidentificationfrontend.models.Address.sanitisedPostcode

case class Address(line1: String,
                   line2: String,
                   line3: Option[String],
                   line4: Option[String],
                   line5: Option[String],
                   postcode: Option[String],
                   countryCode: String
                  ) {

  lazy val withSanitisedPostcode: Address = copy(postcode = postcode.map(sanitisedPostcode))

}

object Address {

  def sanitisedPostcode(postcode: String): String = postcode.toUpperCase.filterNot(_.isWhitespace) match {
    case standardPostcodeFormat(outwardCode, inwardCode) => outwardCode + " " + inwardCode
    case bfpoFormat(outwardCode, inwardCode)             => outwardCode + " " + inwardCode
    case other => throw new InternalServerException(s"Invalid postcode format: $other") // should never happen as it is validated in the form
  }

  private[models] val standardPostcodeFormat = "([A-Z]{1,2}[0-9][0-9A-Z]?)([0-9][A-Z]{2})".r
  private[models] val bfpoFormat = "(BFPO)([0-9]{1,3})".r

  implicit val format: OFormat[Address] = Json.format[Address]

}
