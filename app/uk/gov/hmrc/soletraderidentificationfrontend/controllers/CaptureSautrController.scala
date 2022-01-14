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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureSautrForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_sautr_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureSautrController @Inject()(mcc: MessagesControllerComponents,
                                       view: capture_sautr_page,
                                       soleTraderIdentificationService: SoleTraderIdentificationService,
                                       val authConnector: AuthConnector,
                                       journeyService: JourneyService
                                      )(implicit val config: AppConfig,
                                        executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {


  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        for {
          journeyConfig <- journeyService.getJourneyConfig(journeyId)
          firstName <- soleTraderIdentificationService
            .retrieveFullName(journeyId)
            .map(optFullName => optFullName.map(_.firstName).getOrElse(throw new IllegalStateException("Full name not found")))
        } yield {
          Ok(view(
            firstName,
            journeyId = journeyId,
            pageConfig = journeyConfig.pageConfig,
            formAction = routes.CaptureSautrController.submit(journeyId),
            form = CaptureSautrForm.form
          ))
        }
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        CaptureSautrForm.form.bindFromRequest().fold(
          formWithErrors => {
            for {
              journeyConfig <- journeyService.getJourneyConfig(journeyId)
              firstName <- soleTraderIdentificationService
                .retrieveFullName(journeyId)
                .map(optFullName => optFullName.map(_.firstName).getOrElse(throw new IllegalStateException("Full name not found")))
            } yield {
              BadRequest(view(
                firstName,
                journeyId = journeyId,
                pageConfig = journeyConfig.pageConfig,
                formAction = routes.CaptureSautrController.submit(journeyId),
                form = formWithErrors
              ))
            }
          },
          sautr =>
            for {
              _ <- soleTraderIdentificationService.storeSautr(journeyId, sautr)
              _ <- soleTraderIdentificationService.removeSaPostcode(journeyId)
              optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
            } yield optNino match {
              case Some(_) =>
                Redirect(routes.CheckYourAnswersController.show(journeyId))
              case None =>
                Redirect(routes.CaptureSaPostcodeController.show(journeyId))
            }
        )
      }
  }

  def noSautr(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        for {
          _ <- soleTraderIdentificationService.removeSautr(journeyId)
          _ <- soleTraderIdentificationService.removeSaPostcode(journeyId)
          optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
        } yield optNino match {
          case Some(_) =>
            Redirect(routes.CheckYourAnswersController.show(journeyId))
          case None =>
            Redirect(routes.CaptureOverseasTaxIdentifiersController.show(journeyId))
        }
      }
  }

}
