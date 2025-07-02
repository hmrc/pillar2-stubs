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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.GroupIds
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject

class EnrolmentStoreProxyController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) {
  private val badService       = "HMRC-PILLAR2-ORG~PLRID~XEPLR0444444400"
  private val plrServiceEmpty  = "HMRC-PILLAR2-ORG~PLRID~XMPLR0012345674"
  private val plrServiceEmpty2 = "HMRC-PILLAR2-ORG~PLRID~XEPLR0000000002"

  def status(serviceName: String): Action[AnyContent] = (Action andThen authFilter) { _ =>
    serviceName match {
      case `badService` => NoContent
      case `plrServiceEmpty` | `plrServiceEmpty2` =>
        val path = "/resources/groupsES1/enrolment-response-with-no-groupid.json"
        Ok(resourceAsString(path).get)
      case _ =>
        Ok(Json.toJson(GroupIds(principalGroupIds = Seq("879D6270-E9C2-4092-AC91-21C61B69D1E7"), delegatedGroupIds = Seq.empty)))
    }
  }

}
