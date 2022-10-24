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
import play.api.data.validation.Constraint
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil._
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}

import scala.util.matching.Regex

object CaptureNinoForm {
  val ninoRegex: Regex = "^([ACEHJLMOPRSWXY][A-CEGHJ-NPR-TW-Z]|B[A-CEHJ-NPR-TW-Z]|G[ACEGHJ-NPR-TW-Z]|[KT][A-CEGHJ-MPR-TW-Z]|N[A-CEGHJL-NPR-SW-Z]|Z[A-CEGHJ-NPR-TW-Y]) ?\\d{2} ?\\d{2} ?\\d{2} ?[A-D]{1}$".r

  val ninoNotEntered: Constraint[String] = Constraint("nino.not-entered")(
    nino => validate(
      constraint = nino.isEmpty,
      errMsg = "enter-nino.not.entered.error"
    )
  )

  val ninoIncorrectFormat: Constraint[String] = Constraint("nino.incorrect-format")(
    nino => validateNot(
      constraint = nino.toUpperCase.matches(ninoRegex.regex),
      errMsg = "enter-nino.invalid.format.error"
    )
  )

  val form: Form[String] =
    Form(
      "nino" -> optText.toTrimmedText.verifying(ninoNotEntered andThen ninoIncorrectFormat)
    )
}
