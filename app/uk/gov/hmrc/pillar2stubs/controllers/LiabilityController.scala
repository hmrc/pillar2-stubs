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

import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.models.UKTRSubmissionRequest
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LiabilityController @Inject() (val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {

  def createLiability(plrReference: String): Action[AnyContent] = Action.async { implicit request =>
    if (request.contentType.getOrElse("") != "application/json" || request.body.asJson.isEmpty) {
      val invalidJsonResponse = resourceAsString("/resources/liabilities/InvalidJsonResponse.json")
        .map(Json.parse)
        .getOrElse(Json.obj("error" -> "Invalid JSON response not found"))
      Future.successful(BadRequest(invalidJsonResponse).as("application/json"))
    } else {
      plrReference match {
        case "XTC01234123412" =>
          request.body.asJson match {
            case Some(json) =>
              json.validate[UKTRSubmissionRequest] match {
                case JsSuccess(_, _) =>
                  val currentDate = LocalDate.now()
                  val successResponse = resourceAsString("/resources/liabilities/LiabilitySuccessResponse.json")
                    .map(replaceDate(_, currentDate.toString))
                    .map(Json.parse)
                    .getOrElse(Json.obj("error" -> "Success response not found"))
                  Future.successful(Created(successResponse).as("application/json"))

                case JsError(errors) =>
                  val invalidRequestResponse: JsObject = resourceAsString("/resources/liabilities/InvalidRequestResponse.json")
                    .map(Json.parse)
                    .collect { case obj: JsObject => obj }
                    .getOrElse(Json.obj("error" -> "Invalid Request response not found"))
                    .++(Json.obj("details" -> JsError.toJson(errors)))

                  Future.successful(BadRequest(invalidRequestResponse).as("application/json"))
              }

            case None =>
              val invalidJsonResponse = resourceAsString("/resources/liabilities/InvalidJsonResponse.json")
                .map(Json.parse)
                .getOrElse(Json.obj("error" -> "Invalid JSON response not found"))
              Future.successful(BadRequest(invalidJsonResponse).as("application/json"))
          }
        case _ =>
          val notFoundResponse = resourceAsString("/resources/liabilities/LiabilitiesNotFoundResponse.json")
            .map(Json.parse)
            .getOrElse(Json.obj("error" -> "Not Found response not found"))
          Future.successful(NotFound(notFoundResponse).as("application/json"))
      }
    }
  }

  private def replaceDate(response: String, registrationDate: String): String =
    response.replace("2024-01-31", registrationDate)
}
