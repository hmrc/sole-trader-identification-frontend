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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.forms

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, of, text}
import play.api.data.validation.Constraint
import uk.gov.hmrc.soletraderidentificationfrontend.testonly.forms.mappings.OptionalJourneyLabelsFormatter
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.validate
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, JourneyLabels, PageConfig}

object TestCreateJourneyForm {

  val continueUrl = "continueUrl"
  val businessVerificationCheck = "businessVerificationCheck"
  val pageConfig = "pageConfig"
  val serviceName = "serviceName"
  val deskProServiceId = "deskProServiceId"
  val alphanumericRegex = "^[A-Z0-9]*$"
  val signOutUrl = "signOutUrl"
  val enableSautrCheck = "enableSautrCheck"
  val accessibilityUrl = "accessibilityUrl"
  val fullNamePageLabel = "fullNamePageLabel"
  val regime = "regime"
  val labels = "labels"
  val welshFullNamePageLabel = "welshFullNamePageLabel"
  val welshServiceName = "welshServiceName"

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

  def accessibilityUrlEmpty: Constraint[String] = Constraint("accessibility_url.not_entered")(
    signOutUrl => validate(
      constraint = signOutUrl.isEmpty,
      errMsg = "Accessibility Url is not entered"
    )
  )

  def regimeEmpty: Constraint[String] = Constraint("regime.not_entered")(
    regime => validate(
      constraint = regime.isEmpty,
      errMsg = "Regime is not entered"
    )
  )

  def form(): Form[JourneyConfig] = {

    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      businessVerificationCheck -> boolean,
       pageConfig -> mapping(
        serviceName -> optText,
        deskProServiceId -> text.verifying(deskProServiceIdEmpty),
        signOutUrl -> text.verifying(signOutUrlEmpty),
         enableSautrCheck -> boolean,
        accessibilityUrl -> text.verifying(signOutUrlEmpty),
        fullNamePageLabel -> optText,
        labels -> of[Option[JourneyLabels]](OptionalJourneyLabelsFormatter)
        )(PageConfig.apply)(PageConfig.unapply),
      regime -> text.verifying(regimeEmpty)
    )(JourneyConfig.apply)(JourneyConfig.unapply))
  }

  def deprecatedForm(): Form[JourneyConfig] = {

    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      businessVerificationCheck -> boolean,
      pageConfig -> mapping(
        serviceName -> optText,
        deskProServiceId -> text.verifying(deskProServiceIdEmpty),
        signOutUrl -> text.verifying(signOutUrlEmpty),
        enableSautrCheck -> optText.toBoolean,
        accessibilityUrl -> text.verifying(signOutUrlEmpty),
        fullNamePageLabel -> optText,
        labels -> of[Option[JourneyLabels]](OptionalJourneyLabelsFormatter)
      )(PageConfig.apply)(PageConfig.unapply),
      regime -> text.verifying(regimeEmpty)
    )(JourneyConfig.apply)(JourneyConfig.unapply))
  }

}
