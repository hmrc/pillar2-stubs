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
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LiabilityControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  "LiabilityController POST" - {

    "return CREATED with success response for valid plrReference" in {
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
        .withHeaders("Content-Type" -> "application/json")
        .withBody(validRequestBody)

      val result = route(app, request).value

      status(result) mustBe CREATED
      contentType(result) mustBe Some("application/json")

      val jsonResponse = contentAsJson(result)

      (jsonResponse \ "success").asOpt[JsValue] match {
        case Some(success) =>
          (success \ "formBundleNumber").as[String] mustBe "119000004320"
          (success \ "chargeReference").as[String] mustBe "XTC01234123412"
        case None =>
          fail(s"Expected 'success' key in response, but got: $jsonResponse")
      }
    }

    "return NOT_FOUND for invalid plrReference" in {
      val request = FakeRequest(POST, routes.LiabilityController.createLiability("INVALID_PLR_REFERENCE").url)
        .withHeaders("Content-Type" -> "application/json")
        .withBody(Json.obj())

      val result = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentType(result) mustBe Some("application/json")

      val jsonResponse = contentAsJson(result)

      (jsonResponse \ "failures").asOpt[Seq[JsValue]].flatMap(_.headOption) match {
        case Some(failure) =>
          (failure \ "code").as[String] mustBe "LIABILITIES_NOT_FOUND"
        case None =>
          fail("Expected 'failures' array in response, but got: " + jsonResponse)
      }
    }

    "return BAD_REQUEST with INVALID_REQUEST for invalid JSON structure" in {
      val invalidJson = Json.obj("invalidField" -> "value")

      val request = FakeRequest(POST, routes.LiabilityController.createLiability("XTC01234123412").url)
        .withHeaders("Content-Type" -> "application/json")
        .withBody(invalidJson)

      val result = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some("application/json")

      val jsonResponse = contentAsJson(result)

      (jsonResponse \ "failures").asOpt[Seq[JsValue]].flatMap(_.headOption) match {
        case Some(failure) =>
          (failure \ "code").as[String] mustBe "INVALID_REQUEST"
        case None =>
          fail("Expected 'failures' array in response, but got: " + jsonResponse)
      }
    }
  }
}
