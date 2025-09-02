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

package views.helpers

import play.api.data.{Form, FormError}

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureDateOfBirthForm._
import uk.gov.hmrc.soletraderidentificationfrontend.views.helpers.DateHelper

class DateHelperSpec extends AnyWordSpec with Matchers {

  val form: Form[_] = captureDateOfBirthForm()

  "DateHelper" should {

    "define an empty class definition for the input components when a form has no errors" in {

      DateHelper.addDayErrorClass(form) mustBe ""

      DateHelper.addMonthErrorClass(form) mustBe ""

      DateHelper.addYearErrorClass(form) mustBe ""
    }

    "define an error class for each of the input components when all of the date components are missing" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(missingDateErrorMsg))))

      DateHelper.addDayErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"
    }

    "define an error class for each of the input components when the date is invalid" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(invalidDateErrorMsg))))

      DateHelper.addDayErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"
    }

    "define an error class for each of the input components if the date is in the future" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(futureDateErrorMsg))))

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"
    }

    "define an error class for each of the input components if the date of birth makes the user less than 16" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(invalidAgeErrorKey))))

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"
    }

    "define an error class for the day input component if the day part of the date of birth is missing" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(missingDayErrorMsg))))

      DateHelper.addDayErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addMonthErrorClass(formWithErrors) mustBe ""

      DateHelper.addYearErrorClass(formWithErrors) mustBe ""
    }

    "define an error class for the month input component if the month part of the date of birth is missing" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(missingMonthErrorMsg))))

      DateHelper.addDayErrorClass(formWithErrors) mustBe ""

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addYearErrorClass(formWithErrors) mustBe ""
    }

    "define an error class for the year input component if the year part of the date of birth is missing" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(missingYearErrorMsg))))

      DateHelper.addDayErrorClass(formWithErrors) mustBe ""

      DateHelper.addMonthErrorClass(formWithErrors) mustBe ""

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"
    }

    "define an error class for the day and month input components if the day and month parts of the date of birth are missing" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, Seq(missingDayAndMonthErrorMsg))))

      DateHelper.addDayErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addYearErrorClass(formWithErrors) mustBe ""
    }

    "define an error class for the day and year input components if the day and year parts of the date of birth are missing" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, missingDayAndYearErrorMsg)))

      DateHelper.addDayErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addMonthErrorClass(formWithErrors) mustBe ""

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"
    }

    "define an error class for the month and year input components if the month and year parts of the date of birth are missing" in {

      val formWithErrors: Form[_] = createFormWithErrors(Seq(FormError(dateKey, missingMonthAndYearErrorMsg)))

      DateHelper.addDayErrorClass(formWithErrors) mustBe ""

      DateHelper.addMonthErrorClass(formWithErrors) mustBe " govuk-input--error"

      DateHelper.addYearErrorClass(formWithErrors) mustBe " govuk-input--error"
    }

  }

  private def createFormWithErrors(errors: Seq[FormError]): Form[_] = Form(form.mapping, Map("key" -> "value"), errors, None)
}
