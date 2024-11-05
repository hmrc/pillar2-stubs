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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames

import java.time.LocalDate

class LiabilityControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  private val stubResourceLoader: String => Option[String] = {
    case "/resources/liabilities/LiabilitySuccessResponse.json" =>
      Some("""{"success":{"processingDate":"2022-01-31T09:26:17Z","formBundleNumber":"119000004320","chargeReference":"XTC01234123412"}}""")
    case _ => None
  }

  override def fakeApplication() = new GuiceApplicationBuilder()
    .overrides(
      bind[String => Option[String]].qualifiedWith("resourceLoader").toInstance(stubResourceLoader)
    )
    .build()

  val authHeader: (String, String) = HeaderNames.authorisation -> "Bearer valid_token"

  "LiabilityController POST" - {

    "return CREATED with success response when plrReference is valid and JSON is correct" in {
      val validRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "liabilities" -> Json.obj(
          "totalLiability"     -> 10000.99,
          "totalLiabilityDTT"  -> 5000.99,
          "totalLiabilityIIR"  -> 4000,
          "totalLiabilityUTPR" -> 10000.99,
          "liableEntities" -> Json.arr(
            Json.obj(
              "ukChargeableEntityName" -> "Newco PLC",
              "idType"                 -> "CRN",
              "idValue"                -> "12345678",
              "amountOwedDTT"          -> 5000,
              "electedDTT"             -> true,
              "amountOwedIIR"          -> 3400,
              "amountOwedUTPR"         -> 6000.5,
              "electedUTPR"            -> true
            )
          )
        )
      )

      val request = FakeRequest(POST, routes.LiabilityController.createLiability("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(validRequestBody)

      val result      = route(app, request).value
      val currentDate = LocalDate.now().toString
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.parse(
        s"""{"success":{"processingDate":"${currentDate}T09:26:17Z","formBundleNumber":"119000004320","chargeReference":"XTC01234123412"}}"""
      )
    }

    "return NOT_FOUND for valid JSON but incorrect plrReference" in {
      val validRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "liabilities" -> Json.obj(
          "totalLiability" -> 10000.99,
          "liableEntities" -> Json.arr(
            Json.obj(
              "ukChargeableEntityName" -> "Newco PLC",
              "idType"                 -> "CRN",
              "idValue"                -> "12345678",
              "amountOwedDTT"          -> 5000,
              "amountOwedIIR"          -> 3400,
              "amountOwedUTPR"         -> 6000.5
            )
          )
        )
      )

      val request = FakeRequest(POST, routes.LiabilityController.createLiability("INVALID_PLR_REFERENCE").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(validRequestBody)

      val result = route(app, request).value
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj("error" -> "No liabilities found for the given reference")
    }

    "return BAD_REQUEST for invalid JSON structure" in {
      val invalidJson = Json.obj("invalidField" -> "value")

      val request = FakeRequest(POST, routes.LiabilityController.createLiability("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(invalidJson)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON request format")
    }

    "return BAD_REQUEST for non-JSON data" in {
      val request = FakeRequest(POST, routes.LiabilityController.createLiability("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody("non-json body")

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON data")
    }
  }
}
