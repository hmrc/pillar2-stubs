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

      request.headers.get("X-Pillar2-Id").get match {
        case "XEPLR4220000002" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(PILLAR_2_ID_MISSING_OR_INVALID_002)))
        case "XEPLR4220000003" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
        case "XEPLR4220000004" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(DUPLICATE_SUBMISSION_004)))
        case "XEPLR4220000025" => UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse(NO_DATA_FOUND_025)))
        case "XEPLR4000000000" => BadRequest(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError()))
        case "XEPLR5000000000" => InternalServerError(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.SAPError))
        case _ =>
          if (accountingPeriods.accountingPeriodValid) Ok(Json.toJson(ObligationsAndSubmissionsSuccessResponse()))
          else UnprocessableEntity(Json.toJson(ObligationsAndSubmissionsDetailedErrorResponse.invalidDateRange))
      }
    }.recover { case e: Throwable =>
      BadRequest(Json.toJson(ObligationsAndSubmissionsSimpleErrorResponse.InvalidJsonError(s"Errors parsing date: ${e.getMessage}")))
    }.get
  }
}
