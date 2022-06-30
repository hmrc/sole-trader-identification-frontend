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

import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.TimeMachine

import java.time.LocalDate
import scala.util.Try

object CaptureDateOfBirthForm {

  val dateKey = "date-of-birth"

  val missingDayErrorMsg = "error.no_day_entered_dob"
  val missingMonthErrorMsg = "error.no_month_entered_dob"
  val missingYearErrorMsg = "error.no_year_entered_dob"
  val missingDayAndMonthErrorMsg = "error.neither_day_nor_month_entered"
  val missingDayAndYearErrorMsg = "error.neither_day_nor_year_entered"
  val missingMonthAndYearErrorMsg = "error.neither_month_nor_year_entered"

  val missingDateErrorMsg = "error.no_entry_dob"
  val invalidDateErrorMsg = "error.invalid_date"
  val futureDateErrorMsg = "error.invalid_dob_future"
  val invalidAgeErrorKey = "error.invalid_age"

  private val dayKey = "date-of-birth-day"
  private val monthKey = "date-of-birth-month"
  private val yearKey = "date-of-birth-year"

  private val localDateFormatter: Formatter[LocalDate] = new Formatter[LocalDate] {

    val timeMachine: TimeMachine = new TimeMachine()

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

      val dayExists = data.getOrElse(dayKey, "").nonEmpty
      val monthExists = data.getOrElse(monthKey, "").nonEmpty
      val yearExists = data.getOrElse(yearKey, "").nonEmpty

      val dateExists = dayExists && monthExists && yearExists

      if (dateExists) determineDate(data) else manageMissingData(dayExists, monthExists, yearExists)
    }

    override def unbind(key: String, value: LocalDate): Map[String, String] = Map(
      dayKey -> value.getDayOfMonth.toString,
      monthKey -> value.getMonth.toString,
      yearKey -> value.getYear.toString
    )

    private def determineDate(data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

      val inputDate = Try(
        for {
          day <- data.get(dayKey).map(Integer.parseInt)
          month <- data.get(monthKey).map(Integer.parseInt)
          year <- data.get(yearKey).map(Integer.parseInt).filter(_ > 1900)
        } yield LocalDate.of(year, month, day)
      ).getOrElse(None)

      inputDate match {
        case None => Left(Seq(FormError(dayKey, invalidDateErrorMsg)))
        case Some(date) if date.isAfter(timeMachine.now()) => Left(Seq(FormError(dayKey, futureDateErrorMsg)))
        case Some(date) if invalidAge(date) => Left(Seq(FormError(dayKey, invalidAgeErrorKey)))
        case Some(date) => Right(date)
      }

    }

    private def manageMissingData(dayExists: Boolean, monthExists: Boolean, yearExists: Boolean): Either[Seq[FormError], LocalDate] = {

      (dayExists, monthExists, yearExists) match {
        case (false, true, true) => Left(Seq(FormError(dayKey, missingDayErrorMsg)))
        case (true, false, true) => Left(Seq(FormError(monthKey, missingMonthErrorMsg)))
        case (true, true, false) => Left(Seq(FormError(yearKey, missingYearErrorMsg)))
        case (false, false, true) => Left(Seq(FormError(dayKey, missingDayAndMonthErrorMsg)))
        case (false, true, false) => Left(Seq(FormError(dayKey, missingDayAndYearErrorMsg)))
        case (true, false, false) => Left(Seq(FormError(monthKey, missingMonthAndYearErrorMsg)))
        case (false, false, false) => Left(Seq(FormError(dayKey, missingDateErrorMsg)))
        case (true, true, true) => throw new InternalServerException("Error : Unexpected invocation of method manageMissingData of object DateHelper")
      }

    }

    private def invalidAge(dateOfBirth: LocalDate): Boolean = {

      val minAge = 16

      dateOfBirth.isAfter(timeMachine.now().minusYears(minAge))
    }

  }

  def captureDateOfBirthForm(): Form[LocalDate] = Form(
    single(dateKey -> of[LocalDate](localDateFormatter))
  )

}


