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
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.Subscription
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject

class SubscriptionController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) with Logging {

  def createSubscription: Action[JsValue] = (Action(parse.json) andThen authFilter) { implicit request =>
    val regime = "PIL2"
    println("****************************************************")
    logger.info(s"Request recieved \n ${request.body} \n")
    println("****************************************************")

    request.body.asOpt[Subscription] match {
      case Some(input) =>
        val organisationName = input.organisationName
        val safeId           = input.safeId

        (input.regime, organisationName, safeId) match {
          case (`regime`, "duplicate", _) => Conflict(resourceAsString("/resources/error/DuplicateSubmission.json").get)
          case (`regime`, "server", _)    => ServiceUnavailable(resourceAsString(s"/resources/error/ServiceUnavailable.json").get)
          case (`regime`, "notFound", _)  => NotFound(resourceAsString(s"/resources/error/RecordNotFound.json").get)
          case (`regime`, _, "XE0000123456789") =>
            Ok(resourceAsString("/resources/subscription/SuccessResponse.json").map(replaceSubscriptionId(_, "XMPLR0012345671")).get)
          case (`regime`, _, "XE0000123456788") =>
            Ok(resourceAsString("/resources/subscription/SuccessResponse.json").map(replaceSubscriptionId(_, "XMPLR0012345672")).get)
          case (`regime`, _, "XE0000123456787") =>
            Ok(resourceAsString("/resources/subscription/SuccessResponse.json").map(replaceSubscriptionId(_, "XMPLR0012345673")).get)
          case (`regime`, _, _) =>
            Ok(resourceAsString("/resources/subscription/SuccessResponse.json").map(replaceSubscriptionId(_, "XMPLR0012345674")).get)
          case _ => BadRequest
        }
      case _ => BadRequest
    }
  }

  private def replaceSubscriptionId(response: String, pillar2Reference: String): String =
    response.replace("[pillar2Reference]", pillar2Reference)
}
