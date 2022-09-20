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
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}

import scala.util.matching.Regex

object CaptureSautrForm {
  val sautrRegex: Regex = "[0-9]{10}".r

  val saUtrNotEntered: Constraint[String] = Constraint("sa-utr.not-entered")(
    saUtr => validate(
      constraint = saUtr.isEmpty,
      errMsg = "sa-utr.not.entered.error"
    )
  )

  val saUtrIncorrectFormat: Constraint[String] = Constraint("sa-utr.incorrect-format")(
    saUtr => validateNot(
      constraint = saUtr.toUpperCase.matches(sautrRegex.regex),
      errMsg = "sa-utr.incorrect.format.error"
    )
  )

  val form: Form[String] =
    Form(
      "sa-utr" -> text.verifying(saUtrNotEntered andThen saUtrIncorrectFormat)
    )
}
