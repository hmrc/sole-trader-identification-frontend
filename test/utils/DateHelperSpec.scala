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

package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.i18n.{Lang, MessagesApi, Messages}

import uk.gov.hmrc.soletraderidentificationfrontend.utils.DateHelper

class DateHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val localDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/uuuu")

  trait EnglishLanguageTest {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))
  }

  trait WelshLanguageTest {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("cy")))
  }

  "DateHelper" should {

    "correctly represent a local date as a string in english" in new EnglishLanguageTest {

      val date: LocalDate = LocalDate.parse("7/7/1980", localDateFormatter)

      DateHelper.formatDate(date) mustBe "7 July 1980"
    }

    "correctly represent a selection of dates as strings in Welsh" in new WelshLanguageTest {

      // 1st January 2020
      val date1: LocalDate = LocalDate.parse("1/1/2020", localDateFormatter)

      DateHelper.formatDate(date1) mustBe "1 Ionawr 2020"

      // 21st June 1999
      val date2: LocalDate = LocalDate.parse("21/06/1999", localDateFormatter)

      DateHelper.formatDate(date2) mustBe "21 Mehefin 1999"

      // 30th October 2016
      val date3: LocalDate = LocalDate.parse("30/10/2016", localDateFormatter)

      DateHelper.formatDate(date3) mustBe "30 Hydref 2016"

      // 31st December 2000
      val date4: LocalDate = LocalDate.parse("31/12/2000", localDateFormatter)

      DateHelper.formatDate(date4) mustBe "31 Rhagfyr 2000"
    }

  }
}
