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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyLabels, TranslationLabels}

object OptionalJourneyLabelsFormatter extends Formatter[Option[JourneyLabels]] {

  val welshFullNamePageLabelKeySuffix: String = "welshFullNamePageLabel"
  val welshServiceNameKeySuffix: String = "welshServiceName"

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[JourneyLabels]] = {

    val welshFullNamePageLabel: Option[String] = data.get(welshFullNamePageLabelKey(key)).flatMap(label => if(label.isEmpty) None else Some(label))
    val welshServiceName: Option[String] = data.get(welshServiceNameKey(key)).flatMap(serviceName => if(serviceName.isEmpty) None else Some(serviceName))

    (welshFullNamePageLabel, welshServiceName) match {
      case (None, None) => Right(None)
      case _ => Right(Some(JourneyLabels(TranslationLabels(welshFullNamePageLabel, welshServiceName))))
    }

  }

  override def unbind(key: String, value: Option[JourneyLabels]): Map[String,String] = {

    value match {
      case Some(journeyLabels) => (journeyLabels.welsh.optFullNamePageLabel, journeyLabels.welsh.optServiceName) match {
        case (Some(welshFullNamePageLabel), Some(welshServiceName)) => Map(
          welshFullNamePageLabelKey(key) -> welshFullNamePageLabel,
          welshServiceNameKey(key) -> welshServiceName
        )
        case(Some(welshFullNamePageLabel), None) => Map(welshFullNamePageLabelKey(key) -> welshFullNamePageLabel)
        case(None, Some(welshServiceName)) => Map(welshServiceNameKey(key) -> welshServiceName)
        case (None, None) => Map.empty
      }
      case _ => Map.empty
    }

  }

  private def welshFullNamePageLabelKey(key: String): String = s"$key.$welshFullNamePageLabelKeySuffix"
  private def welshServiceNameKey(key: String): String = s"$key.$welshServiceNameKeySuffix"

}
