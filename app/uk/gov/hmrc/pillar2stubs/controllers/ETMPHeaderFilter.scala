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

import cats.Semigroup
import cats.data.Validated
import play.api.mvc._
import uk.gov.hmrc.pillar2stubs.controllers.ETMPHeaderFilter.{mandatoryHeaders, validateHeaderExists}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ETMPHeaderFilter @Inject() ()(using ec: ExecutionContext) extends ActionFilter[Request] {

  given Semigroup[Boolean] = (x: Boolean, _: Boolean) => x

  given Semigroup[String] = (x, y) => s"$x || $y"

  override protected def filter[A](request: Request[A]): Future[Option[Result]] =
    mandatoryHeaders
      .map(validateHeaderExists(request, _))
      .foldLeft(Validated.valid[String, Boolean](true))((a, b) => a.combine(b))
      .leftMap(str => Results.BadRequest(str))
      .fold(err => Future.successful(Option(err)), _ => Future.successful(None))

  override protected def executionContext: ExecutionContext = ec
}

object ETMPHeaderFilter {

  val mandatoryHeaders: List[String] = List("correlationid", "x-transmitting-system", "x-originating-system", "x-receipt-date", "x-pillar2-id")

  def validateHeaderExists[A](request: Request[A], headerName: String): Validated[String, Boolean] =
    Validated.cond(request.headers.hasHeader(headerName), true, s"Header $headerName not provided")
}
