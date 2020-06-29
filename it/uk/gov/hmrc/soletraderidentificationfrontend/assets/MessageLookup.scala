/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.assets

object MessageLookup {

  val suffix = " - Use software to send Income Tax updates - GOV.UK"

  object Base {
    val continue = "Continue"
    val yes = "Yes"
    val no = "No"
    val acceptAndContinue = "Accept and continue"
  }

  object PersonalDetails {
    val title = "Enter your details"
    val heading = "Who do you want to register for VAT?"
    val line_1 = "We will attempt to match these details with the information we already have."
    val form_field_1 = "First name"
    val form_field_2 = "Last name"
    val form_field_3 = "Date of birth"
    val form_field_3_hint = "For example, 27 3 2007"
  }

}
