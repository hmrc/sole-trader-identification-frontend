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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.forms

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, text}
import play.api.data.validation.Constraint
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.validate
import uk.gov.hmrc.soletraderidentificationfrontend.models.{JourneyConfig, JourneyLabels, PageConfig}

object TestCreateJourneyForm {

  val continueUrl = "continueUrl"
  val businessVerificationCheck = "businessVerificationCheck"
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

  def continueUrlEmpty: Constraint[String] = Constraint("continue_url.not_entered")(companyNumber =>
    validate(
      constraint = companyNumber.isEmpty,
      errMsg     = "Continue URL not entered"
    )
  )

  def deskProServiceIdEmpty: Constraint[String] = Constraint("desk_pro_service_id.not_entered")(serviceId =>
    validate(
      constraint = serviceId.isEmpty,
      errMsg     = "DeskPro Service Identifier is not entered"
    )
  )

  def signOutUrlEmpty: Constraint[String] = Constraint("sign_out_url.not_entered")(signOutUrl =>
    validate(
      constraint = signOutUrl.isEmpty,
      errMsg     = "Sign Out Url is not entered"
    )
  )

  def accessibilityUrlEmpty: Constraint[String] = Constraint("accessibility_url.not_entered")(signOutUrl =>
    validate(
      constraint = signOutUrl.isEmpty,
      errMsg     = "Accessibility Url is not entered"
    )
  )

  def regimeEmpty: Constraint[String] = Constraint("regime.not_entered")(regime =>
    validate(
      constraint = regime.isEmpty,
      errMsg     = "Regime is not entered"
    )
  )

  def form(enableSautrCheck: Boolean): Form[JourneyConfig] = {
    Form(
      mapping(
        continueUrl               -> text.verifying(continueUrlEmpty),
        businessVerificationCheck -> boolean,
        serviceName               -> optText,
        deskProServiceId          -> text.verifying(deskProServiceIdEmpty),
        signOutUrl                -> text.verifying(signOutUrlEmpty),
        accessibilityUrl          -> text.verifying(signOutUrlEmpty),
        fullNamePageLabel         -> optText,
        regime                    -> text.verifying(regimeEmpty),
        welshFullNamePageLabel    -> optText,
        welshServiceName          -> optText
      )(
        (continueUrl,
         businessVerificationCheck,
         serviceName,
         deskProServiceId,
         signOutUrl,
         accessibilityUrl,
         fullNamePageLabel,
         regime,
         welshFullNamePageLabel,
         welshServiceName
        ) =>
          JourneyConfig.apply(
            continueUrl,
            businessVerificationCheck,
            PageConfig(
              deskProServiceId,
              signOutUrl,
              enableSautrCheck,
              accessibilityUrl,
              JourneyLabels(welshServiceName, serviceName, welshFullNamePageLabel, fullNamePageLabel)
            ),
            regime
          )
      )(journeyConfig =>
        Some(
          (
            journeyConfig.continueUrl,
            journeyConfig.businessVerificationCheck,
            journeyConfig.pageConfig.labels.flatMap(_.optEnglishServiceName),
            journeyConfig.pageConfig.deskProServiceId,
            journeyConfig.pageConfig.signOutUrl,
            journeyConfig.pageConfig.accessibilityUrl,
            journeyConfig.pageConfig.labels.flatMap(_.optEnglishFullNamePageLabel),
            journeyConfig.regime,
            journeyConfig.pageConfig.labels.flatMap(_.optWelshServiceName),
            journeyConfig.pageConfig.labels.flatMap(_.optWelshFullNamePageLabel)
          )
        )
      )
    )
  }

  def deprecatedForm(): Form[JourneyConfig] = {
    Form(
      mapping(
        continueUrl               -> text.verifying(continueUrlEmpty),
        businessVerificationCheck -> boolean,
        serviceName               -> optText,
        deskProServiceId          -> text.verifying(deskProServiceIdEmpty),
        signOutUrl                -> text.verifying(signOutUrlEmpty),
        enableSautrCheck          -> optText.toBoolean,
        accessibilityUrl          -> text.verifying(signOutUrlEmpty),
        fullNamePageLabel         -> optText,
        regime                    -> text.verifying(regimeEmpty),
        welshFullNamePageLabel    -> optText,
        welshServiceName          -> optText
      )(
        (continueUrl,
         businessVerificationCheck,
         serviceName,
         deskProServiceId,
         signOutUrl,
         enableSautrCheck,
         accessibilityUrl,
         fullNamePageLabel,
         regime,
         welshFullNamePageLabel,
         welshServiceName
        ) =>
          JourneyConfig.apply(
            continueUrl,
            businessVerificationCheck,
            PageConfig(
              deskProServiceId,
              signOutUrl,
              enableSautrCheck,
              accessibilityUrl,
              JourneyLabels(welshServiceName, serviceName, welshFullNamePageLabel, fullNamePageLabel)
            ),
            regime
          )
      )(journeyConfig =>
        Some(
          (
            journeyConfig.continueUrl,
            journeyConfig.businessVerificationCheck,
            journeyConfig.pageConfig.labels.flatMap(_.optEnglishServiceName),
            journeyConfig.pageConfig.deskProServiceId,
            journeyConfig.pageConfig.signOutUrl,
            journeyConfig.pageConfig.enableSautrCheck,
            journeyConfig.pageConfig.accessibilityUrl,
            journeyConfig.pageConfig.labels.flatMap(_.optEnglishFullNamePageLabel),
            journeyConfig.regime,
            journeyConfig.pageConfig.labels.flatMap(_.optWelshServiceName),
            journeyConfig.pageConfig.labels.flatMap(_.optWelshFullNamePageLabel)
          )
        )
      )
    )
  }

}
