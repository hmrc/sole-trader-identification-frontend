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
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.NinoNotFound
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.services._
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.check_your_answers_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           soleTraderIdentificationService: SoleTraderIdentificationService,
                                           journeyService: JourneyService,
                                           submissionService: SubmissionService,
                                           auditService: AuditService,
                                           summaryListRowBuilderService: CheckYourAnswersRowBuilder,
                                           val authConnector: AuthConnector
                                          )(implicit val config: AppConfig,
                                            executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {


  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        soleTraderIdentificationService.retrieveIndividualDetails(journeyId).flatMap {
          case Some(individualDetails) =>
            journeyService.getJourneyConfig(journeyId).flatMap {
              journeyConfig => soleTraderIdentificationService.retrieveAddress(journeyId).flatMap {
                address =>
                  summaryListRowBuilderService.buildSummaryListRowSeq(journeyId, individualDetails, address, journeyConfig.pageConfig.enableSautrCheck).map {
                    summaryRows => Ok(view(
                      pageConfig = journeyConfig.pageConfig,
                      formAction = routes.CheckYourAnswersController.submit(journeyId),
                      journeyId = journeyId,
                      individualDetails = individualDetails,
                      summaryRows = summaryRows
                    ))
                  }
              }
            }
          case _ =>
            throw new InternalServerException("Failed to retrieve data from database")
        }
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        journeyService.getJourneyConfig(journeyId).flatMap {
          journeyConfig =>
            submissionService.submit(journeyId).map {
              case StartBusinessVerification(businessVerificationUrl) =>
                Redirect(businessVerificationUrl)
              case JourneyCompleted(continueUrl) =>
                if (journeyConfig.pageConfig.enableSautrCheck) auditService.auditSoleTraderJourney(journeyId)
                else auditService.auditIndividualJourney(journeyId)
                Redirect(continueUrl + s"?journeyId=$journeyId")
              case SoleTraderDetailsMismatch(NinoNotFound) =>
                if (journeyConfig.pageConfig.enableSautrCheck) auditService.auditSoleTraderJourney(journeyId)
                else auditService.auditIndividualJourney(journeyId)
                Redirect(routes.DetailsNotFoundController.show(journeyId))
              case SoleTraderDetailsMismatch(_) =>
                if (journeyConfig.pageConfig.enableSautrCheck) auditService.auditSoleTraderJourney(journeyId)
                else auditService.auditIndividualJourney(journeyId)
                Redirect(routes.PersonalInformationErrorController.show(journeyId))
            }
        }
      }
  }

}
