import org.typelevel.scalacoptions.ScalacOptions
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "3.7.3"
ThisBuild / majorVersion := 0

val scalafixSettings = Seq(
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val microservice = Project("pillar2-stubs", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    PlayKeys.playDefaultPort := 10052,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Compile / tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnNonUnitStatement,
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports
    ),
    scalafixSettings
  )
  .settings(CodeCoverageSettings.settings *)

lazy val it = project
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Compile / tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnNonUnitStatement,
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports
    )
  )
  .settings(libraryDependencies ++= AppDependencies.it)

ThisBuild / scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s",
  "-Wconf:msg=Flag.*set repeatedly:s",
  "-Wconf:msg=Setting -Wunused set to all redundantly:s",
  "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s"
)

addCommandAlias("prePrChecks", "; scalafmtCheckAll; scalafmtSbtCheck; scalafixAll --check")
addCommandAlias("checkCodeCoverage", "; clean; coverage; test; it/test; coverageReport")
addCommandAlias("lint", "; scalafmtAll; scalafmtSbt; scalafixAll")
