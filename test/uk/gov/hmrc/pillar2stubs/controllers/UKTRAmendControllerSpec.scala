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
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.models.error.Origin.HIP
import uk.gov.hmrc.pillar2stubs.models.error.{HIPErrorWrapper, HIPFailure}
import uk.gov.hmrc.pillar2stubs.models.{LiabilityData, LiableEntity, UKTRSubmissionData}

import java.time.*

class UKTRAmendControllerSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with OptionValues {

  private val stubResourceLoader: String => Option[String] = {
    case "/resources/liabilities/LiabilitySuccessResponse.json" =>
      Some("""{"success":{"processingDate":"2022-01-31T09:26:17Z","formBundleNumber":"119000004320","chargeReference":"XTC01234123412"}}""")
    case "/resources/liabilities/NilReturnSuccessResponse.json" =>
      Some("""{"success":{"processingDate":"2022-01-31T09:26:17Z","message":"Nil return received and processed successfully"}}""")
    case _ => None
  }

  private def createValidSubmissionData(
    accountingPeriodFrom: LocalDate = LocalDate.of(2024, 8, 14),
    accountingPeriodTo:   LocalDate = LocalDate.of(2024, 12, 14)
  ): UKTRSubmissionData =
    UKTRSubmissionData(
      accountingPeriodFrom = accountingPeriodFrom,
      accountingPeriodTo = accountingPeriodTo,
      obligationMTT = true,
      electionUKGAAP = true,
      liabilities = LiabilityData(
        electionDTTSingleMember = false,
        electionUTPRSingleMember = false,
        numberSubGroupDTT = 1,
        numberSubGroupUTPR = 1,
        totalLiability = 100,
        totalLiabilityDTT = 50,
        totalLiabilityIIR = 50,
        totalLiabilityUTPR = 0,
        liableEntities = List(LiableEntity("Acme Ltd", "CRN", "123456", 50, 0, 0))
      )
    )

  private def createRequest(plrId: String, body: JsValue) =
    FakeRequest(PUT, routes.UKTRAmendController.amendUKTR.url)
      .withHeaders("Content-Type" -> "application/json", authHeader)
      .withHeaders("X-Pillar2-Id" -> plrId)
      .withBody(body)

  val cogsworth: Clock = Clock.fixed(Instant.ofEpochMilli(1734699180110L), ZoneId.of("Z"))

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(
      bind[String => Option[String]].qualifiedWith("resourceLoader").toInstance(stubResourceLoader),
      bind[Clock].toInstance(cogsworth)
    )
    .build()

  val authHeader: (String, String) = HeaderNames.authorisation -> "Bearer valid_token"

  "UKTRAmendController PUT" - {

    "return OK with success response for a valid uktr amendment" in {
      val request = createRequest("XEPLR1234567890", Json.toJson(createValidSubmissionData()))

      val result = route(app, request).value
      status(result) mustBe OK
      val jsonResult = contentAsJson(result)
      (jsonResult \ "success" \ "formBundleNumber").as[String] mustEqual "119000004320"
      (jsonResult \ "success" \ "chargeReference").as[String] mustEqual "XTC01234123412"
      (jsonResult \ "success" \ "processingDate").as[ZonedDateTime] mustEqual ZonedDateTime.now(cogsworth)
    }

    "return OK with success response for a valid NIL_RETURN amendment" in {
      val validNilReturnRequestBody = Json.obj(
        "accountingPeriodFrom" -> "2024-08-14",
        "accountingPeriodTo"   -> "2024-09-14",
        "qualifyingGroup"      -> true,
        "obligationDTT"        -> true,
        "obligationMTT"        -> true,
        "electionUKGAAP"       -> true,
        "liabilities"          -> Json.obj(
          "returnType" -> "NIL_RETURN"
        )
      )

      val request = createRequest("XEPLR1234567890", validNilReturnRequestBody)

      val result = route(app, request).value
      status(result) mustBe OK
      val jsonResult = contentAsJson(result)
      (jsonResult \ "success" \ "formBundleNumber").as[String] mustEqual "119000004320"
      (jsonResult \ "success" \ "processingDate").as[ZonedDateTime] mustEqual ZonedDateTime.now(cogsworth)
    }

    "return UNPROCESSABLE_ENTITY with tax obligation already met error for specific Pillar2Id" in {
      val request = createRequest("XEPLR0422044000", Json.toJson(createValidSubmissionData()))

      val result = route(app, request).value
      status(result) mustBe UNPROCESSABLE_ENTITY
      val jsonResult = contentAsJson(result)
      (jsonResult \ "errors" \ "code").as[String] mustEqual "044"
      (jsonResult \ "errors" \ "text").as[String] mustEqual "Tax obligation already met"
    }

    "return BAD_REQUEST for specific Pillar2Id" in {
      val request = createRequest("XEPLR0400000000", Json.toJson(createValidSubmissionData()))

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      val response = contentAsJson(result).as[HIPErrorWrapper[HIPFailure]]
      response.response.failures must have size 1
      response.response.failures.head.reason mustEqual "invalid json"
      response.origin mustEqual HIP
    }

    "return INTERNAL_SERVER_ERROR for specific Pillar2Id" in {
      val request = createRequest("XEPLR0500000000", Json.toJson(createValidSubmissionData()))

      val result = route(app, request).value
      status(result) mustBe INTERNAL_SERVER_ERROR
      val jsonResult = contentAsJson(result)
      (jsonResult \ "error" \ "code").as[String] mustEqual "500"
      (jsonResult \ "error" \ "message").as[String] mustEqual "Internal server error"
    }

    "return BAD_REQUEST for invalid JSON structure" in {
      val invalidJson = Json.obj("invalidField" -> "value")
      val request     = createRequest("XEPLR1234567890", invalidJson)

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      val response = contentAsJson(result).as[HIPErrorWrapper[HIPFailure]]
      response.response.failures must have size 1
      response.response.failures.head.reason mustEqual "invalid json"
      response.origin mustEqual HIP
    }

    "return BAD_REQUEST for non-JSON data" in {
      val request = FakeRequest(PUT, routes.UKTRAmendController.amendUKTR.url)
        .withHeaders("Content-Type" -> "application/json", authHeader)
        .withHeaders("X-Pillar2-Id" -> "XEPLR1234567890")
        .withBody("non-json body")

      val result = route(app, request).value
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe Json.obj("error" -> "Invalid JSON data")
    }

    "return UNPROCESSABLE_ENTITY if accountingPeriodTo is before accountingPeriodFrom" in {
      val request = createRequest(
        "XEPLR1234567890",
        Json.toJson(
          createValidSubmissionData(
            accountingPeriodFrom = LocalDate.of(2024, 12, 14),
            accountingPeriodTo = LocalDate.of(2024, 8, 14)
          )
        )
      )

      val result = route(app, request).value
      status(result) mustBe UNPROCESSABLE_ENTITY
      (contentAsJson(result) \ "errors" \ "text").as[String] mustEqual "Invalid date range: accountingPeriodTo must be after accountingPeriodFrom"
    }

    "return UNPROCESSABLE_ENTITY if liableEntities array is empty" in {
      val emptyLiabilityData = createValidSubmissionData().copy(
        liabilities = createValidSubmissionData().liabilities.copy(liableEntities = List())
      )

      val request = createRequest("XEPLR1234567890", Json.toJson(emptyLiabilityData))

      val result = route(app, request).value
      status(result) mustBe UNPROCESSABLE_ENTITY
      val jsonResult = contentAsJson(result)
      (jsonResult \ "errors" \ "code").as[String] mustEqual "002"
      (jsonResult \ "errors" \ "text").as[String] mustEqual "Liable entities must not be empty"
    }
  }
}
