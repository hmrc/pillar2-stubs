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

import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
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
        val pillar2Id = request.headers.get("X-Pillar2-Id").getOrElse("")

        pillar2Id match {
          case "" =>
            BadRequest(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError("Missing X-Pillar2-Id header")))
          case "XEPLR0200000422" =>
            UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(PILLAR_2_ID_MISSING_OR_INVALID_002)))
          case "XEPLR0300000422" =>
            UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
          case "XEPLR0400000422" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(DUPLICATE_SUBMISSION_004)))
          case "XEPLR2500000422" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(NO_DATA_FOUND_025)))
          case "XEPLR0000000400" => BadRequest(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError()))
          case "XEPLR0000000500" => InternalServerError(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.SAPError))
          case "XEPLR1111111111" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withMultipleAccountingPeriods()))
          case "XEPLR2222222222" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse.withNoAccountingPeriods()))
          case "XEPLR3333333333" => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse()))
          case _                 => Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse()))
        }
      }
    }.recover { case e: Throwable =>
      BadRequest(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError(s"Errors parsing date: ${e.getMessage}")))
    }.get
  }
}
