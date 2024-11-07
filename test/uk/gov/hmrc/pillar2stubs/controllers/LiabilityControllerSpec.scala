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
    case "/resources/liabilities/NilReturnSuccessResponse.json" =>
      Some("""{"success":{"processingDate":"2022-01-31T09:26:17Z","message":"Nil return received and processed successfully"}}""")
    case _ => None
  }

  override def fakeApplication() = new GuiceApplicationBuilder()
    .overrides(
      bind[String => Option[String]].qualifiedWith("resourceLoader").toInstance(stubResourceLoader)
    )
    .build()

  val authHeader: (String, String) = HeaderNames.authorisation -> "Bearer valid_token"

  "LiabilityController POST" - {

    "return CREATED with success response for a valid liability submission" in {
      val validLiabilityRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
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

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(validLiabilityRequestBody)

      val result      = route(app, request).value
      val currentDate = LocalDate.now().toString
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.parse(
        s"""{"success":{"processingDate":"${currentDate}T09:26:17Z","formBundleNumber":"119000004320","chargeReference":"XTC01234123412"}}"""
      )
    }

    "return CREATED with success response for a valid NIL_RETURN submission" in {
      val validNilReturnRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-09-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
        "liabilities" -> Json.obj(
          "returnType" -> "NIL_RETURN"
        )
      )

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(validNilReturnRequestBody)

      val result      = route(app, request).value
      val currentDate = LocalDate.now().toString
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.parse(
        s"""{"success":{"processingDate":"${currentDate}T09:26:17Z","message":"Nil return received and processed successfully"}}"""
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

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("INVALID_PLR_REFERENCE").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(validRequestBody)

      val result = route(app, request).value
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe Json.obj("error" -> "No liabilities found for the given reference")
    }

    "return BAD_REQUEST for invalid JSON structure" in {
      val invalidJson = Json.obj("invalidField" -> "value")

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(invalidJson)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON request format")
    }

    "return BAD_REQUEST for non-JSON data" in {
      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody("non-json body")

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON data")
    }

    "return BAD_REQUEST if liableEntities array is empty" in {
      val invalidRequestBody = Json.obj(
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
          "liableEntities"     -> Json.arr()
        )
      )

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(invalidRequestBody)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "liableEntities must not be empty")
    }

    "return BAD_REQUEST if a required field in liableEntities is missing" in {
      val incompleteEntityRequestBody = Json.obj(
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
              "idValue"                -> "12345678", // Missing "idType"
              "amountOwedDTT"          -> 5000,
              "electedDTT"             -> true,
              "amountOwedIIR"          -> 3400,
              "amountOwedUTPR"         -> 6000.5,
              "electedUTPR"            -> true
            )
          )
        )
      )

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(incompleteEntityRequestBody)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON request format")
    }

    "return BAD_REQUEST if liableEntities key is missing" in {
      val missingEntitiesKeyRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "liabilities" -> Json.obj(
          "totalLiability"     -> 10000.99,
          "totalLiabilityDTT"  -> 5000.99,
          "totalLiabilityIIR"  -> 4000,
          "totalLiabilityUTPR" -> 10000.99
        )
      )

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(missingEntitiesKeyRequestBody)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON request format")
    }

    "return BAD_REQUEST if liabilities key is missing" in {
      val missingLiabilitiesKeyRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true
      )

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(missingLiabilitiesKeyRequestBody)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON request format")
    }

    "return CREATED with additional fields ignored in response" in {
      val extraFieldsRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-12-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
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
        ),
        "extraField" -> "unexpected data"
      )

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(extraFieldsRequestBody)

      val result      = route(app, request).value
      val currentDate = LocalDate.now().toString
      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.parse(
        s"""{"success":{"processingDate":"${currentDate}T09:26:17Z","formBundleNumber":"119000004320","chargeReference":"XTC01234123412"}}"""
      )
    }

    "return BAD_REQUEST if accountingPeriodTo is before accountingPeriodFrom" in {
      val invalidDateRangeRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-07-14", // Earlier than accountingPeriodFrom
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
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

      val request = FakeRequest(POST, routes.LiabilityController.submitUktr("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withBody(invalidDateRangeRequestBody)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid date range: accountingPeriodTo must be after accountingPeriodFrom")
    }
  }
}
