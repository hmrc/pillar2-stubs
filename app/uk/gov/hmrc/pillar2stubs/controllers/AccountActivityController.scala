/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.accountactivity.AccountActivityErrorCodes.*
import uk.gov.hmrc.pillar2stubs.models.accountactivity.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.util.Try

class AccountActivityController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter, etmpHeaderFilter: ETMPHeaderFilter)
    extends BackendController(cc)
    with Logging {

  def retrieveAccountActivity(fromDate: String, toDate: String): Action[AnyContent] =
    (Action andThen authFilter andThen etmpHeaderFilter) { implicit request =>
      Try {
        val accountActivityRequest = AccountActivityRequest(fromDate = LocalDate.parse(fromDate), toDate = LocalDate.parse(toDate))

        // First check if date range is valid, return error if not valid
        if !accountActivityRequest.dateRangeValid then {
          UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
        } else {
          // Continue with other validations and responses only if date range is valid
          // ETMPHeaderFilter validates "x-pillar2-id" exists, and Play normalizes headers to lowercase
          val pillar2Id = request.headers.get("x-pillar2-id").getOrElse("")
          logger.info(s"Account Activity - Pillar2 ID received: '$pillar2Id' (length: ${pillar2Id.length})")
          pillar2Id match {
            case "XEPLR0000000400" =>
              BadRequest(Json.toJson(AccountActivityErrorResponse.badRequest))
            case "XEPLR0000000500" =>
              InternalServerError(Json.toJson(AccountActivityErrorResponse.internalServerError))
            case "XEPLR0000000422_001" =>
              UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REGIME_MISSING_OR_INVALID_001)))
            case "XEPLR0000000422_003" =>
              UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
            case "XEPLR0000000422_014" =>
              UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(NO_DATA_FOUND_014)))
            case "XEPLR0000000422_089" =>
              UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(ID_NUMBER_MISSING_OR_INVALID_089)))
            case _ =>
              Ok(Json.toJson(AccountActivitySuccessResponse()))
          }
        }
      }.recover { case e: Throwable =>
        BadRequest(Json.toJson(AccountActivityErrorResponse(
          AccountActivityError(
            code = "400",
            message = s"Invalid date format: ${e.getMessage}",
            logID = "1D43D17801EBCC4C4EAB8974C05448D9"
          )
        )))
      }.get
    }
}

