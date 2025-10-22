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

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.pillar2stubs.controllers.ETMPHeaderFilterTest.FutureAwait

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Random

class TestETMPHeaderFilter extends ETMPHeaderFilter {
  def runTest[A](req: Request[A]): Future[Option[Result]] = filter(req)
}

class ETMPHeaderFilterTest extends AnyFunSuite {

  val classUnderTest = new TestETMPHeaderFilter()

  test("All headers are present") {
    val headers = ETMPHeaderFilter.mandatoryHeaders.map(_ -> Random.nextString(10))
    val request = FakeRequest().withHeaders(headers*)
    classUnderTest.runTest(request).await shouldEqual None
  }

  ETMPHeaderFilter.mandatoryHeaders.toSet
    .subsets()
    .map(_.toList)
    .toList
    .filterNot(_.isEmpty)
    .filterNot(_.length == ETMPHeaderFilter.mandatoryHeaders.length)
    .foreach { combo =>
      test(s"Missing ${combo.mkString("and")}") {
        val headers = ETMPHeaderFilter.mandatoryHeaders
          .filterNot(str => combo.contains(str))
          .map(_ -> Random.nextString(10))
        val request = FakeRequest().withHeaders(headers*)
        val response: Future[Result] = classUnderTest
          .runTest(request)
          .map {
            case Some(result) => result
            case None         => fail("Expected a result")
          }
        status(response) shouldEqual 400
        val errorStr    = combo.map(str => s"Header $str not provided").sorted
        val responseStr = contentAsString(response).split(" \\|\\| ").toList.sorted
        responseStr shouldEqual errorStr
      }
    }

}

object ETMPHeaderFilterTest {
  implicit class FutureAwait(fut: Future[Option[Result]]) {
    def await: Option[Result] = Await.result(fut, 5.seconds)
  }
}
