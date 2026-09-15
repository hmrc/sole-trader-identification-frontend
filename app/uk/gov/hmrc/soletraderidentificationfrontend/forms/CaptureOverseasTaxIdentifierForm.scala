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

package uk.gov.hmrc.soletraderidentificationfrontend.forms

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import uk.gov.hmrc.soletraderidentificationfrontend.models.ConfirmOverseasTaxIdentifier
import uk.gov.hmrc.soletraderidentificationfrontend.models.enumerations.YesNo

import scala.util.matching.Regex

object CaptureOverseasTaxIdentifierForm {

  val identifiersRegex: Regex = """[A-Za-z0-9]{1,60}""".r

  private val overseasTaxIdentifierRadioKey: String = "tax-identifier-radio"
  private val overseasTaxIdentifierKey: String = "tax-identifier"

  private val noSelectionMadeErrorMsg: String = "error.no_tax_identifiers_selection"
  private val overseasTaxIdentifierNotEnteredErrorMsg: String = "error.no_tax_identifiers"
  private val overseasTaxIdentifierTooLongErrorMsg: String = "error.invalid_tax_identifiers_length"
  private val overseasTaxIdentifierInvalidCharsErrorMsg: String = "error.invalid_tax_identifiers"

  private val overseasTaxIdentifierFormatter: Formatter[ConfirmOverseasTaxIdentifier] = new Formatter[ConfirmOverseasTaxIdentifier] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ConfirmOverseasTaxIdentifier] = {

      val overseasTaxIdentifierChoice: String = data.getOrElse(key, "")

      if (overseasTaxIdentifierChoice == YesNo.Yes.toString) {

        handleOverseasTaxIdentifier(data)

      } else if (overseasTaxIdentifierChoice == YesNo.No.toString) {

        Right(ConfirmOverseasTaxIdentifier(YesNo.No))

      } else {

        Left(Seq(FormError(key, noSelectionMadeErrorMsg)))

      }

    }

    override def unbind(key: String, value: ConfirmOverseasTaxIdentifier): Map[String, String] = {

      val data: Map[String, String] = Map(key -> value.hasOverseasTaxIdentifier)

      if (value.hasOverseasTaxIdentifier == YesNo.Yes)
        data + (overseasTaxIdentifierKey -> value.overseasTaxIdentifier.get)
      else
        data

    }

    def handleOverseasTaxIdentifier(data: Map[String, String]): Either[Seq[FormError], ConfirmOverseasTaxIdentifier] = {

      data.get(overseasTaxIdentifierKey) match {
        case Some(id) => validateOverseasTaxIdentifier(id)
        case None     => Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierNotEnteredErrorMsg)))
      }

    }

    def validateOverseasTaxIdentifier(id: String): Either[Seq[FormError], ConfirmOverseasTaxIdentifier] = {

      if (validateEntered(id)) {

        if (validateLength(id)) {

          if (validateCharacters(id)) {

            Right(ConfirmOverseasTaxIdentifier(YesNo.Yes, Some(id)))

          } else {

            Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierInvalidCharsErrorMsg)))

          }

        } else {

          Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierTooLongErrorMsg)))

        }

      } else {

        Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierNotEnteredErrorMsg)))

      }

    }

    def validateEntered(id: String): Boolean = id.nonEmpty

    def validateLength(id: String): Boolean = id.length <= 60

    def validateCharacters(id: String): Boolean = id matches identifiersRegex.regex
  }

  val form: Form[ConfirmOverseasTaxIdentifier] = {
    Form(
      single(overseasTaxIdentifierRadioKey -> of[ConfirmOverseasTaxIdentifier](overseasTaxIdentifierFormatter))
    )
  }

}
