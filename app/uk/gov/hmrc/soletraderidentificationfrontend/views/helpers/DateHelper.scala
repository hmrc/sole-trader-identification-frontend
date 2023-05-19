/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.views.helpers

import play.api.data.Form

import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureDateOfBirthForm._

object DateHelper {

  private val missingDayErrorMessages: Seq[String] = Seq(missingDayErrorMsg, missingDayAndMonthErrorMsg, missingDayAndYearErrorMsg)
  private val missingMonthErrorMessages: Seq[String] = Seq(missingMonthErrorMsg, missingDayAndMonthErrorMsg, missingMonthAndYearErrorMsg)
  private val missingYearErrorMessages: Seq[String] = Seq(missingYearErrorMsg, missingDayAndYearErrorMsg, missingMonthAndYearErrorMsg)

  private val wholeDateErrorMessages: Seq[String] =
    Seq(missingDateErrorMsg, invalidDateErrorMsg, futureDateErrorMsg, invalidAgeErrorKey, notRealDateErrorMsg)

  def addDayErrorClass(form: Form[_]): String = addErrorClass(missingDayErrorMessages, form)

  def addMonthErrorClass(form: Form[_]): String = addErrorClass(missingMonthErrorMessages, form)

  def addYearErrorClass(form: Form[_]): String = addErrorClass(missingYearErrorMessages, form)

  private def addErrorClass(fieldErrorMsgs: Seq[String], form: Form[_]): String = {

    if (form.hasErrors) {

      if (searchFormErrors(wholeDateErrorMessages, form) || searchFormErrors(fieldErrorMsgs, form)) " govuk-input--error" else ""

    } else ""

  }

  private def searchFormErrors(expectedErrorMsgs: Seq[String], form: Form[_]): Boolean = {

    val formErrorMessages: Seq[String] = form.errors.flatMap(_.messages)

    formErrorMessages.exists(expectedErrorMsgs.contains(_))
  }

}
