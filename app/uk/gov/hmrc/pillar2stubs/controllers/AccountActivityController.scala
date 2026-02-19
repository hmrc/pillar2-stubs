/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.AccountActivityController.generateSuccessfulResponse
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.accountactivity.AccountActivityErrorCodes.*
import uk.gov.hmrc.pillar2stubs.models.accountactivity.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.util.{Random, Try}
import scala.util.matching.Regex

class AccountActivityController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter, etmpHeaderFilter: ETMPHeaderFilter)
    extends BackendController(cc)
    with Logging {

  def retrieveAccountActivityReport: Action[AnyContent] =
    (Action andThen authFilter andThen etmpHeaderFilter) { implicit request =>
      val yearsAndTransactionPattern: Regex = "XMPLR0000000(.{3})\\s*$".r
      val fromDate = request.getQueryString("fromDate").orElse(request.getQueryString("dateFrom"))
      val toDate   = request.getQueryString("toDate").orElse(request.getQueryString("dateTo"))

      (fromDate, toDate) match {
        case (Some(from), Some(to)) =>
          if !request.headers.get("X-Message-Type").contains("ACCOUNT_ACTIVITY") then {
            BadRequest(Json.toJson(AccountActivityErrorResponse.badRequest))
          } else {
            Try {
              AccountActivityRequest(fromDate = LocalDate.parse(from), toDate = LocalDate.parse(to))
            }.fold(
              error =>
                BadRequest(
                  Json.toJson(
                    AccountActivityErrorResponse(
                      AccountActivityError(
                        code = "400",
                        message = s"Invalid date format: ${error.getMessage}",
                        logID = "1D43D17801EBCC4C4EAB8974C05448D9"
                      )
                    )
                  )
                ),
              accountActivityRequest =>
                if !accountActivityRequest.dateRangeValid then {
                  UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
                } else {
                  // Continue with other validations and responses only if date range is valid
                  // ETMPHeaderFilter validates "x-pillar2-id" exists, and Play normalizes headers to lowercase
                  val pillar2Id = request.headers.get("x-pillar2-id").getOrElse("")
                  logger.info(s"Account Activity - Pillar2 ID received: $pillar2Id")
                  pillar2Id match {
                    case AccountActivitySuccessResponse.Scenario1UktrCharges =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario1UktrCharges()))
                    case AccountActivitySuccessResponse.Scenario2UktrInterestCharges =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario2UktrInterestCharges()))
                    case AccountActivitySuccessResponse.Scenario3DeterminationCharges =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario3DeterminationCharges()))
                    case AccountActivitySuccessResponse.Scenario4DiscoveryAssessmentCharges =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario4DiscoveryAssessmentCharges()))
                    case AccountActivitySuccessResponse.Scenario5OverpaidClaimAssessment =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario5OverpaidClaimAssessmentCharges()))
                    case AccountActivitySuccessResponse.Scenario7UktrLateFilingPenalties =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario7UktrLateFilingPenalties()))
                    case AccountActivitySuccessResponse.Scenario8OrnGirLateFilingPenalties =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario8OrnGirLateFilingPenalties()))
                    case AccountActivitySuccessResponse.Scenario9PotentialLostRevenue =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario9PotentialLostRevenuePenalty()))
                    case AccountActivitySuccessResponse.Scenario10Schedule36 =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario10Schedule36()))
                    case AccountActivitySuccessResponse.Scenario11RecordKeeping =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario11RecordKeeping()))
                    case AccountActivitySuccessResponse.Scenario12DeterminationWithInterest =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario12DeterminationWithInterest()))
                    case AccountActivitySuccessResponse.Scenario13DiscAssmtWithInterest =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario13DiscAssmtWithInterest()))
                    case AccountActivitySuccessResponse.Scenario14OverpaidClaimAssmtWithInterest =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.scenario14OverpaidClaimAssmtWithInterest()))
                    case "XEPLR0000422001" =>
                      UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REGIME_MISSING_OR_INVALID_001)))
                    case "XEPLR0000422003" =>
                      UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
                    case "XEPLR0000422014" =>
                      UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(NO_DATA_FOUND_014)))
                    case "XEPLR0000422089" =>
                      UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(ID_NUMBER_MISSING_OR_INVALID_089)))
                    case "XEPLR0000000400" =>
                      BadRequest(Json.toJson(AccountActivityErrorResponse.badRequest))
                    case "XEPLR0000000500" =>
                      InternalServerError(Json.toJson(AccountActivityErrorResponse.internalServerError))
                    // No data found (match ETMP behaviour)
                    case "XMPLR0000000000" =>
                      UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(NO_DATA_FOUND_014)))
                    case "XEPLR4040000000" =>
                      UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(NO_DATA_FOUND_014)))
                    case "XEPLR2000000001" =>
                      Ok(Json.toJson(AccountActivitySuccessResponse.overdueOutstandingCharge()))
                    case yearsAndTransactionPattern(numberOfTransactions) =>
                      Ok(
                        Json.toJson(
                          generateSuccessfulResponse(numberOfTransactions.toInt, LocalDate.parse(fromDate.get), LocalDate.parse(toDate.get))
                        )
                      )
                    case _ =>
                      Ok(Json.toJson(AccountActivitySuccessResponse()))
                  }
                }
            )
          }
        case _ => BadRequest(Json.toJson(AccountActivityErrorResponse.badRequest))
      }
    }

  def retrieveAccountActivity(fromDate: String, toDate: String): Action[AnyContent] =
    (Action andThen authFilter andThen etmpHeaderFilter) { implicit request =>
      process(fromDate, toDate)
    }

  private def process(fromDate: String, toDate: String)(implicit request: play.api.mvc.Request[?]): Result =
    if !request.headers.get("X-Message-Type").contains("ACCOUNT_ACTIVITY") then {
      BadRequest(Json.toJson(AccountActivityErrorResponse.badRequest))
    } else {
      Try {
        AccountActivityRequest(fromDate = LocalDate.parse(fromDate), toDate = LocalDate.parse(toDate))
      }.fold(
        error =>
          BadRequest(
            Json.toJson(
              AccountActivityErrorResponse(
                AccountActivityError(
                  code = "400",
                  message = s"Invalid date format: ${error.getMessage}",
                  logID = "1D43D17801EBCC4C4EAB8974C05448D9"
                )
              )
            )
          ),
        accountActivityRequest =>
          if !accountActivityRequest.dateRangeValid then {
            UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
          } else {
            // Continue with other validations and responses only if date range is valid
            // ETMPHeaderFilter validates "x-pillar2-id" exists, and Play normalizes headers to lowercase
            val pillar2Id = request.headers.get("x-pillar2-id").getOrElse("")
            val yearsAndTransactionPattern: Regex = "XMPLR0000000(.{3})\\s*$".r
            logger.info(s"Account Activity - Pillar2 ID received: $pillar2Id")
            pillar2Id match {
              case AccountActivitySuccessResponse.Scenario1UktrCharges =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario1UktrCharges()))
              case AccountActivitySuccessResponse.Scenario2UktrInterestCharges =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario2UktrInterestCharges()))
              case AccountActivitySuccessResponse.Scenario3DeterminationCharges =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario3DeterminationCharges()))
              case AccountActivitySuccessResponse.Scenario4DiscoveryAssessmentCharges =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario4DiscoveryAssessmentCharges()))
              case AccountActivitySuccessResponse.Scenario5OverpaidClaimAssessment =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario5OverpaidClaimAssessmentCharges()))
              case AccountActivitySuccessResponse.Scenario7UktrLateFilingPenalties =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario7UktrLateFilingPenalties()))
              case AccountActivitySuccessResponse.Scenario8OrnGirLateFilingPenalties =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario8OrnGirLateFilingPenalties()))
              case AccountActivitySuccessResponse.Scenario9PotentialLostRevenue =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario9PotentialLostRevenuePenalty()))
              case AccountActivitySuccessResponse.Scenario10Schedule36 =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario10Schedule36()))
              case AccountActivitySuccessResponse.Scenario11RecordKeeping =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario11RecordKeeping()))
              case AccountActivitySuccessResponse.Scenario12DeterminationWithInterest =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario12DeterminationWithInterest()))
              case AccountActivitySuccessResponse.Scenario13DiscAssmtWithInterest =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario13DiscAssmtWithInterest()))
              case AccountActivitySuccessResponse.Scenario14OverpaidClaimAssmtWithInterest =>
                Ok(Json.toJson(AccountActivitySuccessResponse.scenario14OverpaidClaimAssmtWithInterest()))
              case "XEPLR0000422001" =>
                UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REGIME_MISSING_OR_INVALID_001)))
              case "XEPLR0000422003" =>
                UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(REQUEST_COULD_NOT_BE_PROCESSED_003)))
              case "XEPLR0000422014" =>
                UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(NO_DATA_FOUND_014)))
              case "XEPLR0000422089" =>
                UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(ID_NUMBER_MISSING_OR_INVALID_089)))
              case "XEPLR0000000400" =>
                BadRequest(Json.toJson(AccountActivityErrorResponse.badRequest))
              case "XEPLR0000000500" =>
                InternalServerError(Json.toJson(AccountActivityErrorResponse.internalServerError))
              // No data found (match ETMP behaviour)
              case "XMPLR0000000000" =>
                UnprocessableEntity(Json.toJson(AccountActivity422ErrorResponse(NO_DATA_FOUND_014)))
              case "XEPLR2000000001" =>
                Ok(Json.toJson(AccountActivitySuccessResponse.overdueOutstandingCharge()))
              case "XEPLR4040000000" =>
                Ok(Json.toJson(AccountActivitySuccessResponse.emptyAccountActivity()))
              case yearsAndTransactionPattern(numberOfTransactions) =>
                Ok(
                  Json.toJson(
                    AccountActivitySuccessResponse(success =
                      generateSuccessfulResponse(numberOfTransactions.toInt, LocalDate.parse(fromDate), LocalDate.parse(toDate))
                    )
                  )
                )
              case _ =>
                Ok(Json.toJson(AccountActivitySuccessResponse()))
            }
          }
      )
    }
}

object AccountActivityController {

  private def generateAccountActivityTransactions(
    numberOfTransactions: Int,
    fromDate:             LocalDate,
    toDate:               LocalDate
  ): Seq[AccountActivityTransactionDetail] = {
    require(numberOfTransactions > 0, "Number of account activity transactions must be positive")

    val paymentsCount:         Int = math.max(1, math.min(numberOfTransactions, numberOfTransactions / 3)) // at least 1 Payment
    val remainingTransactions: Int = numberOfTransactions - paymentsCount
    val repaymentsCount: Int = if remainingTransactions > 0 then math.max(1, remainingTransactions / 2) else 0 // at least one Repayment if possible
    val interestRepaymentsCount: Int = math.max(0, numberOfTransactions - paymentsCount - repaymentsCount) // Remaining transactions are Interest

    val payments: Seq[AccountActivityTransactionDetail] = (1 to paymentsCount).map(_ =>
      AccountActivityTransactionDetail(
        transactionType = "PAYMENT",
        transactionDesc = "Pillar 2 Payment on Account",
        transactionDate = generateRandomDate(fromDate, toDate),
        originalAmount = -generateBigDecimal,
        outstandingAmount = Some(-generateBigDecimal),
        clearedAmount = Some(-generateBigDecimal),
        clearingDetails = Some(Seq.empty)
      )
    )

    val repayments: Seq[AccountActivityTransactionDetail] = (1 to repaymentsCount).map(_ =>
      AccountActivityTransactionDetail(
        transactionType = "PAYMENT",
        transactionDesc = "Pillar 2 Payment on Account",
        transactionDate = generateRandomDate(fromDate, toDate),
        originalAmount = -generateBigDecimal,
        clearedAmount = Some(-generateBigDecimal),
        clearingDetails = Some(
          Seq(
            ClearingDetail(
              transactionDesc = "Repayment",
              amount = generateBigDecimal,
              clearingDate = generateRandomDate(fromDate, toDate),
              clearingReason = Some("Outgoing payment - Paid")
            )
          )
        )
      )
    )

    val repaymentInterests: Seq[AccountActivityTransactionDetail] = (1 to interestRepaymentsCount).map(_ =>
      AccountActivityTransactionDetail(
        transactionType = "CREDIT",
        transactionDesc = "Repayment interest - UKTR",
        transactionDate = generateRandomDate(fromDate, toDate),
        originalAmount = -generateBigDecimal,
        clearedAmount = Some(-generateBigDecimal),
        clearingDetails = Some(
          Seq(
            ClearingDetail(
              transactionDesc = "Repayment",
              amount = generateBigDecimal,
              clearingDate = generateRandomDate(fromDate, toDate),
              clearingReason = Some("Outgoing payment - Paid")
            )
          )
        )
      )
    )

    payments ++ repayments ++ repaymentInterests
  }

  private def generateSuccessfulResponse(numberOfTransactions: Int, fromDate: LocalDate, toDate: LocalDate) =
    AccountActivitySuccess(
      AccountActivityResponse.now,
      transactionDetails = generateAccountActivityTransactions(numberOfTransactions, fromDate, toDate)
    )

  private def generateBigDecimal: BigDecimal =
    val start: Double = 10000.00
    val end:   Double = 100000000000.00

    BigDecimal.valueOf(Random.between(start, end)).setScale(2, BigDecimal.RoundingMode.HALF_EVEN)

  private def generateRandomDate(fromDate: LocalDate, toDate: LocalDate): LocalDate =
    LocalDate.ofEpochDay(Random.between(fromDate.toEpochDay, toDate.toEpochDay + 1))
}
