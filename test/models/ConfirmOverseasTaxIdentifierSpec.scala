/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.ConfirmOverseasTaxIdentifier
import uk.gov.hmrc.soletraderidentificationfrontend.models.ConfirmOverseasTaxIdentifier.format
import uk.gov.hmrc.soletraderidentificationfrontend.models.enumerations.YesNo

class ConfirmOverseasTaxIdentifierSpec extends AnyWordSpec with Matchers {

  "ConfirmOverseasTaxIdentifier" should {

    "be able to de-serialise a valid JSON object" in new Setup {

      val json: JsValue = Json.parse(overseasTaxIdentifierConfirmed)

      json.validate[ConfirmOverseasTaxIdentifier] match {
        case JsSuccess(value, _) =>
          value.hasOverseasTaxIdentifier mustBe YesNo.Yes
          value.overseasTaxIdentifier mustBe Some("123456789")
        case e: JsError => fail(s"Parse of valid JSON failed with error: $e")
      }

    }

    "be able to de-serialise a valid JSON object when tax identifier is not confirmed" in new Setup {

      val json: JsValue = Json.parse(overseasTaxIdentifierNotConfirmed)

      json.validate[ConfirmOverseasTaxIdentifier] match {
        case JsSuccess(value, _) =>
          value.hasOverseasTaxIdentifier mustBe YesNo.No
          value.overseasTaxIdentifier mustBe None
        case e: JsError => fail(s"Parse of valid JSON failed with error: $e")
      }

    }

    "raise an error when de-serialising an invalid JSON object" in new Setup {

      val invalidJson: JsValue = Json.parse(invalidOverseasTaxIdentifierConfirmed)

      invalidJson.validate[ConfirmOverseasTaxIdentifier] match {
        case JsSuccess(value, _) => fail(s"Parse of invalid JSON should have failed but returned: $value")
        case e: JsError => e.errors.head._2.head.message mustBe "Unknown enum value : Invalid"
      }

    }

    "be able to serialise a ConfirmOverseasTaxIdentifier object with 'hasOverseasTaxIdentifier' set to 'Yes' to JSON" in new Setup {

      val json: JsValue = Json.toJson(confirmedOverseasTaxIdentifier)

      json.toString mustBe removeWhitespace(overseasTaxIdentifierConfirmed)

    }

    "be able to serialise a ConfirmOverseasTaxIdentifier object with 'hasOverseasTaxIdentifier' set to 'No' to JSON" in new Setup {

      val json: JsValue = Json.toJson(notConfirmedOverseasTaxIdentifier)

      json.toString mustBe removeWhitespace(overseasTaxIdentifierNotConfirmed)

    }

  }

  trait Setup {

    val overseasTaxIdentifierConfirmed: String =
      """
        |{
        |  "hasOverseasTaxIdentifier": "Yes",
        |  "overseasTaxIdentifier": "123456789"
        |}
        |""".stripMargin

    val overseasTaxIdentifierNotConfirmed: String =
      """
        |{
        |  "hasOverseasTaxIdentifier": "No"
        |}
        |""".stripMargin

    val invalidOverseasTaxIdentifierConfirmed: String =
      """
        |{
        |  "hasOverseasTaxIdentifier": "Invalid",
        |  "overseasTaxIdentifier": "123456789"
        |}
        |""".stripMargin

  }

  val confirmedOverseasTaxIdentifier: ConfirmOverseasTaxIdentifier = ConfirmOverseasTaxIdentifier(YesNo.Yes, Some("123456789"))

  val notConfirmedOverseasTaxIdentifier: ConfirmOverseasTaxIdentifier = ConfirmOverseasTaxIdentifier(YesNo.No, None)

  def removeWhitespace(s: String): String = s.replaceAll("\\s", "")

}
