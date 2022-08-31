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
import play.api.data.Forms.mapping
import play.api.data.validation.Constraint
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullName

class CaptureFullNameForm {

  def validName(text: String): Boolean = text.matches(nameRegex)

  val nameRegex: String = "^(?=.{1,99}$)([A-Z]([-'. ]{0,1}[A-Za-z ]+)*[A-Za-z]?)$"

  private val firstNameNotEntered: Constraint[String] = Constraint("first_name.not_entered")(
    firstName => validate(
      constraint = firstName.isEmpty,
      errMsg = "error.no_entry_first_name"
    )
  )

  private val firstNameInvalid: Constraint[String] = Constraint("first_name.invalid")(
    firstName => validateNot(
      constraint = validName(firstName.capitalize),
      errMsg = "error.invalid_first_name"
    )
  )

  private val lastNameNotEntered: Constraint[String] = Constraint("last_name.not_entered")(
    lastName => validate(
      constraint = lastName.isEmpty,
      errMsg = "error.no_entry_last_name"
    )
  )

  private val lastNameInvalid: Constraint[String] = Constraint("last_name.invalid")(
    lastName => validateNot(
      constraint = validName(lastName.capitalize),
      errMsg = "error.invalid_last_name"
    )
  )

  def apply(): Form[FullName] = {
    Form(
      mapping(
        "first-name" -> optText.toTrimmedText.verifying(firstNameNotEntered andThen firstNameInvalid),
        "last-name" -> optText.toTrimmedText.verifying(lastNameNotEntered andThen lastNameInvalid)
      )(FullName.apply)(FullName.unapply)
    )
  }

}
