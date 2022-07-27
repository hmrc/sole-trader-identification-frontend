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

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureSautrForm.sautrRegex

object CaptureSautrNewForm {

  val option_yes: String = "Yes"
  val option_no: String = "No"
  val radioKey: String = "optSautr"
  val sautrValueKey: String = "sa-utr"

  val sautrErrorKey: String = "sa-utr.error"
  val sautrRadioMissingKey: String = "new-sautr.no-selection"

  def sautrMapping(radioError: String, sautrError: String): Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      data.get(key) match {
        case Some(`option_yes`) =>
          data.get(sautrValueKey) match {
            case Some(sautr) =>
              if (sautr.matches(sautrRegex.regex)) Right(Some(sautr))
              else Left(Seq(FormError(sautrValueKey, sautrError)))
            case _ => Left(Seq(FormError(sautrValueKey, sautrError)))
          }
        case Some(`option_no`) => Right(None)
        case _ => Left(Seq(FormError(key, radioError)))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {
      val stringValue = value match {
        case Some(_) => option_yes
        case None => option_no
      }

      Map(key -> stringValue)
    }
  }

  val form: Form[Option[String]] =
    Form(
      radioKey -> of(sautrMapping(sautrRadioMissingKey, sautrErrorKey))
    )

}
