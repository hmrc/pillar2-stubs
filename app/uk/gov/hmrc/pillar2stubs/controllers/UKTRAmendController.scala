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
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.UKTRSubmissionResponse.{successfulLiabilityResponse, successfulNilResponse}
import uk.gov.hmrc.pillar2stubs.models.error.Origin.{HIP, HoD}
import uk.gov.hmrc.pillar2stubs.models.error.{HIPError, HIPErrorWrapper, HIPFailure}
import uk.gov.hmrc.pillar2stubs.models.{UKTRSubmission, UKTRSubmissionData, UKTRSubmissionNilReturn}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.format.DateTimeFormatter
import java.time.{Clock, ZoneId, ZonedDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class UKTRAmendController @Inject() (
  cc:             ControllerComponents,
  authFilter:     AuthActionFilter
)(implicit clock: Clock)
    extends BackendController(cc)
    with Logging {

  private val TaxObligationMetPlrId = "XEPLR0422044000"
  private val BadRequestPlrId       = "XEPLR0400000000"
  private val ServerErrorPlrId      = "XEPLR0500000000"

  def amendUKTR: Action[String] = (Action(parse.tolerantText) andThen authFilter).async { implicit request =>
    val plrId = request.headers.get("X-Pillar2-Id") match {
      case Some(value) => value
      case None        => throw new RuntimeException("X-Pillar2-Id not provided")
    }

    plrId match {
      case TaxObligationMetPlrId =>
        logger.info(s"Returning tax obligation already met error for PLR ID: $plrId")
        Future.successful(
          UnprocessableEntity(
            Json.obj(
              "errors" -> Json.obj(
                "processingDate" -> ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_INSTANT),
                "code"           -> "044",
                "text"           -> "Tax obligation already met"
              )
            )
          )
        )

      case BadRequestPlrId =>
        logger.info(s"Returning bad request error for PLR ID: $plrId")
        Future.successful(BadRequest(Json.toJson(HIPErrorWrapper(HIP, HIPFailure(List(HIPError("invalid json", "invalid json")))))))

      case ServerErrorPlrId =>
        logger.info(s"Returning server error for PLR ID: $plrId")
        Future.successful(
          InternalServerError(
            Json.obj(
              "error" -> Json.obj(
                "code"    -> "500",
                "message" -> "Internal server error",
                "LogID"   -> "C0000AB8190C8E1F000000C700006836"
              )
            )
          )
        )

      case _ =>
        Try(Json.parse(request.body)) match {
          case Success(json) =>
            json.validate[UKTRSubmission] match {
              case JsSuccess(submission, _) =>
                if (!submission.accountingPeriodValid) {
                  logger.error("Invalid date range: accountingPeriodTo is before accountingPeriodFrom")
                  Future.successful(
                    UnprocessableEntity(
                      Json.obj(
                        "errors" -> Json.obj(
                          "processingDate" -> ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_INSTANT),
                          "code"           -> "001",
                          "text"           -> "Invalid date range: accountingPeriodTo must be after accountingPeriodFrom"
                        )
                      )
                    )
                  )
                } else {
                  submission match {
                    case d @ UKTRSubmissionData(_, _, _, _, _) =>
                      if (d.liabilities.liableEntities.isEmpty) {
                        logger.error("Liable entities array is empty in liability submission")
                        Future.successful(
                          UnprocessableEntity(
                            Json.obj(
                              "errors" -> Json.obj(
                                "processingDate" -> ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT),
                                "code"           -> "002",
                                "text"           -> "Liable entities must not be empty"
                              )
                            )
                          )
                        )
                      } else {
                        logger.info("Returning success response for liability amendment")
                        Future.successful(Ok(successfulLiabilityResponse))
                      }
                    case UKTRSubmissionNilReturn(_, _, _, _, _) =>
                      logger.info("Returning success response for Nil return amendment")
                      Future.successful(Ok(successfulNilResponse))
                  }
                }

              case JsError(errors) =>
                if (errors.exists(_._2.exists(_.messages.contains("liableEntities must not be empty")))) {
                  logger.error("Liable entities array is empty in liability submission")
                  Future.successful(BadRequest(Json.toJson(HIPErrorWrapper(HoD, Json.obj("error" -> "liableEntities must not be empty")))))
                } else {
                  logger.error(s"JSON validation failed with errors: $errors")
                  Future.successful(BadRequest(Json.toJson(HIPErrorWrapper(HIP, HIPFailure(List(HIPError("invalid json", "invalid json")))))))
                }
            }

          case Failure(exception) =>
            logger.error("Failed to parse JSON request body", exception)
            Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON data")).as("application/json"))
        }
    }
  }
}
