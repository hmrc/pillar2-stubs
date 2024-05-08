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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.utils.ResourceHelper.resourceAsString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject

class TaxEnrolmentController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) with Logging {

  private val badGroupID = "0000"

  def allocate(groupID: String, serviceName: String): Action[AnyContent] = (Action andThen authFilter) { _ =>
    groupID match {
      case `badGroupID` =>
        val path = "/resources/taxEnrolmentES8/tax-enrolment-failure.json"
        Ok(resourceAsString(path).get)
      case _ =>
        Created
    }
  }

  def revoke(groupID: String, serviceName: String): Action[AnyContent] = (Action andThen authFilter) { _ =>
    groupID match {
      case `badGroupID` =>
        val path = "/resources/taxEnrolmentES8/tax-enrolment-failure.json"
        Ok(resourceAsString(path).get)
      case _ => NoContent
    }
  }

}
