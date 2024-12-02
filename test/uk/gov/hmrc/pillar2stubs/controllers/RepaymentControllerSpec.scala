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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, CREATED, NO_CONTENT}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.models._

class RepaymentControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {
  private val authHeader: (String, String) = HeaderNames.authorisation -> "token"
  val validPayload: SendRepaymentDetails = SendRepaymentDetails(
    repaymentDetails = RepaymentDetails(plrReference = "plrReference", name = "name", utr = None, reasonForRepayment = "???", refundAmount = 10000.1),
    bankDetails = BankDetails(
      nameOnBankAccount = "Paddington",
      bankName = "Bank of Bears",
      sortCode = Some("666666"),
      accountNumber = Some("00000000"),
      iban = None,
      bic = None,
      countryCode = None
    ),
    contactDetails = RepaymentContactDetails(contactDetails = "paddington, paddington@peru.com, marmalade sandwich")
  )

  "RepaymentController" - {

    "must return CREATED for a valid json payload" in {
      val request =
        FakeRequest(POST, routes.RepaymentController.submitRepaymentDetails.url).withBody(Json.toJson(validPayload)).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe CREATED
    }

    "must return NoContent for a valid json payload with a bad actor" in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "bad person")))
      val request = FakeRequest(POST, routes.RepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result  = route(app, request).value

      status(result) shouldBe NO_CONTENT
    }

    "must return BAD_REQUEST for an invalid json payload" in {
      val json: JsValue = Json.obj("invalid" -> "payload")
      val request = FakeRequest(POST, routes.RepaymentController.submitRepaymentDetails.url).withBody(json).withHeaders(authHeader)
      val result  = route(app, request).value

      status(result) shouldBe BAD_REQUEST
    }

  }
}
