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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.models.*

class HipRepaymentControllerSpec  extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {
  private val authHeader: (String, String)     = HeaderNames.authorisation -> "token"
  val validPayload:       SendRepaymentDetails = SendRepaymentDetails(
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

  "HipRepaymentController" - {

    "must return CREATED for a valid json payload" in {
      val request =
        FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(Json.toJson(validPayload)).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe CREATED
    }

    "must return NoContent for a valid json payload with a bad actor" in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "bad person")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result  = route(app, request).value

      status(result) shouldBe NO_CONTENT
    }

    "must return Unauthorized when missing auth header" in {
      val request =
        FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(Json.toJson(validPayload))
      val result = route(app, request).value

      status(result) shouldBe UNAUTHORIZED
    }

    """must return forbidden when nameOnBankAccount is "forbidden" """ in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "forbidden")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe FORBIDDEN
    }

    """must return not found when nameOnBankAccount is "not found" """ in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "not found")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe NOT_FOUND
    }

    """must return Unprocessable Entity error with code 003 when nameOnBankAccount is "cannot process" """ in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "cannot process")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe UNPROCESSABLE_ENTITY
      (contentAsJson(result) \ "errors" \ "code").as[String] shouldBe "003"
    }

    """must return Unprocessable Entity error with code 004 when nameOnBankAccount is "duplicate" """ in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "duplicate")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe UNPROCESSABLE_ENTITY
      (contentAsJson(result) \ "errors" \ "code").as[String] shouldBe "004"
    }

    """must return Unprocessable Entity error with code 016 when nameOnBankAccount is "invalid id" """ in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "invalid id")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe UNPROCESSABLE_ENTITY
      (contentAsJson(result) \ "errors" \ "code").as[String] shouldBe "016"

    }

    """must return Internal Server error when nameOnBankAccount is "server error" """ in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "server error")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    """must return Service Unavailable error when nameOnBankAccount is "service unavailable" """ in {
      val badJson = Json.toJson(validPayload.copy(bankDetails = validPayload.bankDetails.copy(nameOnBankAccount = "service unavailable")))
      val request = FakeRequest(POST, routes.HipRepaymentController.submitRepaymentDetails.url).withBody(badJson).withHeaders(authHeader)
      val result = route(app, request).value

      status(result) shouldBe SERVICE_UNAVAILABLE
    }

    "must return BAD_REQUEST for an invalid json payload" in {
      val json: JsValue = Json.obj("invalid" -> "payload")
      val request = FakeRequest(POST, routes.RepaymentController.submitRepaymentDetails.url).withBody(json).withHeaders(authHeader)
      val result  = route(app, request).value

      status(result) shouldBe BAD_REQUEST
    }

  }
}

