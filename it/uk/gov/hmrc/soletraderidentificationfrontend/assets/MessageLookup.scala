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

package uk.gov.hmrc.soletraderidentificationfrontend.assets

import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.testFirstName

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val change = "Change"
    val saveAndContinue = "Save and continue"
    val continue = "Continue"
    val getHelp = "Is this page not working properly? (opens in new tab)"
    val yes = "Yes"
    val no = "No"
    val back = "Back"
    val try_again = "Try again"

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
    val title = "What is your full name? - Entity Validation Service - GOV.UK"
    val testFullNamePageTitle = "What is the name of the nominated partner? - Entity Validation Service - GOV.UK"
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
    val title = s"What is applicant’s date of birth? - Entity Validation Service - GOV.UK"
    val heading = s"What is $testFirstName’s date of birth?"
    val hint = "For example, 27 3 2007"

    object Error {
      val noDobEntered = "Enter the date of birth"
      val invalidDate = "Enter the date of birth in the correct format"
      val futureDate = "The date of birth must be in the past"
      val invalidAge = "You must be at least 16 years of age"
      val missingDay = "The date of birth must include a day"
      val missingMonth = "The date of birth must include a month"
      val missingYear = "The date of birth must include a year"
      val missingDayAndMonth = "The date of birth must include a day and a month"
      val missingDayAndYear = "The date of birth must include a day and a year"
      val missingMonthAndYear = "The date of birth must include a month and a year"
    }

  }

  object CaptureNino {
    val title = s"What is applicant’s National Insurance number? - Entity Validation Service - GOV.UK"
    val heading = s"What is $testFirstName’s National Insurance number?"
    val line_1 = "It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’."
    val form_field_1 = "It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’."
    val no_nino = "I do not have a National Insurance number"

    object Error {
      val invalidNinoEntered = "Enter a National Insurance number in the correct format"
    }

  }

  object CaptureAddress {
    val title = s"Enter applicant’s home address - Entity Validation Service - GOV.UK"
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
      val too_many_characters_address = "The address must be 35 characters or fewer"
      val invalid_characters_address = "The address must only include letters a to z, numbers and special characters such as hyphens, speech marks and full stops"
      val invalid_characters_postcode = "The postcode must not include special characters"
      val non_uk_postcode = "Enter a postcode, like AA1 1AA"
      val no_entry_country = "Select a country"
      val no_postcode_GB = "Enter your postcode"
    }

  }

  object CaptureSautr {
    val title = "What is applicant’s Unique Taxpayer Reference? - Entity Validation Service - GOV.UK"
    val heading = s"What is $testFirstName’s Unique Taxpayer Reference?"
    val line_1 = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Self Assessment. It may be called ‘reference’, ‘UTR’ or ‘official use’."
    val line_2 = "I do not have a Unique Taxpayer Reference"
    val details_line_1 = "Your UTR helps us identify your business"
    val details_line_2 = "I cannot find the UTR"
    val details_line_3 = "The business does not have a UTR"

    val new_title = "Does the applicant have a Unique Taxpayer Reference? - Entity Validation Service - GOV.UK"
    val new_heading = s"Does $testFirstName have a Unique Taxpayer Reference?"
    val new_line_1 = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Self Assessment. It may be called ‘reference’, ‘UTR’ or ‘official use’. Your UTR helps us identify your business."
    val new_label = "Unique Taxpayer Reference"

    object Error {
      val invalidSautrEntered = "Enter the Unique Taxpayer Reference in the correct format"
      val new_no_selection = "Select yes if there is a Unique Taxpayer Reference"
    }

  }

  object CaptureSaPostcode {

    val title = "What is the postcode used to register the business for Self Assessment? - Test Service - GOV.UK"
    val heading = "What is the postcode used to register the business for Self Assessment?"
    val hint = "For example, AB1 2YZ"
    val no_postcodeLink = "The business does not have a Self Assessment postcode"

    object Error {
      val invalid_sa_postcode = "Enter the postcode in the correct format, for example, AB1 2YZ"
      val no_entry_sa_postcode = "Enter the postcode where the business is registered for Self Assessment"
    }

  }

  object CaptureOverseasTaxIdentifiers {

    val title = "What is the business’s overseas tax identifier? - Entity Validation Service - GOV.UK"
    val heading = "What is the business’s overseas tax identifier?"
    val hint = "We may use this number to help us identify your business. The tax identifier could be a VAT registration number, Employee Identification Number (EIN) or any other identifier we could use to verify your business details."
    val form_field_1 = "Enter a tax identifier"
    val form_field_2 = "Enter the name of the country that issued the tax identifier"
    val no_identifierLink = "I do not want to provide an identifier"

    object Error {
      val invalid_tax_identifier = "Enter a tax identifier that does not contain special characters"
      val no_entry_tax_identifier = "Enter a tax identifier"
      val invalid_length_tax_identifier = "The tax identifier must be 60 characters or fewer"
      val no_entry_country = "Enter the name of the country that issued the tax identifier"
    }

  }

  object CaptureOverseasTaxIdentifier {

    val title = "Does the business have an overseas tax identifier?"
    val hint = "We may use this number to help us identify your business. The tax identifier could be a VAT registration number, Employee Identification Number (EIN) or any other identifier we could use to verify your business details."

    object Error {
      val no_tax_identifier_selection = "Select yes if the business has an overseas tax identifier"
      val no_entry_tax_identifier = "Enter the overseas tax identifier"
      val invalid_tax_identifier = "Enter a tax identifier that does not contain special characters"
      val invalid_length_tax_identifier = "The overseas tax identifier must be 60 characters or fewer"
    }
  }

  object CaptureOverseasTaxIdentifiersCountry {
    val title = "Which country issued the overseas tax identifier? - Entity Validation Service - GOV.UK"
    val heading = "Which country issued the overseas tax identifier?"

    object Error {
      val no_entry_country = "Enter the name of the country that issued the overseas tax identifier"
    }
  }

  object PersonalInformationError {
    val title = "We could not identify you on our records"
    val heading = "We could not identify you on our records"
    val line_1 = "This could have been because of a mistake when entering your name, date of birth or National Insurance number."
    val button = "Try again"
  }

  object DetailsNotFound {
    val title = "The details you entered did not match our records - Entity Validation Service - GOV.UK"
    val heading = "The details you entered did not match our records"
    val line_1 = "We could not match the details you entered with records held by HMRC."
    val line_2 = "If you used the wrong details, you can try again using different details."
    val link_2 = "try again using different details."
    val line_3 = "If you used the correct details, you cannot continue to register using this online service."
    val line_4 = "You need to contact the National Insurance team (opens in a new tab) and tell them there is an issue with your National Insurance number, no matter what tax regime you’re using."
    val link_4 = "contact the National Insurance team (opens in a new tab)"
  }

  object DetailsDidNotMatch {
    val title = "The details you entered did not match our records - Entity Validation Service - GOV.UK"
    val heading = "The details you entered did not match our records"
    val line_1 = "We could not match the details you entered with records held by HMRC."
    val line_2 = "You cannot continue to register using this online service."
    val button = "Sign out"
  }

  object CannotConfirmBusiness {
    val title = "The details you provided do not match records held by HMRC - Test Service - GOV.UK"
    val heading = "The details you provided do not match records held by HMRC"
    val line_1 = "If these details are correct, you can still register. If you entered the wrong details, go back and make changes."
    val radio = "Do you want to continue registering with the details you provided?"

    object Error {
      val no_selection = "Select yes if you want to continue registering with the details you provided"
    }

  }

  object CouldNotConfirmBusiness {
    val title = "We could not confirm your business - Entity Validation Service - GOV.UK"
    val heading = "We could not confirm your business"
    val line_1 = "The information you provided does not match the details we have about your business."
  }

  object CheckYourAnswers {
    val title = "Check your answers - Entity Validation Service - GOV.UK"
    val heading = "Check your answers"
    val firstName = "First name"
    val lastName = "Last name"
    val dob = "Date of birth"
    val nino = "National Insurance number"
    val sautr = "Unique Taxpayer Reference (UTR)"
    val address = "Home Address"
    val noSautr = "There is no UTR"
    val noNino = "There is no National Insurance number"
    val saPostcode = "Self Assessment postcode"
    val overseasTaxIdentifier = "Overseas tax identifier"
    val overseasTaxIdentifierCountry = "Country of overseas tax identifier"

  }

}
