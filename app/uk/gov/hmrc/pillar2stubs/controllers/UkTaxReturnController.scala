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
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import java.time.ZonedDateTime
import java.time.ZoneOffset
import play.api.mvc.Result
import uk.gov.hmrc.pillar2stubs.models.UKTRSubmissionRequest

@Singleton
class UkTaxReturnController @Inject() (
  cc:         ControllerComponents,
  authFilter: AuthActionFilter
) extends BackendController(cc)
    with Logging {

  def submitUktr: Action[UKTRSubmissionRequest] = (Action(parse.json[UKTRSubmissionRequest]) andThen authFilter).async { implicit request =>
    request.headers.get("X-Pillar2-Id") match {
      case None =>
        logger.warn("No PLR Reference provided in headers")
        returnErrorResponse("uktaxreturn/MissingPLRResponse.json")

      case Some(plrReference) => handleSubmission(plrReference)
    }
  }

  private def handleSubmission(plrReference: String): Future[Result] =
    plrReference match {
      case "XTC01234123412"  => returnSuccessResponse("uktaxreturn/SuccessResponse.json")
      case "XEPLR1066196400" => returnErrorResponse("uktaxreturn/InvalidRequestResponse.json")
      case _ =>
        returnSuccessResponse("uktaxreturn/SuccessResponse.json")
    }

  private def returnSuccessResponse(filename: String): Future[Result] = {
    val response = resourceAsString(s"/resources/$filename")
      .map(replaceDate(_, getCurrentTimestamp))
      .map(Json.parse)
      .getOrElse(Json.obj("error" -> "Response not found"))

    logger.info(s"Returning success response for $filename")
    Future.successful(Created(response).as("application/json"))
  }

  private def returnErrorResponse(filename: String): Future[Result] = {
    val response = resourceAsString(s"/resources/$filename")
      .map(replaceDate(_, getCurrentTimestamp))
      .map(Json.parse)
      .getOrElse(Json.obj("error" -> "Error response not found"))

    Future.successful(BadRequest(response))
  }

  private def getCurrentTimestamp: String =
    ZonedDateTime.now(ZoneOffset.UTC).toString

  private def replaceDate(response: String, newDate: String): String =
    response.replace("2022-01-31T09:26:17Z", newDate)
}
