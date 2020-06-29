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

package uk.gov.hmrc.soletraderidentificationfrontend.utils

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}

import scala.collection.JavaConversions._

trait ViewSpec {

  implicit class ElementTest(element: Element) {

    lazy val content: Element = element.getElementsByTag("article").head

    lazy val getParagraphs: Elements = element.getElementsByTag("p")

    lazy val getBulletPoints: Elements = element.getElementsByTag("li")

    lazy val getH1Elements: Elements = element.getElementsByTag("h1")

    lazy val getH2Elements: Elements = element.getElementsByTag("h2")

    lazy val getFormElements: Elements = element.getElementsByClass("form-field-group")

    lazy val getLabelElement: Elements = element.getElementsByTag("label")

    lazy val getLegendElement: Elements = element.getElementsByTag("legend")

    lazy val getErrorSummaryMessage: Elements = element.select("#error-summary-display ul")

    lazy val getSubmitButton: Elements = element.select("button[type=submit]")

    lazy val getHintText: String = element.select(s"""span[class=form-hint]""").text()

    lazy val getForm: Elements = element.select("form")

    def getSpan(id: String): Elements = element.select(s"""span[id=$id]""")

    def getLink(id: String): Elements = element.select(s"""a[id=$id]""")

    def getTextFieldInput(id: String): Elements = element.select(s"""input[id=$id]""")

    def getFieldErrorMessage(id: String): Elements = element.select(s"""a[id=$id-error-summary]""")

    def getBulletPointList: Elements = element.select("ul[class=list list-bullet]")

  }

  def text(text: String): HavePropertyMatcher[Elements, String] =
    new HavePropertyMatcher[Elements, String] {
      def apply(element: Elements) =
        HavePropertyMatchResult(
          element.text() == text,
          "text",
          text,
          element.text()
        )
    }

}
