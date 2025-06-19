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
import uk.gov.hmrc.pillar2stubs.models.{AmendSubscriptionSuccess, Subscription}
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

class SubscriptionController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) with Logging {

  // Simple in-memory counter for registration in progress testing
  private val pollCounters = scala.collection.mutable.Map[String, Int]()

  def createSubscription: Action[JsValue] = (Action(parse.json) andThen authFilter) { implicit request =>
    logger.info(s"Subscription Request received \n ${request.body} \n")

    request.body.asOpt[Subscription] match {
      case Some(input) =>
        val organisationName = input.organisationName
        val safeId           = input.safeId
        val upeContactName   = input.upeContactName.getOrElse("")

        // Check UPE contact details first, then fall back to company name/safeId
        (upeContactName, organisationName, safeId) match {
          // Registration in progress test cases - return specific PLR references based on UPE contact details
          case ("Quick Processing", _, _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XEPLR0000000001")).get)
          case ("Medium Processing", _, _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XEPLR0000000002")).get)
          case ("Timeout Processing", _, _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XEPLR0000000003")).get)
          
          // Original company name test cases - return specific PLR references based on company name  
          case (_, "Quick Processing Corp", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XEPLR0000000001")).get)
          case (_, "Medium Processing Corp", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XEPLR0000000002")).get)
          case (_, "Timeout Processing Corp", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XEPLR0000000003")).get)

          // Existing test cases
          case (_, "duplicateSub", _) =>
            Conflict(resourceAsString("/resources/error/subscription/Conflict.json").get)
          case (_, "unprocessableSub", _) =>
            UnprocessableEntity(resourceAsString("/resources/error/subscription/CannotCompleteRequest.json").get)
          case (_, "subServerError", _) =>
            ServiceUnavailable(resourceAsString("/resources/error/subscription/ServiceUnavailable.json").get)
          case (_, "subRecordNotFound", _) =>
            NotFound(resourceAsString("/resources/error/subscription/NotFound.json").get)
          case (_, "subReqNotProcessed", _) =>
            UnprocessableEntity(resourceAsString("/resources/error/subscription/UnprocessableEntity.json").get)
          case (_, "subInvalidRequest", _) =>
            BadRequest(resourceAsString("/resources/error/subscription/BadRequest.json").get)

          case (_, "XE0000123456400", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XE0000123456400")).get)
          case (_, "XE0000123456404", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XE0000123456404")).get)
          case (_, "XE0000123456422", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XE0000123456422")).get)
          case (_, "XE0000123456500", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XE0000123456500")).get)
          case (_, "XE0000123456503", _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XE0000123456503")).get)
          case (_, _, "XE0000123456789") =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XMPLR0012345671")).get)
          case (_, "XMPLR0009999999", _) =>
            Conflict(resourceAsString("/resources/error/subscription/Conflict.json").map(replacePillar2Id(_, "XMPLR0009999999")).get)
          case (_, _, _) =>
            Created(resourceAsString("/resources/subscription/SuccessResponse.json").map(replacePillar2Id(_, "XMPLR0012345674")).get)
          case _ => BadRequest(resourceAsString("/resources/error/subscription/BadRequest.json").get)
        }
      case _ => BadRequest(resourceAsString("/resources/error/subscription/BadRequest.json").get)
    }
  }

  def retrieveSubscription(plrReference: String): Action[AnyContent] =
    (Action andThen authFilter).async {
      logger.info(s"retrieveSubscription Request received \n $plrReference \n")
      plrReference match {
        // Registration in progress test PLR references with polling behavior
        case "XEPLR0000000001" => // Quick processing - succeeds after 3 polls (6 seconds)
          val count = pollCounters.getOrElseUpdate(plrReference, 0) + 1
          pollCounters(plrReference) = count
          logger.info(s"Quick Processing Corp - Poll attempt $count for $plrReference")
          if (count <= 3) {
            Future.successful(NotFound(resourceAsString("/resources/error/subscription/NotFound.json").get))
          } else {
            Future.successful(
              Ok(
                resourceAsString("/resources/subscription/ReadSuccessResponse.json")
                  .map(replacePillar2Id(_, plrReference))
                  .get
              )
            )
          }

        case "XEPLR0000000002" => // Medium processing - succeeds after 8 polls (16 seconds)
          val count = pollCounters.getOrElseUpdate(plrReference, 0) + 1
          pollCounters(plrReference) = count
          logger.info(s"Medium Processing Corp - Poll attempt $count for $plrReference")
          if (count <= 8) {
            Future.successful(NotFound(resourceAsString("/resources/error/subscription/NotFound.json").get))
          } else {
            Future.successful(
              Ok(
                resourceAsString("/resources/subscription/ReadSuccessResponse.json")
                  .map(replacePillar2Id(_, plrReference))
                  .get
              )
            )
          }

        case "XEPLR0000000003" => // Timeout scenario - always returns 422 (processing)
          logger.info(s"Timeout Processing Corp - Always processing for $plrReference")
          Future.successful(InternalServerError(resourceAsString("/resources/error/subscription/ServerError.json").get))

        // Existing test cases
        case "XEPLR0123456400" =>
          Future.successful(BadRequest(resourceAsString("/resources/error/subscription/BadRequest.json").get))
        case "XEPLR0123456404" =>
          Future.successful(NotFound(resourceAsString("/resources/error/subscription/NotFound.json").get))
        case "XEPLR0123456422" =>
          Future.successful(NotFound(resourceAsString("/resources/error/subscription/NotFound.json").get))
        case "XEPLR0123456500" =>
          Future.successful(InternalServerError(resourceAsString("/resources/error/subscription/ServerError.json").get))
        case "XEPLR0123456503" =>
          Future.successful(ServiceUnavailable(resourceAsString("/resources/error/subscription/ServiceUnavailable.json").get))
        case "XEPLR5555555555" =>
          Future.successful(
            Ok(
              resourceAsString("/resources/subscription/ReadSuccessResponse.json")
                .map(replacePillar2Id(_, "XMPLR0012345674"))
                .map(_.replace("\"inactive\": false", "\"inactive\": true"))
                .get
            )
          )

        case "XEPLR5555551111" =>
          val currentDate = LocalDate.now()

          Future.successful(
            Ok(
              resourceAsString("/resources/subscription/ReadSuccessResponse.json")
                .map(replaceDate(_, currentDate.toString))
                .get
            )
          )

        case "XEPLR6666666666" =>
          Future.successful(
            Ok(
              resourceAsString("/resources/subscription/ReadSuccessResponse.json")
                .map(replacePillar2Id(_, "XEPLR6666666666"))
                .map(_.replace("\"registrationDate\": \"2024-01-31\"", "\"registrationDate\": \"2011-01-31\""))
                .get
            )
          )

        case "XEPLR1066196600" =>
          Future.successful(
            Ok(
              resourceAsString("/resources/subscription/ReadSuccessResponse.json")
                .map(replacePillar2Id(_, "XEPLR1066196600"))
                .map(_.replace("\"domesticOnly\": false", "\"domesticOnly\": true"))
                .get
            )
          )

        case "XEPLR1066196602" =>
          Future.successful(
            Ok(
              resourceAsString("/resources/subscription/ReadSuccessResponse.json")
                .map(replacePillar2Id(_, "XEPLR1066196602"))
                .map(_.replace("\"domesticOnly\": false", "\"domesticOnly\": true"))
                .get
            )
          )

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

  def amendSubscription: Action[JsValue] = (Action(parse.json) andThen authFilter).async { implicit request =>
    logger.info(s"amendSubscription Request recieved")
    Json.fromJson[AmendSubscriptionSuccess](request.body) match {
      case JsSuccess(subscriptionResponse, _) =>
        subscriptionResponse.primaryContactDetails.name match {
          case "400" =>
            Future.successful(BadRequest(resourceAsString("/resources/error/BadRequest.json").getOrElse("Bad request error")))

          case "409" =>
            Future.successful(Conflict(resourceAsString("/resources/error/DuplicateSubmission.json").getOrElse("Conflict error")))

          case "422" =>
            Future.successful(
              UnprocessableEntity(resourceAsString("/resources/error/UnprocessableEntity.json").getOrElse("Unprocessable entity error"))
            )

          case "500" =>
            Future.successful(InternalServerError(resourceAsString("/resources/error/InternalServerError.json").getOrElse("Internal server error")))

          case "503" =>
            Future.successful(ServiceUnavailable(resourceAsString("/resources/error/ServiceUnavailable.json").getOrElse("Service unavailable error")))

          case "timeout" =>
            Thread.sleep(30000)
            Future.successful(Ok(resourceAsString("/resources/subscription/AmendSuccessResponse.json").getOrElse("Success response")))

          case "10 seconds" =>
            Thread.sleep(10000)
            Future.successful(Ok(resourceAsString("/resources/subscription/AmendSuccessResponse.json").getOrElse("Success response")))

          case _ =>
            Future.successful(Ok(resourceAsString("/resources/subscription/AmendSuccessResponse.json").getOrElse("Success response")))
        }

      case JsError(_) =>
        Future.successful(BadRequest(Json.toJson(ErrorResponse(400, "Invalid JSON format"))))
    }
  }

  private def replacePillar2Id(response: String, pillar2Reference: String): String =
    response.replace("[pillar2Reference]", pillar2Reference)

  private def replaceDate(response: String, registrationDate: String): String =
    response.replace("2024-01-31", registrationDate)
}
