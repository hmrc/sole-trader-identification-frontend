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

package uk.gov.hmrc.soletraderidentificationfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}
import uk.gov.hmrc.soletraderidentificationfrontend.models.Address

import scala.util.matching.Regex

object CaptureAddressForm {

  val addressRegex: Regex = "([A-Za-z0-9]([-'.& ]{0,1}[A-Za-z0-9 ]+)*[A-Za-z0-9]?)$".r
  val postCodeRegex: Regex = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$""".r

  val address1NotEntered: Constraint[String] = Constraint("address1.not-entered")(
    address1 => validate(
      constraint = address1.isEmpty,
      errMsg = "error.no_entry_address1"
    )
  )

  val address2NotEntered: Constraint[String] = Constraint("address2.not-entered")(
    address2 => validate(
      constraint = address2.isEmpty,
      errMsg = "error.no_entry_address2"
    )
  )

  val countryNotEntered: Constraint[String] = Constraint("country.not-entered")(
    country => validate(
      constraint = country.isEmpty,
      errMsg = "error.no_entry_country"
    )
  )

  val postcodeInvalid: Constraint[String] = Constraint("postcode.invalid-format")(
    postcode => validateNot(
      constraint = postcode.toUpperCase matches postCodeRegex.regex,
      errMsg = "error.invalid_characters_postcode"
    )
  )

  val addressInvalid: Constraint[String] = Constraint("address1.invalid-format")(
    address => validateNot(
      constraint = address matches addressRegex.regex,
      errMsg = "error.invalid_characters_address"
    )
  )

  val addressTooManyCharacters: Constraint[String] = Constraint("address.too-many-characters")(
    address => validateNot(
      constraint = address.length < 35,
      errMsg = "error.too_many_characters_address"
    )
  )

  def apply(): Form[Address] = {
    Form(
      mapping(
        "address1" -> optText.toTrimmedText.verifying(address1NotEntered andThen addressInvalid andThen addressTooManyCharacters),
        "address2" -> optText.toTrimmedText.verifying(address2NotEntered andThen addressInvalid andThen addressTooManyCharacters),
        "address3" -> optional(optText.toTrimmedText.verifying(addressInvalid andThen addressTooManyCharacters)),
        "address4" -> optional(optText.toTrimmedText.verifying(addressInvalid andThen addressTooManyCharacters)),
        "address5" -> optional(optText.toTrimmedText.verifying(addressInvalid andThen addressTooManyCharacters)),
        "postcode" -> optional(optText.toTrimmedText.verifying(postcodeInvalid)),
        "country" -> optText.toText.verifying(countryNotEntered)
      )(Address.apply)(Address.unapply)
    )
  }

}
