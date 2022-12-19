import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.12.0"
  

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion            % "test, it",
    "org.mockito"            % "mockito-core"            % "3.7.7"   % "test, it",
    "com.typesafe.play"      %% "play-test"              % current   % "test, it",
    "org.pegdown"            % "pegdown"                 % "1.6.0"   % "test, it",
    "com.vladsch.flexmark"   % "flexmark-all"            % "0.35.10" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0"   % "test, it",
    "org.scalatestplus"      %% "mockito-3-4"            % "3.2.7.0" % "test",
    "com.github.tomakehurst" % "wiremock-standalone"     % "2.27.2"  % "test, it"
  )
}
