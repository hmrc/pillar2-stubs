/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.pillar2stubs.controllers

import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.error.Origin.{HIP, HoD}
import uk.gov.hmrc.pillar2stubs.models.error.{HIPError, HIPErrorWrapper, HIPFailure}
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions.ObligationsAndSubmissionsErrorCodes._
import uk.gov.hmrc.pillar2stubs.models.obligationsandsubmissions._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.util.Try

class ObligationAndSubmissionsController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter)
    extends BackendController(cc)
    with Logging {

  def retrieveData(fromDate: String, toDate: String): Action[AnyContent] = (Action andThen authFilter) { implicit request =>
    Try {
      val accountingPeriods = ObligationsAndSubmissionsRequest(fromDate = LocalDate.parse(fromDate), toDate = LocalDate.parse(toDate))

      // First check if accounting period is valid, return error if not valid
      if (!accountingPeriods.accountingPeriodValid) {
        UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse.invalidDateRange))
      } else {
        // Continue with other validations and responses only if accounting period is valid
        request.headers.get("X-Pillar2-Id").get match {
          case "XEPLR0200000422" =>
            UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(PILLAR_2_ID_MISSING_OR_INVALID_002)))
          case "XEPLR0300000422" =>
            UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
          case "XEPLR0300000404" =>
            NotFound(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(RECORD_NOT_FOUND_004)))
          case "XEPLR0300000499" =>
            // Wait 20 seconds then return 499 timeout error
            Thread.sleep(20000)
            Result(
              header = ResponseHeader(499),
              body = play.api.http.HttpEntity.Strict(
                ByteString(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(TIMEOUT_ERROR_499)).toString),
                Some("application/json")
              )
            )
          case "XEPLR0400000422" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(DUPLICATE_SUBMISSION_004)))
          case "XEPLR2500000422" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(NO_DATA_FOUND_025)))
          case "XEPLR0000000400" => BadRequest(Json.toJson(HIPErrorWrapper(HIP, HIPFailure(List(HIPError("invalid json", "invalid json"))))))
          case "XEPLR0000000500" => InternalServerError(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.SAPError))
          case "XEPLR1111111111" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withMultipleAccountingPeriods()))
          case "XEPLR2222222222" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()))
          case "XEPLR3333333333" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse()))
          case "XEPLR4444444444" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withAllFulfilled()))
          case "XEPLR4444444445" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withAllFulfilledAndReceived()))
          case "XEPLR5555555555" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withMultipleAccountingPeriodsWithSubmissions()))
          case "XEPLR7777777777" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withOneAccountingPeriodAndOneSubmission()))
          case "XEPLR9999999991" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.singleActiveAccountingPeriodWithNoSubmission()))
          case "XEPLR9999999992" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.twoActiveAccountingPeriodsWithNoSubmissions()))
          case "XEPLR9999999993" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.threeActiveAccountingPeriodsWithDifferentScenarios()))
          case "XEPLR9999999994" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.fourActiveAccountingPeriodsWithDifferentScenarios()))
          case "XEPLR9999999995" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.btnUnderEnquiryScenario()))
          case "XEPLR1066196602" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.twoActiveAccountingPeriodsWithNoSubmissions()))
          case "XMPLR0012345675" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.uktrDueScenario()))
          case "XMPLR0012345676" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario()))
          case "XMPLR0012345677" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.uktrIncompleteScenario()))
          case "XEPLR9999999996" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withEmptyObligations()))
          case "XEPLR0000000504" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withAllFulfilled()))
          // Payment overdue w/o interest, no Return
          case "XEPLR2000000101" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()))
          // Payment overdue w/ interest, no Return
          case "XEPLR2000000102" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()))
          // Payment overdue w/o interest, Return due
          case "XEPLR2000000103" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.uktrDueScenario()))
          // Payment overdue w/ interest, Return overdue
          case "XEPLR2000000104" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario()))
          // Payment overdue w/ interest, Return due
          case "XEPLR2000000105" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse()))
          // Payment overdue w/ interest, Return overdue
          case "XEPLR2000000106" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario()))
          // Payment overdue w/o interest, Return received
          case "XEPLR2000000107" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withAllFulfilledAndReceived()))
          // Payment paid, no Return
          case "XEPLR2000000108" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()))
          // No payments, no Return, BTN
          case "XEPLR2000000109" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()))
          // Payment overdue, Return overdue, BTN
          case "XEPLR2000000110" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.uktrOverdueScenario()))
          // Payment overdue, no Return, BTN
          case "XEPLR2000000111" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()))
          case _                 => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse()))
        }
      }
    }.recover { case e: Throwable =>
      BadRequest(
        Json.toJson(HIPErrorWrapper(HoD, ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError(s"Errors parsing date: ${e.getMessage}")))
      )
    }.get
  }
}
