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
import play.api.libs.json.*
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.SendRepaymentDetails
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class RepaymentController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) with Logging {

  def submitRepaymentDetails: Action[JsValue] = (Action(parse.json) andThen authFilter).async { implicit request =>
    Json.fromJson[SendRepaymentDetails](request.body) match {
      case JsSuccess(repaymentSuccess, _) =>
        repaymentSuccess.bankDetails.nameOnBankAccount.toLowerCase match {
          case "bad person" =>
            Future.successful(NoContent)
          case _ =>
            Future.successful(Created)
        }

      case JsError(_) =>
        Future.successful(BadRequest)
    }
  }
}
