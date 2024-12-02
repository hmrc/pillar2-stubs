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
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.UKTRSubmissionRequest
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class LiabilityController @Inject() (
  cc:         ControllerComponents,
  authFilter: AuthActionFilter
) extends BackendController(cc)
    with Logging {

  def submitUktr(plrReference: String): Action[String] = (Action(parse.tolerantText) andThen authFilter).async { implicit request =>
    if (plrReference != "XTC01234123412") {
      logger.warn(s"Invalid PLR Reference provided: $plrReference")
      Future.successful(NotFound(Json.obj("error" -> "No liabilities found for the given reference")))
    } else {
      Try(Json.parse(request.body)) match {
        case Success(json) =>
          json.validate[UKTRSubmissionRequest] match {
            case JsSuccess(uktrRequest, _) =>
              if (!uktrRequest.isValid) {
                logger.error("Invalid date range: accountingPeriodTo is before accountingPeriodFrom")
                Future.successful(BadRequest(Json.obj("error" -> "Invalid date range: accountingPeriodTo must be after accountingPeriodFrom")))
              } else if (uktrRequest.liabilities.returnType.contains("NIL_RETURN")) {
                val nilReturnResponse = resourceAsString("/resources/liabilities/NilReturnSuccessResponse.json")
                  .map(replaceDate(_, LocalDate.now().toString + "T09:26:17Z"))
                  .map(Json.parse)
                  .getOrElse(Json.obj("error" -> "Nil return response not found"))

                logger.info("Returning success response for Nil return request")
                Future.successful(Created(nilReturnResponse).as("application/json"))
              } else if (uktrRequest.liabilities.liableEntities.exists(_.isEmpty)) {
                logger.error("Liable entities array is empty in liability submission")
                Future.successful(BadRequest(Json.obj("error" -> "liableEntities must not be empty")).as("application/json"))
              } else {
                val liabilitySuccessResponse = resourceAsString("/resources/liabilities/LiabilitySuccessResponse.json")
                  .map(replaceDate(_, LocalDate.now().toString + "T09:26:17Z"))
                  .map(Json.parse)
                  .getOrElse(Json.obj("error" -> "Success response not found"))

                logger.info("Returning success response for liability request")
                Future.successful(Created(liabilitySuccessResponse).as("application/json"))
              }

            case JsError(errors) =>
              if (errors.exists(_._2.exists(_.messages.contains("liableEntities must not be empty")))) {
                logger.error("Liable entities array is empty in liability submission")
                Future.successful(BadRequest(Json.obj("error" -> "liableEntities must not be empty")).as("application/json"))
              } else {
                logger.error(s"JSON validation failed with errors: $errors")
                Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON request format")).as("application/json"))
              }
          }

        case Failure(exception) =>
          logger.error("Failed to parse JSON request body", exception)
          Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON data")).as("application/json"))
      }
    }
  }

  private def replaceDate(response: String, registrationDate: String): String =
    response.replace("2022-01-31T09:26:17Z", registrationDate)
}
