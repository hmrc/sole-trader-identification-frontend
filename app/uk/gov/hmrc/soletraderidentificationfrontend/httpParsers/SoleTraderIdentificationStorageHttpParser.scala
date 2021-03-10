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

package uk.gov.hmrc.soletraderidentificationfrontend.httpParsers

import play.api.http.Status.OK
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{StorageResult, SuccessfullyStored}

object SoleTraderIdentificationStorageHttpParser {

  implicit object SoleTraderIdentificationStorageHttpReads extends HttpReads[StorageResult] {
    override def read(method: String, url: String, response: HttpResponse): StorageResult = {
      response.status match {
        case OK =>
          SuccessfullyStored
        case status =>
          throw new InternalServerException(s"Storage in Sole Trader Identification failed with status: $status")
      }
    }
  }

}
