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

package uk.gov.hmrc.soletraderidentificationfrontend.models.enumerations

import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}
import scala.language.implicitConversions

object YesNo extends Enumeration {

  type YesNo = Value

  val Yes: Value = Value("Yes")
  val No: Value = Value("No")

  implicit def toString(choice: YesNo.Value): String = choice.toString

  private def fromString(s: String): Option[Value] = YesNo.values.collectFirst { case v if v.toString == s => v }

  implicit val yesNoReads: Reads[YesNo.Value] = Reads {
    case JsString(s) =>
      fromString(s) match {
        case Some(value) => JsSuccess(value)
        case None        => JsError(s"Unknown enum value : $s")
      }
    case _ => JsError(s"String value expected")
  }

  implicit val yesNoWrites: Writes[YesNo.Value] = Writes(v => JsString(v.toString))

  implicit val format: Format[YesNo.Value] = Format(yesNoReads, yesNoWrites)
}
