/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.testFirstName

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val change = "Change"
    val saveAndContinue = "Save and continue"
    val continue = "Continue"
    val getHelp = "Is this page not working properly?"
    val yes = "Yes"
    val no = "No"
    val back = "Back"

    val technicalDifficultiesTitle = "Sorry, we are experiencing technical difficulties - 500"
    val technicalDifficultiesHeading = "Sorry, we’re experiencing technical difficulties"

    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }

  }

  object Header {
    val signOut = "Sign out"
  }

  object BetaBanner {
    val title = "This is a new service – your feedback will help us to improve it."
  }

  object CaptureFullName {
    val title = "What is your full name?"
    val heading = "What is your full name?"
    val line_1 = "We will attempt to match these details with the information we already have."
    val form_field_1 = "First name"
    val form_field_2 = "Last name"

    object Error {
      val noFirstNameEntered = "Enter your first name"
      val noLastNameEntered = "Enter your last name"
      val invalidFirstNameEntered = "First name must be 99 characters or fewer"
      val invalidLastNameEntered = "Last name must be 99 characters or fewer"
    }

  }

  object CaptureDateOfBirth {
    val title = s"What is applicant’s date of birth?"
    val heading = s"What is $testFirstName’s date of birth?"
    val hint = "For example, 27 3 2007"

    object Error {
      val noDobEntered = "Enter your date of birth"
      val invalidDate = "Enter a real date"
      val futureDate = "The date of birth must be in the past"
      val invalidAge = "You must be at least 16 years of age"
    }

  }

  object CaptureNino {
    val title = s"What is applicant’s National Insurance number?"
    val heading = s"What is $testFirstName’s National Insurance number?"
    val line_1 = "It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’."
    val form_field_1 = "It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’."
    val no_nino = "I do not have a National Insurance number"

    object Error {
      val invalidNinoEntered = "Enter a National Insurance number in the correct format"
    }

  }

  object CaptureAddress {
    val title = s"Enter applicant’s home address"
    val heading = s"Enter $testFirstName’s home address"
    val line_1 = "Address line 1"
    val line_2 = "Address line 2"
    val line_3 = "Address line 3 (optional)"
    val line_4 = "Address line 4 (optional)"
    val line_5 = "Address line 5 (optional)"
    val postcode = "Postcode"
    val country = "Country"

    object Error {
      val no_entry_address1 = "Enter the first line of the address"
      val no_entry_address2 = "Enter the second line of the address"
      val too_many_characters_address = "Enter the address using 35 characters or less"
      val invalid_characters_address = "Enter the address without special characters"
      val invalid_characters_postcode = "Enter the postcode without special characters"
      val non_uk_postcode = "Enter a postcode, like AA1 1AA"
      val no_entry_country = "Select a country"
    }

  }

  object CaptureSautr {
    val title = s"What is applicant’s Unique Taxpayer Reference?"
    val heading = s"What is $testFirstName’s Unique Taxpayer Reference?"
    val line_1 = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Self Assessment. It may be called ‘reference’, ‘UTR’ or ‘official use’."
    val line_2 = "I do not have a Unique Taxpayer Reference"
    val details_line_1 = "Your UTR helps us identify your business"
    val details_line_2 = "I cannot find the UTR"
    val details_line_3 = "The business does not have a UTR"

    object Error {
      val invalidSautrEntered = "Enter a Unique Taxpayer Reference in the correct format"
    }

  }

  object CaptureSaPostcode {

    val title = "What is the postcode where the business is registered for Self Assessment?"
    val heading = "What is the postcode where the business is registered for Self Assessment?"
    val hint = "For example, AB1 2YZ"
    val no_postcodeLink = "The business does not have a Self Assessment postcode"

    object Error {
      val invalid_sa_postcode = "Enter a UK postcode"
      val no_entry_sa_postcode = "Enter a postcode"
    }

  }

  object CaptureOverseasTaxIdentifiers {

    val title = "What is the business’s overseas tax identifier?"
    val heading = "What is the business’s overseas tax identifier?"
    val hint = "We may use this number to help us identify your business. The tax identifier could be VAT registration number, Employee Identification Number (EIN) or any other identifier we could use to verify your business details."
    val form_field_1 = "Enter a tax identifier"
    val form_field_2 = "Enter the name of the country that issued the tax identifier"
    val no_identifierLink = "I do not want to provide an identifier"

    object Error {
      val invalid_tax_identifier = "Enter a tax identifier that does not contain special characters"
      val no_entry_tax_identifier = "Enter a tax identifier"
      val invalid_length_tax_identifier = "Enter a tax identifier that is 60 characters or less"
      val no_entry_country = "Enter the name of the country that issued the tax identifier"
    }

  }

  object PersonalInformationError {
    val title = "We could not identify you on our records"
    val heading = "We could not identify you on our records"
    val line_1 = "This could have been because of a mistake when entering your name, date of birth or National Insurance number."
    val button = "Try again"
  }

  object DetailsNotFound {
    val title = "The details you entered did not match our records"
    val heading = "The details you entered did not match our records"
    val line_1 = "We could not match the details you entered with records held by HMRC."
    val line_2 = "If you used the wrong details, you can try again using different details."
    val link_2 = "try again using different details."
    val line_3 = "If you used the correct details, you cannot continue to register using this online service."
    val line_4 = "You need to contact the National Insurance team (opens in a new tab) and tell them there is an issue with your National Insurance number, no matter what tax regime you’re using."
    val link_4 = "contact the National Insurance team (opens in a new tab)"
  }

  object CannotConfirmBusiness {
    val title = "The details you provided do not match records held by HMRC"
    val heading = "The details you provided do not match records held by HMRC"
    val line_1 = "If these details are correct, you can still register. If you entered the wrong details, go back and make changes."
    val radio = "Do you want to continue registering with the details you provided?"

    object Error {
      val no_selection = "Select yes if you want to continue registering with the details you provided"
    }

  }

  object CheckYourAnswers {
    val title = "Check your answers"
    val heading = "Check your answers"
    val firstName = "First name"
    val lastName = "Last name"
    val dob = "Date of birth"
    val nino = "National insurance number"
    val sautr = "Unique taxpayers reference number"
    val address = "Home Address"
    val noSautr = "The business does not have a UTR"
    val noNino = "I do not have a National Insurance number"
    val saPostcode = "Self Assessment postcode"
    val overseasTaxIdentifier = "Overseas tax identifier"

  }

}
