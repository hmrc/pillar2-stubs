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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class UserCacheController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) with Logging {

  private val subscriptionCache = new ConcurrentHashMap[String, String]()

  def getReadSubscription(userId: String): Action[AnyContent] = (Action andThen authFilter) { _ =>
    Option(subscriptionCache.get(userId)) match {
      case Some(json) => Ok(Json.parse(json))
      case None       => Ok(Json.obj())
    }
  }

  def putReadSubscription(userId: String): Action[JsValue] = (Action(parse.json) andThen authFilter) { implicit request =>
    subscriptionCache.put(userId, request.body.toString())
    logger.info(s"UserCacheController: stored read-subscription for userId=$userId")
    Ok(Json.obj())
  }
}
