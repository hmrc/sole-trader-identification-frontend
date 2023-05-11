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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{NinoNotDeclaredButFound, NinoNotFound}
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.services._
import uk.gov.hmrc.soletraderidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.check_your_answers_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject() (mcc: MessagesControllerComponents,
                                            view: check_your_answers_page,
                                            soleTraderIdentificationService: SoleTraderIdentificationService,
                                            journeyService: JourneyService,
                                            submissionService: SubmissionService,
                                            auditService: AuditService,
                                            rowBuilder: CheckYourAnswersRowBuilder,
                                            val authConnector: AuthConnector,
                                            messagesHelper: MessagesHelper
                                           )(implicit val config: AppConfig, executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with AuthorisedFunctions
    with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised().retrieve(internalId) {
      case Some(authInternalId) =>
        for {
          journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
          individualDetails <- soleTraderIdentificationService
                                 .retrieveIndividualDetails(journeyId)
                                 .map(_.getOrElse(throw new InternalServerException(s"Individual details not found for journeyId: $journeyId")))
          optAddress              <- soleTraderIdentificationService.retrieveAddress(journeyId)
          optSaPostcode           <- soleTraderIdentificationService.retrieveSaPostcode(journeyId)
          optOverseasTaxId        <- soleTraderIdentificationService.retrieveOverseasTaxIdentifier(journeyId)
          optOverseasTaxIdCountry <- soleTraderIdentificationService.retrieveOverseasTaxIdentifierCountry(journeyId)
          summaryRows = rowBuilder.buildSummaryListRows(journeyId,
                                                        individualDetails,
                                                        optAddress,
                                                        optSaPostcode,
                                                        optOverseasTaxId,
                                                        optOverseasTaxIdCountry,
                                                        journeyConfig.pageConfig.enableSautrCheck
                                                       )
        } yield {
          val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
          implicit val messages: Messages = remoteMessagesApi.preferred(request)
          Ok(
            view(
              pageConfig  = journeyConfig.pageConfig,
              formAction  = routes.CheckYourAnswersController.submit(journeyId),
              summaryRows = summaryRows
            )
          )
        }
      case None =>
        throw new InternalServerException("Internal ID could not be retrieved from Auth")
    }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised().retrieve(internalId and allEnrolments) {
      case Some(authInternalId) ~ enrolments =>
        journeyService.getJourneyConfig(journeyId, authInternalId).flatMap { journeyConfig =>
          for {
            (nextUrl, shouldAuditJourney) <- submissionService.submit(journeyId, journeyConfig, enrolments).map {
                                               case StartBusinessVerification(businessVerificationUrl) => (businessVerificationUrl, DoNotAuditJourney)
                                               case JourneyCompleted(continueUrl) => (continueUrl + s"?journeyId=$journeyId", AuditJourney)
                                               case SoleTraderDetailsMismatch(NinoNotFound) =>
                                                 (routes.DetailsNotFoundController.show(journeyId).url, AuditJourney)
                                               case SoleTraderDetailsMismatch(NinoNotDeclaredButFound) =>
                                                 (routes.CouldNotConfirmBusinessErrorController.show(journeyId).url, AuditJourney)
                                               case SoleTraderDetailsMismatch(_) =>
                                                 (routes.CannotConfirmBusinessErrorController.show(journeyId).url, AuditJourney)
                                             }
          } yield {
            if (shouldAuditJourney == AuditJourney) auditService.auditJourney(journeyId, journeyConfig) else ()
            Redirect(nextUrl)
          }
        }
      case None ~ _ =>
        throw new InternalServerException("Internal ID could not be retrieved from Auth")
    }
  }

  sealed trait JourneyState

  case object AuditJourney extends JourneyState

  case object DoNotAuditJourney extends JourneyState
}
