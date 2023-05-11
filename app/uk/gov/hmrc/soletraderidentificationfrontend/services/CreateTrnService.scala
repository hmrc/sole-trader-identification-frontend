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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateTrnConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateTrnService @Inject() (soleTraderIdentificationService: SoleTraderIdentificationService, createTrnConnector: CreateTrnConnector)(implicit
  ec: ExecutionContext
) {

  def createTrn(journeyId: String)(implicit headerCarrier: HeaderCarrier): Future[String] = {
    for {
      optDateOfBirth <- soleTraderIdentificationService.retrieveDateOfBirth(journeyId)
      optName        <- soleTraderIdentificationService.retrieveFullName(journeyId)
      optAddress     <- soleTraderIdentificationService.retrieveAddress(journeyId)
      trn <- (optDateOfBirth, optName, optAddress) match {
               case (Some(dateOfBirth), Some(name), Some(address)) =>
                 createTrnConnector.createTrn(dateOfBirth,
                                              name.copy(firstName = name.firstName.capitalize, lastName = name.lastName.capitalize),
                                              address.withSanitisedPostcode
                                             )
               case _ => throw new InternalServerException(s"Missing required data to create TRN for journeyId: $journeyId")
             }
      _ <- soleTraderIdentificationService.storeTrn(journeyId, trn)
    } yield {
      trn
    }
  }

}
