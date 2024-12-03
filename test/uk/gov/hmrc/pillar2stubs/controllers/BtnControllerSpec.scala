package uk.gov.hmrc.pillar2stubs.controllers

import org.scalatest.OptionValues
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.pillar2stubs.models.btn.{BTNRequest, BtnErrorResponse, BtnFailureResponsePayload, BtnSuccessResponsePayload}
import play.api.libs.json._

import java.time.LocalDate
import scala.util.Random

class BtnControllerSpec extends AnyFunSuite with Matchers with GuiceOneAppPerSuite with OptionValues {

  val validHeaders = (ETMPHeaderFilter.mandatoryHeaders ++ List(HeaderNames.authorisation)).map(_ -> Random.nextString(10))

  test("Valid BTN submission") {
    val request = FakeRequest(POST, routes.BTNController.submitBTN("XMPLR00000000012").url)
      .withHeaders(Headers(validHeaders: _*))
      .withBody(Json.toJson(BTNRequest(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1))))

    val result = route(app, request).value

    status(result) shouldEqual 200
    contentAsJson(result).validate[BtnSuccessResponsePayload].asEither.isRight shouldBe true
  }

  test("BadRequest BTN submission") {
    val request = FakeRequest(POST, routes.BTNController.submitBTN("XEPLR4000000000").url)
      .withHeaders(Headers(validHeaders: _*))
      .withBody(Json.toJson(BTNRequest(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1))))

    val result = route(app, request).value

    status(result) shouldEqual 400
    contentAsJson(result).validate[BtnFailureResponsePayload].asEither.isRight shouldBe true
  }

  test("InternalServerError BTN submission") {
    val request = FakeRequest(POST, routes.BTNController.submitBTN("XEPLR5000000000").url)
      .withHeaders(Headers(validHeaders: _*))
      .withBody(Json.toJson(BTNRequest(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1))))

    val result = route(app, request).value

    status(result) shouldEqual 500
    contentAsJson(result).validate[BtnErrorResponse].asEither.isRight shouldBe true
  }



}
