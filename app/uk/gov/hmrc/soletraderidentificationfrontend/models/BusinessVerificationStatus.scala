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

package uk.gov.hmrc.soletraderidentificationfrontend.models

import play.api.libs.json._

sealed trait BusinessVerificationStatus

case object BusinessVerificationPass extends BusinessVerificationStatus

case object BusinessVerificationFail extends BusinessVerificationStatus

case object BusinessVerificationNotEnoughInformationToChallenge extends BusinessVerificationStatus

case object BusinessVerificationNotEnoughInformationToCallBV extends BusinessVerificationStatus

// to be removed after SAR-9396 release
case object BusinessVerificationUnchallenged extends BusinessVerificationStatus

case object SaEnrolled extends BusinessVerificationStatus

object BusinessVerificationStatus {
  val BusinessVerificationPassKey = "PASS"
  val BusinessVerificationFailKey = "FAIL"
  val BusinessVerificationNotEnoughInfoToChallengeKey = "NOT_ENOUGH_INFORMATION_TO_CHALLENGE"
  val BusinessVerificationNotEnoughInfoToCallBVKey = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"
  val BusinessVerificationUnchallengedKey = "UNCHALLENGED" // remove after SAR-9396 release
  val BusinessVerificationSaEnrolledKey = "SA_ENROLLED"
  val BusinessVerificationStatusKey = "verificationStatus"

  implicit val format: Format[BusinessVerificationStatus] = new Format[BusinessVerificationStatus] {
    override def writes(businessVerificationStatus: BusinessVerificationStatus): JsObject = {
      val businessVerificationStatusString = businessVerificationStatus match {
        case BusinessVerificationPass => BusinessVerificationPassKey
        case BusinessVerificationFail => BusinessVerificationFailKey
        case BusinessVerificationNotEnoughInformationToChallenge => BusinessVerificationNotEnoughInfoToChallengeKey
        case BusinessVerificationNotEnoughInformationToCallBV => BusinessVerificationNotEnoughInfoToCallBVKey
        case SaEnrolled => BusinessVerificationSaEnrolledKey
        case BusinessVerificationUnchallenged => BusinessVerificationUnchallengedKey
      }

      Json.obj(BusinessVerificationStatusKey -> businessVerificationStatusString)
    }

    override def reads(json: JsValue): JsResult[BusinessVerificationStatus] =
      (json \ BusinessVerificationStatusKey).validate[String].collect(JsonValidationError("Invalid business validation state")) {
        case BusinessVerificationPassKey => BusinessVerificationPass
        case BusinessVerificationFailKey => BusinessVerificationFail
        case BusinessVerificationNotEnoughInfoToChallengeKey => BusinessVerificationNotEnoughInformationToChallenge
        case BusinessVerificationNotEnoughInfoToCallBVKey => BusinessVerificationNotEnoughInformationToCallBV
        case BusinessVerificationSaEnrolledKey => SaEnrolled
        case BusinessVerificationUnchallengedKey => BusinessVerificationUnchallenged
      }
  }
}
