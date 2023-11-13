/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.{Subscription, SubscriptionResponse}
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.Future

class SubscriptionController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) with Logging {

  def createSubscription: Action[JsValue] = (Action(parse.json) andThen authFilter) { implicit request =>
    logger.info(s"Subscription Request recieved \n ${request.body} \n")

    request.body.asOpt[Subscription] match {
      case Some(input) =>
        val organisationName = input.organisationName
        val safeId           = input.safeId

        (organisationName, safeId) match {
          case ("duplicate", _) => Conflict(resourceAsString("/resources/error/DuplicateSubmission.json").get)
          case ("server", _)    => ServiceUnavailable(resourceAsString(s"/resources/error/ServiceUnavailable.json").get)
          case ("notFound", _)  => NotFound(resourceAsString(s"/resources/error/RecordNotFound.json").get)
          case (_, "XE0000123456789") =>
            Ok(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XMPLR0012345671")).get)
          case (_, _) =>
            Ok(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XMPLR0012345674")).get)
          case _ => BadRequest
        }
      case _ => BadRequest
    }
  }

  def retrieveSubscription(plrReference: String): Action[AnyContent] =
    (Action andThen authFilter).async {
      plrReference match {
        case "400" =>
          Future.successful(BadRequest(resourceAsString(s"/resources/error/BadRequest.json").get))
        case "404" =>
          Future.successful(NotFound(resourceAsString(s"/resources/error/RecordNotFound.json").get))
        case "422" =>
          Future.successful(UnprocessableEntity(resourceAsString(s"/resources/error/RequestCouldNotBeProcessed.json").get))
        case "500" =>
          Future.successful(InternalServerError(resourceAsString(s"/resources/error/InternalServerError.json").get))
        case "503" =>
          Future.successful(ServiceUnavailable(resourceAsString(s"/resources/error/ServiceUnavailable.json").get))
        case _ =>
          resourceAsString("/resources/subscription/ReadSuccessResponse.json") match {
            case Some(responseTemplate) =>
              val responseBody = replacePillar2Id(responseTemplate, plrReference)
              Future.successful(Ok(responseBody))
            case None =>
              Future.successful(InternalServerError("Unable to read ReadSuccessResponse.json"))
          }
      }
    }

  def amendSubscription: Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        Json.fromJson[SubscriptionResponse](json) match {
          case JsSuccess(subscriptionResponse, _) =>
            if (subscriptionResponse.success.upeDetails.organisationName.equals("400")) {
              Future.successful(BadRequest(resourceAsString("/resources/error/BadRequest.json").getOrElse("Bad request error")))
            } else if (subscriptionResponse.success.upeDetails.organisationName.equals("409")) {
              Future.successful(Conflict(resourceAsString("/resources/error/DuplicateSubmission.json").getOrElse("Conflict error")))
            } else if (subscriptionResponse.success.upeDetails.organisationName.equals("422")) {
              Future.successful(
                UnprocessableEntity(resourceAsString("/resources/error/RequestCouldNotBeProcessed.json").getOrElse("Unprocessable entity error"))
              )
            } else if (subscriptionResponse.success.upeDetails.organisationName.equals("500")) {
              Future.successful(InternalServerError(resourceAsString("/resources/error/InternalServerError.json").getOrElse("Internal server error")))
            } else if (subscriptionResponse.success.upeDetails.organisationName.equals("503")) {
              Future.successful(
                ServiceUnavailable(resourceAsString("/resources/error/ServiceUnavailable.json").getOrElse("Service unavailable error"))
              )
            } else {
              Future.successful(Ok(resourceAsString("/resources/subscription/AmendSuccessResponse.json").getOrElse("Success response")))
            }

          case JsError(errors) =>
            Future.successful(BadRequest(Json.toJson(ErrorResponse(400, "Invalid JSON format"))))
        }

      case None =>
        Future.successful(BadRequest(Json.toJson(ErrorResponse(400, "Missing JSON request body"))))
    }
  }

  private def replacePillar2Id(response: String, pillar2Reference: String): String =
    response.replace("[pillar2Reference]", pillar2Reference)

}
