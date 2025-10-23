import org.typelevel.scalacoptions.ScalacOptions
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "3.7.3"
ThisBuild / majorVersion := 0
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project("pillar2-stubs", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    PlayKeys.playDefaultPort := 10052,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:msg=Flag.*set repeatedly:s",
    scalacOptions += "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s"
  )
  .settings(CodeCoverageSettings.settings*)
  .settings(
    Compile / tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnNonUnitStatement,
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports
    )
  )

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

addCommandAlias("prePrChecks", "; scalafmtCheckAll; scalafmtSbtCheck; scalafixAll --check")
addCommandAlias("checkCodeCoverage", "; clean; coverage; test; it/test; coverageReport")
addCommandAlias("lint", "; scalafmtAll; scalafmtSbt; scalafixAll")
