/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RemoveSoleTraderDetailsHttpParser._
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RetrieveIndividualDetailsHttpParser.RetrieveIndividualDetailsHttpReads
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser._
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, SoleTraderDetails}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderIdentificationConnector @Inject() (http: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def retrieveSoleTraderDetails[DataType](journeyId: String, dataKey: String)(implicit
    dataTypeReads: Reads[DataType],
    manifest: Manifest[DataType],
    hc: HeaderCarrier
  ): Future[Option[DataType]] =
    http.get(url = url"${appConfig.soleTraderIdentificationUrl(journeyId)}/$dataKey")(hc).execute[Option[DataType]]

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[SoleTraderDetails]] =
    http.get(url = url"${appConfig.soleTraderIdentificationUrl(journeyId)}")(hc).execute[Option[SoleTraderDetails]]

  def retrieveIndividualDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[IndividualDetails]] =
    http
      .get(url = url"${appConfig.soleTraderIdentificationUrl(journeyId)}")(hc)
      .execute[Option[IndividualDetails]](RetrieveIndividualDetailsHttpReads, ec)

  def storeData[DataType](journeyId: String, dataKey: String, data: DataType)(implicit
    dataTypeWriter: Writes[DataType],
    hc: HeaderCarrier
  ): Future[SuccessfullyStored.type] = {
    http
      .put(url = url"${appConfig.soleTraderIdentificationUrl(journeyId)}/$dataKey")(hc)
      .withBody(Json.toJson(data))
      .execute[SuccessfullyStored.type](SoleTraderIdentificationStorageHttpReads, ec)
  }

  def removeSoleTraderDetails(journeyId: String, dataKey: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http
      .delete(url = url"${appConfig.soleTraderIdentificationUrl(journeyId)}/$dataKey")(hc)
      .execute[SuccessfullyRemoved.type](RemoveSoleTraderDetailsHttpReads, ec)

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http
      .delete(url = url"${appConfig.soleTraderIdentificationUrl(journeyId)}")(hc)
      .execute[SuccessfullyRemoved.type](RemoveSoleTraderDetailsHttpReads, ec)

}
