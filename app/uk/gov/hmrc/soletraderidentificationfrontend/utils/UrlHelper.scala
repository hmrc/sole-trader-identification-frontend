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

package uk.gov.hmrc.soletraderidentificationfrontend.utils

import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, OnlyRelative, RedirectUrl}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

@Singleton
class UrlHelper @Inject()(appConfig: AppConfig) {

  def isAValidUrl(urlToBeValidated: String): Boolean =
    Try(RedirectUrl(urlToBeValidated)) match {
      case Failure(_: IllegalArgumentException) =>
        false
      case Success(maybeAValidUrl) =>
        maybeAValidUrl.getEither(OnlyRelative | AbsoluteWithHostnameFromAllowlist(appConfig.allowedHosts)) match {
          case Right(_) => true
          case Left(_) => false
        }
    }

}
