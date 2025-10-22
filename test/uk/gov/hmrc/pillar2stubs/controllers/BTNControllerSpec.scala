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
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.models.btn._
import uk.gov.hmrc.pillar2stubs.models.error.Origin.HIP
import uk.gov.hmrc.pillar2stubs.models.error.{HIPErrorWrapper, HIPFailure}

import java.time.LocalDate
import scala.util.Random

class BTNControllerSpec extends AnyFunSuite with Matchers with GuiceOneAppPerSuite with OptionValues {

  val validHeaders: List[(String, String)] =
    (ETMPHeaderFilter.mandatoryHeaders ++ List(HeaderNames.authorisation)).map(_ -> Random.nextString(10))

  def request(implicit pillar2Id: String): FakeRequest[JsValue] =
    FakeRequest(POST, routes.BTNController.submitBTN.url)
      .withHeaders(Headers(validHeaders*))
      .withHeaders("X-PILLAR2-ID" -> pillar2Id)
      .withBody(Json.toJson(BTNRequest(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1))))

  test("Valid BTN submission") {
    implicit val pillar2Id: String = "XMPLR00000000012"
    val result = route(app, request).value

    status(result) shouldEqual 201
    contentAsJson(result).validate[BTNSuccessResponsePayload].asEither.isRight shouldBe true
  }

  test("UnprocessableContent BTN submission") {
    implicit val pillar2Id: String = "XEPLR4220000000"
    val result = route(app, request).value

    status(result) shouldEqual 422
    contentAsJson(result).validate[BTNFailureResponsePayload].asEither.isRight shouldBe true
  }

  test("BadRequest BTN submission") {
    implicit val pillar2Id: String = "XEPLR4000000000"
    val result = route(app, request).value

    status(result) shouldEqual 400
    val response = contentAsJson(result).as[HIPErrorWrapper[HIPFailure]]
    response.response.failures should have size 1
    response.response.failures.head.reason shouldEqual "invalid json"
    response.origin shouldEqual HIP
  }

  test("InternalServerError BTN submission") {
    implicit val pillar2Id: String = "XEPLR5000000000"
    val result = route(app, request).value

    status(result) shouldEqual 500
    contentAsJson(result).validate[BTNErrorResponse].asEither.isRight shouldBe true
    contentAsJson(result).as[BTNErrorResponse].error.code shouldEqual "500"
  }

}
