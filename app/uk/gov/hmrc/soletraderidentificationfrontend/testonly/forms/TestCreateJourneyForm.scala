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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.forms

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.data.validation.Constraint
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.validate
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, PageConfig}

object TestCreateJourneyForm {

  val continueUrl = "continueUrl"
  val serviceName = "serviceName"
  val deskProServiceId = "deskProServiceId"
  val alphanumericRegex = "^[A-Z0-9]*$"
  val signOutUrl = "signOutUrl"
  val enableSautrCheck = "enableSautrCheck"

  def continueUrlEmpty: Constraint[String] = Constraint("continue_url.not_entered")(
    companyNumber => validate(
      constraint = companyNumber.isEmpty,
      errMsg = "Continue URL not entered"
    )
  )

  def deskProServiceIdEmpty: Constraint[String] = Constraint("desk_pro_service_id.not_entered")(
    serviceId => validate(
      constraint = serviceId.isEmpty,
      errMsg = "DeskPro Service Identifier is not entered"
    )
  )

  def signOutUrlEmpty: Constraint[String] = Constraint("sign_out_url.not_entered")(
    signOutUrl => validate(
      constraint = signOutUrl.isEmpty,
      errMsg = "Sign Out Url is not entered"
    )
  )

  def form(enableSautrCheck: Boolean): Form[JourneyConfig] = {
    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      serviceName -> optText,
      deskProServiceId -> text.verifying(deskProServiceIdEmpty),
      signOutUrl -> text.verifying(signOutUrlEmpty)
    )((continueUrl, serviceName, deskProServiceId, signOutUrl) =>
      JourneyConfig.apply(continueUrl, PageConfig(serviceName, deskProServiceId, signOutUrl, enableSautrCheck))
    )(journeyConfig =>
      Some(journeyConfig.continueUrl, journeyConfig.pageConfig.optServiceName,
        journeyConfig.pageConfig.deskProServiceId, journeyConfig.pageConfig.signOutUrl)
    ))
  }

  def deprecatedForm(): Form[JourneyConfig] = {
    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      serviceName -> optText,
      deskProServiceId -> text.verifying(deskProServiceIdEmpty),
      signOutUrl -> text.verifying(signOutUrlEmpty),
      enableSautrCheck -> optText.toBoolean
    )((continueUrl, serviceName, deskProServiceId, signOutUrl, enableSautrCheck) =>
      JourneyConfig.apply(continueUrl, PageConfig(serviceName, deskProServiceId, signOutUrl, enableSautrCheck))
    )(journeyConfig =>
      Some(journeyConfig.continueUrl, journeyConfig.pageConfig.optServiceName,
        journeyConfig.pageConfig.deskProServiceId, journeyConfig.pageConfig.signOutUrl,
        journeyConfig.pageConfig.enableSautrCheck)
    ))
  }

}
