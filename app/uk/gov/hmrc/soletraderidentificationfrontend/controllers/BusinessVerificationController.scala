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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessVerificationController @Inject()(mcc: MessagesControllerComponents,
                                               val authConnector: AuthConnector,
                                               businessVerificationService: BusinessVerificationService,
                                               journeyService: JourneyService,
                                               registrationOrchestrationService: RegistrationOrchestrationService,
                                               soleTraderIdentificationService: SoleTraderIdentificationService
                                              )(implicit val executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def retrieveBusinessVerificationResult(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised() {
        req.getQueryString("journeyId") match {
          case Some(businessVerificationJourneyId) =>
            for {
              journeyConfig <- journeyService.getJourneyConfig(journeyId)
              verificationStatus <- businessVerificationService.retrieveBusinessVerificationStatus(businessVerificationJourneyId)
              _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, verificationStatus)
              _ <- registrationOrchestrationService.register(journeyId)
            } yield {
              SeeOther(journeyConfig.continueUrl + s"?journeyId=$journeyId")
            }
          case None =>
            throw new InternalServerException("Missing JourneyID from Business Verification callback")
        }
      }
  }

}