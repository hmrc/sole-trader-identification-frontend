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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.{AuthenticatorConnector, RetrieveKnownFactsConnector}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching._
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, JourneyConfig, KnownFactsResponse, KnownFactsNoContentError}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderMatchingService @Inject()(authenticatorConnector: AuthenticatorConnector,
                                          retrieveKnownFactsConnector: RetrieveKnownFactsConnector,
                                          soleTraderIdentificationService: SoleTraderIdentificationService) {

  def matchSoleTraderDetails(journeyId: String,
                             individualDetails: IndividualDetails,
                             journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier,
                                                           ec: ExecutionContext): Future[SoleTraderDetailsMatchResult] =
    for {
      authenticatorResponse <- authenticatorConnector.matchSoleTraderDetails(individualDetails).map {
        case Right(authenticatorDetails) if journeyConfig.pageConfig.enableSautrCheck =>
          if (authenticatorDetails.optSautr == individualDetails.optSautr)
            Right(authenticatorDetails)
          else {
            Left(DetailsMismatch)
          }
        case authenticatorResponse =>
          authenticatorResponse
      }
      matchingResponse <- authenticatorResponse match {
        case Right(details) =>
          soleTraderIdentificationService.storeAuthenticatorDetails(journeyId, details).flatMap {
            _ =>
              soleTraderIdentificationService.storeIdentifiersMatch(journeyId, SuccessfulMatch).map {
                _ => SuccessfulMatch
              }
          }
        case Left(failureResponse) =>
          soleTraderIdentificationService.storeAuthenticatorFailureResponse(journeyId, failureResponse).flatMap {
            _ =>
              soleTraderIdentificationService.storeIdentifiersMatch(journeyId, failureResponse).map {
                _ => failureResponse
              }
          }
      }
    } yield
      matchingResponse


  def matchSoleTraderDetailsNoNino(journeyId: String,
                                   individualDetails: IndividualDetails
                                  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SoleTraderDetailsMatchResult] = {
    for {
      optUserPostcode <- soleTraderIdentificationService.retrieveSaPostcode(journeyId)
      matchingResponse <-
        if (individualDetails.optSautr.isEmpty) {
          Future.successful(NotEnoughInformationToMatch)
        }
        else {
          retrieveKnownFactsConnector.retrieveKnownFacts(individualDetails.optSautr.get).flatMap {
            case Right(KnownFacts@KnownFactsResponse(_, _, Some(_))) =>
              soleTraderIdentificationService.storeES20Details(journeyId, KnownFacts).map(
                _ => NinoNotDeclaredButFound
              )
            case Right(KnownFacts@KnownFactsResponse(Some(retrievePostcode), _, _))
              if optUserPostcode.exists(userPostcode => userPostcode filterNot (_.isWhitespace) equalsIgnoreCase (retrievePostcode filterNot (_.isWhitespace))) =>
              soleTraderIdentificationService.storeES20Details(journeyId, KnownFacts).map(
                _ => SuccessfulMatch
              )
            case Right(KnownFacts@KnownFactsResponse(_, Some(true), _)) if optUserPostcode.isEmpty =>
              soleTraderIdentificationService.storeES20Details(journeyId, KnownFacts).map(
                _ => SuccessfulMatch
              )
            case Right(KnownFacts@KnownFactsResponse(_, _, _)) =>
              soleTraderIdentificationService.storeES20Details(journeyId, KnownFacts).map(
                _ => DetailsMismatch
              )
            case Left(KnownFactsNoContentError) => Future.successful(KnownFactsNoContent)
          }
        }
      _ <- soleTraderIdentificationService.storeIdentifiersMatch(journeyId, matchingResponse)
    }

    yield {
      matchingResponse
    }
  }

}
