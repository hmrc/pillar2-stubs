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
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.btn._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import javax.inject.Inject

class BTNController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter, etmpHeaderFilter: ETMPHeaderFilter)
    extends BackendController(cc)
    with Logging {

  def submitBTN: Action[BTNRequest] = (Action(parse.json[BTNRequest]) andThen authFilter andThen etmpHeaderFilter) { implicit request =>
    request.headers.get("X-PILLAR2-ID").get match {
      case "XEPLR4220000000" =>
        UnprocessableEntity(Json.toJson(BTNFailureResponsePayload(BTNFailure(LocalDateTime.now(ZoneId.of("UTC")), "094", "Invalid DTT Election"))))
      case "XEPLR4000000000" =>
        BadRequest(Json.toJson(BTNErrorResponse(BTNError("400", "Request could not be processed"))))
      case "XEPLR5000000000" => InternalServerError(Json.toJson(BTNErrorResponse(BTNError("500", "Error in downstream system"))))
      case _ =>
        Created(
          Json.toJson(
            BTNSuccessResponsePayload(
              BTNSuccess(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS), "11223344556677", "XTC01234123412")
            )
          )
        )
    }
  }

}
