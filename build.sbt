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

// Minimal Wconf suppressions only for warnings that tpolecat cannot handle:
// - Plugin conflicts (flags set by both tpolecat and other plugins)
// - Generated routes files
// - @nowarn annotation validation (Scala 3 compiler check)
// - Unreachable case (intentional pattern match exhaustiveness)
ThisBuild / scalacOptions ++= Seq(
  "-Wconf:msg=Flag.*set repeatedly:s",
  "-Wconf:msg=Flag -unchecked set repeatedly:s",
  "-Wconf:msg=Flag -deprecation set repeatedly:s",
  "-Wconf:msg=Flag -encoding set repeatedly:s",
  "-Wconf:msg=Unreachable case:s",
  "-Wconf:msg=@nowarn annotation does not suppress any warnings:s",
  "-Wconf:src=routes/.*:s"
)

addCommandAlias("prePrChecks", "; scalafmtCheckAll; scalafmtSbtCheck; scalafixAll --check")
addCommandAlias("checkCodeCoverage", "; clean; coverage; test; it/test; coverageReport")
addCommandAlias("lint", "; scalafmtAll; scalafmtSbt; scalafixAll")
