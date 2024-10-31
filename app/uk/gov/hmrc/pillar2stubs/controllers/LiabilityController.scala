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

import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.models.{UKTRSubmissionRequest, UKTRSubmissionResponse, UKTRSubmissionSuccess}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import java.time.LocalDateTime

@Singleton
class LiabilityController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {

  def createLiability(): Action[AnyContent] = Action { implicit request =>
    request.body.asJson
      .map { json =>
        json
          .validate[UKTRSubmissionRequest]
          .map { submissionRequest =>
            val response = UKTRSubmissionResponse(
              success = UKTRSubmissionSuccess(
                processingDate = LocalDateTime.now(),
                formBundleNumber = "123456789012345",
                chargeReference = Some("XTC01234123412")
              )
            )
            Created(Json.toJson(response))
          }
          .recoverTotal { _ =>
            BadRequest("Invalid JSON")
          }
      }
      .getOrElse {
        BadRequest("Expecting JSON data")
      }
  }
}
