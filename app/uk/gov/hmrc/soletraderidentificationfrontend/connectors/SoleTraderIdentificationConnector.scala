/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RemoveSoleTraderDetailsHttpParser._
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RetrieveIndividualDetailsHttpParser.RetrieveIndividualDetailsHttpReads
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser._
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, SoleTraderDetails}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderIdentificationConnector @Inject() (http: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends HttpReadsInstances {

  def retrieveSoleTraderDetails[DataType](journeyId: String, dataKey: String)(implicit
    dataTypeReads: Reads[DataType],
    manifest: Manifest[DataType],
    hc: HeaderCarrier
  ): Future[Option[DataType]] =
    http.GET[Option[DataType]](s"${appConfig.soleTraderIdentificationUrl(journeyId)}/$dataKey")

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[SoleTraderDetails]] =
    http.GET[Option[SoleTraderDetails]](appConfig.soleTraderIdentificationUrl(journeyId))

  def retrieveIndividualDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[IndividualDetails]] =
    http.GET[Option[IndividualDetails]](appConfig.soleTraderIdentificationUrl(journeyId))(RetrieveIndividualDetailsHttpReads, hc, ec)

  def storeData[DataType](journeyId: String, dataKey: String, data: DataType)(implicit
    dataTypeWriter: Writes[DataType],
    hc: HeaderCarrier
  ): Future[SuccessfullyStored.type] = {
    http.PUT[DataType, SuccessfullyStored.type](s"${appConfig.soleTraderIdentificationUrl(journeyId)}/$dataKey", data)
  }

  def removeSoleTraderDetails(journeyId: String, dataKey: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http.DELETE[SuccessfullyRemoved.type](s"${appConfig.soleTraderIdentificationUrl(journeyId)}/$dataKey")(RemoveSoleTraderDetailsHttpReads, hc, ec)

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http.DELETE[SuccessfullyRemoved.type](appConfig.soleTraderIdentificationUrl(journeyId))(RemoveSoleTraderDetailsHttpReads, hc, ec)

}
