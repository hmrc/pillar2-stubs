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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.pillar2stubs.controllers.actions.AuthActionFilter
import uk.gov.hmrc.pillar2stubs.models.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject

class BarsController @Inject() (cc: ControllerComponents, authFilter: AuthActionFilter) extends BackendController(cc) {

  def verify: Action[JsValue] = (Action(parse.json) andThen authFilter) { implicit request =>
    request.body.asOpt[BarsBusinessAssessmentRequest] match {
      case Some(value) => toResponse(value)
      case None        => BadRequest("Request not matching")
    }

  }

  private def toResponse(value: BarsBusinessAssessmentRequest): _root_.play.api.mvc.Result =
    (value.account.sortCode, value.account.accountNumber) match {
      case ("206705", "86473611") =>
        val businessAccountName = "Epic Adventure Inc"
        val matchedName         =
          if (businessAccountName == value.business.companyName) NameMatches.Yes
          else if (businessAccountName.contains(value.business.companyName)) NameMatches.Partial
          else NameMatches.No

        Ok(Json.toJson(barsAccountResponse(accountName = Some(businessAccountName), nameMatches = matchedName)))
      case ("206705", "86563612") =>
        Ok(Json.toJson(barsAccountResponse(accountName = Some("Sanguine Skincare"), accountNumberIsWellFormatted = AccountNumberIsWellFormatted.No)))
      case ("206705", "76523611") => Ok(Json.toJson(barsAccountResponse(accountName = Some("Vortex Solar"), accountExists = AccountExists.No)))
      case ("206705", "56523611") =>
        Ok(Json.toJson(barsAccountResponse(accountName = Some("Innovation Arch"), accountExists = AccountExists.Inapplicable)))
      case ("206705", "56945688") =>
        Ok(Json.toJson(barsAccountResponse(accountName = Some("Eco Focus"), accountExists = AccountExists.Indeterminate)))
      case ("207102", "86473611") => Ok(Json.toJson(barsAccountResponse(accountName = Some("Flux Water Gear"), accountExists = AccountExists.Error)))
      case ("207102", "86563611") => Ok(Json.toJson(barsAccountResponse(accountName = Some("Lambent Illumination"), nameMatches = NameMatches.No)))
      case ("207102", "76523611") => Ok(Json.toJson(barsAccountResponse(accountName = Some("Boneféte Fun"), nameMatches = NameMatches.Inapplicable)))
      case ("207102", "56523611") => Ok(Json.toJson(barsAccountResponse(accountName = Some("Cogent-Data"), nameMatches = NameMatches.Indeterminate)))
      case ("207102", "74597611") => Ok(Json.toJson(barsAccountResponse(accountName = Some("Cipher Publishing"), nameMatches = NameMatches.Error)))
      case ("207106", "86473611") =>
        Ok(Json.toJson(barsAccountResponse(accountName = Some("Security Engima"), sortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.No)))
      case ("207106", "86563611") =>
        Ok(Json.toJson(barsAccountResponse(accountName = Some("Megacorp"), sortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.Error)))
      case ("207106", "76523611") =>
        Ok(
          Json.toJson(
            barsAccountResponse(
              accountName = Some("Genomics Inc"),
              nonStandardAccountDetailsRequiredForBacs = NonStandardAccountDetailsRequiredForBacs.Yes
            )
          )
        )
      case ("207106", "56523611") =>
        Ok(
          Json.toJson(
            barsAccountResponse(accountName = Some("Full Force Futures"), sortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.Error)
          )
        )
      case ("207106", "74597611") =>
        Ok(
          Json.toJson(
            barsAccountResponse(
              accountName = Some("Resource Refresh"),
              sortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.No,
              nameMatches = NameMatches.No,
              accountExists = AccountExists.No
            )
          )
        )
      case ("609593", "96863604") =>
        Ok(
          Json.toJson(
            barsAccountResponse(
              accountName = Some("O'Connor Construction"),
              accountNumberIsWellFormatted = AccountNumberIsWellFormatted.Indeterminate
            )
          )
        )
      case ("609593", "96113600") =>
        Ok(
          Json.toJson(
            barsAccountResponse(
              accountName = Some("Candyland Consulting"),
              accountNumberIsWellFormatted = AccountNumberIsWellFormatted.Indeterminate,
              accountExists = AccountExists.No
            )
          )
        )
      case _ => InternalServerError("Unable to match")
    }

  private def barsAccountResponse(
    nameMatches:                              NameMatches = NameMatches.Yes,
    accountName:                              Option[String],
    accountNumberIsWellFormatted:             AccountNumberIsWellFormatted = AccountNumberIsWellFormatted.Yes,
    accountExists:                            AccountExists = AccountExists.Yes,
    sortCodeIsPresentOnEISCD:                 SortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.Yes,
    nonStandardAccountDetailsRequiredForBacs: NonStandardAccountDetailsRequiredForBacs = NonStandardAccountDetailsRequiredForBacs.No,
    sortCodeSupportsDirectCredit:             SortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.No
  ): BarsAccountResponse = BarsAccountResponse(
    accountNumberIsWellFormatted,
    sortCodeIsPresentOnEISCD,
    sortCodeBankName = Some("BARCLAYS BANK UK PLC"),
    nonStandardAccountDetailsRequiredForBacs,
    accountExists,
    nameMatches,
    accountName,
    SortCodeSupportsDirectDebit.No,
    sortCodeSupportsDirectCredit,
    iban = Some("GB21BARC20670586473611")
  )
}
