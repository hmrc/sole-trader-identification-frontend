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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter

object OptionalBooleanMapping {

  def optionalBooleanMapping(): Formatter[Option[Boolean]] = new Formatter[Option[Boolean]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Boolean]] = {
      data.get(key) match {
        case Some(s) if s.trim.toUpperCase == "FALSE" => Right(Some(false))
        case Some(s) if s.trim.toUpperCase == "TRUE" => Right(Some(true))
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[Boolean]): Map[String, String] = {
      val mapValue: String = value match {
        case Some(b) => b.toString
        case _ => ""
      }
      Map(key -> mapValue)
    }

  }

}
