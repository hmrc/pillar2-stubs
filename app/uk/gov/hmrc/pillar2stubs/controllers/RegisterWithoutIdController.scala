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

import play.api.i18n.Lang.logger
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.Registration
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.util.Random

class RegisterWithoutIdController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) {

  def registerWithoutId: Action[JsValue] = (Action(parse.json) andThen authFilter) { implicit request =>
    logger.info(s"Registration Request recieved \n ${request.body} \n")
    val regimeName = "PLR"
    val register   = request.body.as[Registration]
    val orgName    = register.organisation.organisationName

    (register.regime, orgName) match {
      case (`regimeName`, "regNoIDTimeoutIssue") =>
        Thread.sleep(30.seconds.toMillis)
        InternalServerError("failed")
      case (`regimeName`, "regNoIDInternalError")     => InternalServerError(resourceAsString(s"/resources/error/InternalServerError.json").get)
      case (`regimeName`, "regNoIDInvalidRequest")    => BadRequest(resourceAsString(s"/resources/error/BadRequest.json").get)
      case (`regimeName`, "regNoIDServerError")       => ServiceUnavailable(resourceAsString(s"/resources/error/ServiceUnavailable.json").get)
      case (`regimeName`, "regNoIDNotProcessed")      => ServiceUnavailable(resourceAsString(s"/resources/error/RequestCouldNotBeProcessed.json").get)
      case (`regimeName`, "regNoIDRecordNotFound")    => NotFound(resourceAsString(s"/resources/error/RecordNotFound.json").get)
      case (`regimeName`, "regNoIDInvalidSubmission") => Forbidden(resourceAsString(s"/resources/error/Forbidden.json").get)
      case (`regimeName`, data) =>
        val safeId = data match {
          case "duplicate"    => "XD3333333333333"
          case "enrolment"    => "XE4444444444444"
          case "organisation" => "XE5555555555555"
          case _              => generateSafeId
        }
        resourceAsString(s"/resources/register/withoutIdResponse.json") match {
          case Some(response) => Ok(response.replace("[safeId]", safeId))

          case _ => NotFound
        }
      case _ => BadRequest
    }
  }

  private def generateSafeId: String = {
    val random        = new Random()
    val randomInteger = (0 to 12).map(_ => random.between(0, 9)).mkString

    s"XE$randomInteger"
  }

}
