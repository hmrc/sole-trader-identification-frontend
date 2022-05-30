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

package uk.gov.hmrc.soletraderidentificationfrontend.utils

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}

import play.api.i18n.Messages

object DateHelper {
  val checkYourAnswersFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy").withResolverStyle(ResolverStyle.STRICT)

  def formatDate(localDate: LocalDate)(implicit messages: Messages): String = {

    if(messages.lang.code == "cy") {

      val monthAsNumber: Int = localDate.getMonthValue

      val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
        s"""d '${messages(s"date.month.name.$monthAsNumber")}' yyyy"""
      ).withResolverStyle(ResolverStyle.STRICT)

      localDate.format(dateTimeFormatter)
    } else {
      localDate.format(checkYourAnswersFormat)
    }
  }

}
