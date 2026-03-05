import org.typelevel.scalacoptions.ScalacOptions
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "3.3.6"
ThisBuild / majorVersion := 0

lazy val microservice = Project("pillar2-stubs", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    PlayKeys.playDefaultPort := 10052,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    compilerSettings
  )
  .settings(CodeCoverageSettings.settings *)

lazy val it = project
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    compilerSettings
  )
  .settings(libraryDependencies ++= AppDependencies.it)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

addCommandAlias("prePrChecks", "; scalafmtCheckAll; scalafmtSbtCheck; scalafixAll --check")
addCommandAlias("checkCodeCoverage", "; clean; coverage; test; it/test; coverageReport")
addCommandAlias("lint", "; scalafmtAll; scalafmtSbt; scalafixAll")

lazy val compilerSettings = Seq(
  scalacOptions ~= (_.distinct),
  tpolecatCiModeOptions += ScalacOptions.warnOption("conf:src=routes/.*:s"),
  Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
)
