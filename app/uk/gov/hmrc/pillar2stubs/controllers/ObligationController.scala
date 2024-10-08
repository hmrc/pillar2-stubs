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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.obligation.ObligationInformation
import uk.gov.hmrc.pillar2stubs.models.obligation.ObligationStatus._
import uk.gov.hmrc.pillar2stubs.models.obligation.ObligationType._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject

class ObligationController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) {

  // For now this API has not been developed yet by ETMP. Therefore this will just return dummy data to satisfy happy path scenarios based on what we expect to receive
  // The only field we care about is status, as this will determine which page they are forwarded to
  def getObligation(plrReference: String, dateFrom: String, dateTo: String): Action[AnyContent] = (Action andThen authFilter) { implicit request =>
    plrReference match {
      case "XEPLR1000000000" =>
        Ok(
          Json.toJson(
            ObligationInformation(
              obligationType = UKTR,
              status = Fulfilled,
              accountingPeriodFromDate = LocalDate.parse(dateFrom),
              accountingPeriodToDate = LocalDate.parse(dateTo),
              dueDate = LocalDate.now.plusMonths(10)
            )
          )
        )

      case "XEPLR4040000000" => NotFound
      case _ =>
        Ok(
          Json.toJson(
            ObligationInformation(
              obligationType = UKTR,
              status = Open,
              accountingPeriodFromDate = LocalDate.parse(dateFrom),
              accountingPeriodToDate = LocalDate.parse(dateTo),
              dueDate = LocalDate.now.plusMonths(10)
            )
          )
        )

    }
  }

}
