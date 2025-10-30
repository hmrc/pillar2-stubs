import org.typelevel.scalacoptions.ScalacOptions
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "3.3.5"
ThisBuild / majorVersion := 0

val commonExcludedTpolecat = Set(
  ScalacOptions.warnNonUnitStatement,
  ScalacOptions.warnValueDiscard,
  ScalacOptions.warnUnusedImports
)

lazy val microservice = Project("pillar2-stubs", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    PlayKeys.playDefaultPort := 10052,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Compile / tpolecatExcludeOptions ++= commonExcludedTpolecat
  )
  .settings(CodeCoverageSettings.settings *)

lazy val it = project
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Compile / tpolecatExcludeOptions ++= commonExcludedTpolecat
  )
  .settings(libraryDependencies ++= AppDependencies.it)
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalacOptions ++= Seq(
  "-Wconf:msg=Flag.*set repeatedly:s",
  "-Wconf:src=routes/.*:s"
)

addCommandAlias("prePrChecks", "; scalafmtCheckAll; scalafmtSbtCheck; scalafixAll --check")
addCommandAlias("checkCodeCoverage", "; clean; coverage; test; it/test; coverageReport")
addCommandAlias("lint", "; scalafmtAll; scalafmtSbt; scalafixAll")
