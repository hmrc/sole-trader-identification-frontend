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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsString
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.SoleTraderIdentificationConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderIdentificationService @Inject()(connector: SoleTraderIdentificationConnector
                                               )(implicit ec: ExecutionContext) {


  def storeFullName(journeyId: String, fullName: FullNameModel)(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData(journeyId, fullNameKey, fullName)

  def storeDateOfBirth(journeyId: String, dateOfBirth: LocalDate)(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData(journeyId, dateOfBirthKey, dateOfBirth)

  def storeNino(journeyId: String, nino: String)(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData(journeyId, ninoKey, nino)

  def storeSautr(journeyId: String, sautr: String)(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData(journeyId, sautrKey, sautr)


  def retrieveFullName(journeyId: String
                      )(implicit hc: HeaderCarrier): Future[Option[FullNameModel]] =
    connector.retrieveSoleTraderIdentification[FullNameModel](journeyId, fullNameKey)

  def retrieveDateOfBirth(journeyId: String
                         )(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    connector.retrieveSoleTraderIdentification[LocalDate](journeyId, dateOfBirthKey)

  def retrieveNino(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderIdentification[JsString](journeyId, ninoKey).map {
      case Some(jsString) => Some(jsString.value)
      case None => None
    }

  def retrieveSautr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderIdentification[String](journeyId, sautrKey)

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[SoleTraderDetailsModel]] =
    connector.retrieveSoleTraderIdentification(journeyId)
}

