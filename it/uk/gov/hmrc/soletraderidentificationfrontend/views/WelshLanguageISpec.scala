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
package uk.gov.hmrc.soletraderidentificationfrontend.views

import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

import play.api.i18n.{Lang, MessagesApi}

import scala.io.{BufferedSource, Source}

class WelshLanguageISpec extends ComponentSpecHelper {

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  val englishMessageKeysSource: BufferedSource = Source.fromResource("messages")
  val welshMessageKeysSource: BufferedSource = Source.fromResource("messages.cy")

  val englishMessageKeys: List[String] = getMessageKeys(englishMessageKeysSource).toList
  val welshMessageKeys: List[String] = getMessageKeys(welshMessageKeysSource).toList

  englishMessageKeysSource.close()
  welshMessageKeysSource.close()

  "the message files" should {

    "not have duplicate english keys" in {
      englishMessageKeys.toSet.size mustBe englishMessageKeys.size
    }

    "not have duplicate welsh keys" in {
      welshMessageKeys.toSet.size mustBe welshMessageKeys.size
    }

    "have a matching welsh key for each english key" in {
      for(key <- englishMessageKeys) welshMessageKeys.contains(key) mustBe true
    }

    "have a matching english key for each welsh key with the exception of the months of the year in welsh" in {
      for(key <- welshMessageKeys.filterNot(_.startsWith("date.month.name"))) englishMessageKeys.contains(key) mustBe true
    }
  }

  "the internationalisation mechanism" should {

    "support the Welsh language" in {
      messagesApi("service.name.default")(Lang("cy")) mustBe "Gwasanaeth Dilysu Endid"
    }

  }

  private def getMessageKeys(source: Source): Iterator[String] =
    source
      .getLines
      .map(_.trim)
      .filterNot(_.startsWith("#"))
      .filter(_.nonEmpty)
      .map(_.split(' ').head)

}
