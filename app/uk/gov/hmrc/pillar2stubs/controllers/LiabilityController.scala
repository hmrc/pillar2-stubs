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

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, BaseController, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.UKTRSubmissionRequest
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class LiabilityController @Inject() (
  val controllerComponents: ControllerComponents,
  authFilter:               AuthActionFilter
)(implicit ec:              ExecutionContext)
    extends BaseController {
  def createLiability(plrReference: String): Action[String] = (Action(parse.tolerantText) andThen authFilter).async { implicit request =>

    if (plrReference != "XTC01234123412") {
      Future.successful(NotFound(Json.obj("error" -> "No liabilities found for the given reference")))
    } else {

      Try(Json.parse(request.body)) match {
        case Success(json) =>
          json.validate[UKTRSubmissionRequest] match {
            case JsSuccess(_, _) =>
              val successResponse = resourceAsString("/resources/liabilities/LiabilitySuccessResponse.json")
                .map(replaceDate(_, LocalDate.now().toString + "T09:26:17Z"))
                .map(Json.parse)
                .getOrElse(Json.obj("error" -> "Success response not found"))
              Future.successful(Created(successResponse).as("application/json"))

            case JsError(_) =>
              Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON request format")).as("application/json"))
          }

        case Failure(_) =>
          Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON data")).as("application/json"))
      }
    }
  }

  private def replaceDate(response: String, registrationDate: String): String =
    response.replace("2022-01-31T09:26:17Z", registrationDate)

}
