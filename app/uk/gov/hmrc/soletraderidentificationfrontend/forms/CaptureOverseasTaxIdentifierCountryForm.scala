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

package uk.gov.hmrc.soletraderidentificationfrontend.forms

import play.api.data.Form
import play.api.data.validation.Constraint
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.validate

object CaptureOverseasTaxIdentifierCountryForm {
  private val countryNotEntered: Constraint[String] = Constraint("country.not-entered")(country =>
    validate(
      constraint = country.isEmpty,
      errMsg     = "error.tax_identifier_country"
    )
  )

  val form: Form[String] = {
    Form("country" -> optText.toText.verifying(countryNotEntered))
  }

}
