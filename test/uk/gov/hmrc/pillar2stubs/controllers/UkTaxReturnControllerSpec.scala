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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames
import play.api.libs.json.Json

class UkTaxReturnControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  private val stubResourceLoader: String => Option[String] = {
    case "/resources/uktaxreturn/SuccessResponse.json" =>
      Some("""{"success":{"processingDate":"2022-01-31T09:26:17Z","formBundleNumber":"119000004320","chargeReference":"XTC01234123412"}}""")
    case "/resources/uktaxreturn/InvalidRequestResponse.json" =>
      Some(
        """{"error":{"code":"400","message":"Invalid JSON message content used; Message: \\"Expected a ',' or '}' at character 93 of {...","logID":"C0000AB8190C86300000000200006836"}}"""
      )
    case "/resources/uktaxreturn/MissingPLRResponse.json" =>
      Some("""{"errors":{"processingDate":"2022-01-31T09:26:17Z","code":"002","text":"Pillar 2 ID missing or invalid"}}""")
    case _ => None
  }

  override def fakeApplication() = new GuiceApplicationBuilder()
    .overrides(
      bind[String => Option[String]].qualifiedWith("resourceLoader").toInstance(stubResourceLoader)
    )
    .build()

  val authHeader: (String, String) = HeaderNames.authorisation -> "Bearer valid_token"

  private val validRequestBody = Json.obj(
    "accountingPeriodFrom" -> "2024-01-01",
    "accountingPeriodTo"   -> "2024-12-31",
    "obligationMTT"        -> true,
    "electionUKGAAP"       -> true,
    "liabilities" -> Json.obj(
      "totalLiability"     -> Some(BigDecimal("10000.99")),
      "totalLiabilityDTT"  -> Some(BigDecimal("5000.99")),
      "totalLiabilityIIR"  -> Some(BigDecimal("4000")),
      "totalLiabilityUTPR" -> Some(BigDecimal("10000.99")),
      "liableEntities" -> Some(
        Seq(
          Json.obj(
            "ukChargeableEntityName" -> "Newco PLC",
            "idType"                 -> "CRN",
            "idValue"                -> "12345678",
            "amountOwedDTT"          -> BigDecimal("5000"),
            "electedDTT"             -> true,
            "amountOwedIIR"          -> BigDecimal("3400"),
            "amountOwedUTPR"         -> BigDecimal("6000.5"),
            "electedUTPR"            -> true
          )
        )
      )
    )
  )

  "UkTaxReturnController POST" - {

    "return CREATED with success response for a valid submission with XTC01234123412" in {
      val request = FakeRequest(POST, "/submit-uk-tax-return")
        .withHeaders("X-Pillar2-Id" -> "XTC01234123412", "Content-Type" -> "application/json", authHeader)
        .withBody(validRequestBody)

      val result = route(app, request).value
      status(result) mustBe CREATED
      contentAsJson(result).toString must include("XTC01234123412")
    }

    "return BAD_REQUEST with invalid request response for XEPLR1066196400" in {
      val request = FakeRequest(POST, "/submit-uk-tax-return")
        .withHeaders("X-Pillar2-Id" -> "XEPLR1066196400", "Content-Type" -> "application/json", authHeader)
        .withBody(validRequestBody)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result).toString must include("Invalid&#x20;JSON&#x20;message&#x20;content&#x20;used")
    }

    "return BAD_REQUEST when X-Pillar2-Id header is missing" in {
      val request = FakeRequest(POST, "/submit-uk-tax-return")
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(validRequestBody)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result).toString must include("Pillar 2 ID missing or invalid")
    }

    "return CREATED with success response for any other PLR reference" in {
      val request = FakeRequest(POST, "/submit-uk-tax-return")
        .withHeaders("X-Pillar2-Id" -> "OTHER_PLR", "Content-Type" -> "application/json", authHeader)
        .withBody(validRequestBody)

      val result = route(app, request).value
      status(result) mustBe CREATED
      contentAsJson(result).toString must include("formBundleNumber")
    }
  }
}
