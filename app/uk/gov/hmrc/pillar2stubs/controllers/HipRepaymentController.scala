/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.Results.Forbidden
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.HipAuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.SendRepaymentDetails
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class HipRepaymentController @Inject() (cc: ControllerComponents, authFilter: HipAuthActionFilter) extends BackendController(cc) with Logging {

  def submitRepaymentDetails: Action[JsValue] = (Action(parse.json) andThen authFilter).async { implicit request =>
    Json.fromJson[SendRepaymentDetails](request.body) match {
      case JsSuccess(repaymentSuccess, _) =>
        repaymentSuccess.bankDetails.nameOnBankAccount.toLowerCase match {
          case "bad person" =>
            Future.successful(NoContent)
          case "forbidden" =>
            Future.successful(Forbidden)
          case "not found" =>
            Future.successful(NotFound)
          case "cannot process" =>
            Future.successful(Status(UNPROCESSABLE_ENTITY) {
              Json.parse(
                """
                    |{
                    |  "errors": {
                    |    "code": "003",
                    |    "processingDate": "2022-01-31T09:26:17Z",
                    |    "text": "Request could not be processed"
                    |  }
                    |}
                    |""".stripMargin
              )
            })
          case "duplicate" =>
            Future.successful(Status(UNPROCESSABLE_ENTITY) {
              Json.parse(
                """
                    |{
                    |  "errors": {
                    |    "code": "004",
                    |    "processingDate": "2022-01-31T09:26:17Z",
                    |    "text": "Duplicate Submission"
                    |  }
                    |}
                    |""".stripMargin
              )
            })
          case "invalid id" =>
            Future.successful(Status(UNPROCESSABLE_ENTITY) {
              Json.parse(
                """
                    |{
                    |  "errors": {
                    |    "code": "016",
                    |    "processingDate": "2022-01-31T09:26:17Z",
                    |    "text": "Invalid ID"
                    |  }
                    |}
                    |""".stripMargin
              )
            })
          case "server error" =>
            Future.successful(Status(INTERNAL_SERVER_ERROR) {
              Json.parse(
                """
                    |{
                    |  "origin": "HoD",
                    |  "response": {
                    |    "error": {
                    |      "code": "500",
                    |      "logID": "D82EBAB67AC6D7565C0682CA91BDC577",
                    |      "message": "string"
                    |    }
                    |  }
                    |}
                    |""".stripMargin
              )
            })
          case "service unavailable" =>
            Future.successful(Status(SERVICE_UNAVAILABLE) {
              Json.parse(
                """
                    |{
                    |  "origin": "HIP",
                    |  "response": {
                    |    "failures": [
                    |      {
                    |        "type": "string",
                    |        "reason": "string"
                    |      }
                    |    ]
                    |  }
                    |}""".stripMargin
              )
            })
          case _ =>
            Future.successful(Created)
        }

      case JsError(_) =>
        Future.successful(Status(BAD_REQUEST) {
          Json.parse(
            """
                |{
                |  "origin": "HoD",
                |  "response": {
                |    "error": {
                |      "code": "400",
                |      "logID": "D82EBAB67AC6D7565C0682CA91BDC577",
                |      "message": "string"
                |    }
                |  }
                |}
                |""".stripMargin
          )
        })
    }
  }
}
