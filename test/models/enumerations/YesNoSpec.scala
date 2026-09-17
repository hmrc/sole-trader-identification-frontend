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

package models.enumerations

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsString, JsSuccess, JsValue, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.enumerations.YesNo
import uk.gov.hmrc.soletraderidentificationfrontend.models.enumerations.YesNo.format

class YesNoSpec extends AnyWordSpec with Matchers {

  "YesNo enumeration" should {

    "be able to de-serialise the enumeration value 'Yes'" in {

      val yesAsJsValue: JsString = JsString(YesNo.Yes.toString)

      yesAsJsValue.validate[YesNo.Value] match {
        case JsSuccess(value, _) => value mustBe YesNo.Yes
        case e: JsError => fail(s"Parse of 'Yes' failed with error: $e")
      }

    }

    "be able to de-serialise the enumeration value 'No'" in {

      val noAsJsValue: JsString = JsString(YesNo.No.toString)

      noAsJsValue.validate[YesNo.Value] match {
        case JsSuccess(value, _) => value mustBe YesNo.No
        case e: JsError => fail(s"Parse of 'No' failed with error: $e")
      }

    }

    "raise an error when de-serialising an invalid enumeration value" in {

      val invalidAsJsValue: JsString = JsString("Invalid")

      invalidAsJsValue.validate[YesNo.Value] match {
        case JsSuccess(value, _) => fail(s"Parse of 'Invalid' should have failed but returned: $value")
        case e: JsError => e.errors.head._2.head.message mustBe "Unknown enum value : Invalid"
      }

    }

    "serialise the value 'Yes' to JsString" in {

      val yesAsJsValue: JsValue = Json.toJson(YesNo.Yes)

      yesAsJsValue mustBe JsString("Yes")

    }

    "serialise the value 'No' to JsString" in {

      val noAsJsValue: JsValue = Json.toJson(YesNo.No)

      noAsJsValue mustBe JsString("No")

    }
  }

}
