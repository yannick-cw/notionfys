import Dependencies.Libraries

name := """notionfys"""

organization in ThisBuild := "notionfys"

scalaVersion in ThisBuild := "2.13.1"

enablePlugins(GraalVMNativeImagePlugin)

mappings in (Compile, packageDoc) := Seq()

graalVMNativeImageOptions ++= Seq(
  "--no-fallback",
  "--allow-incomplete-classpath",
  "--report-unsupported-elements-at-runtime",
  "--initialize-at-build-time",
  "--static"
)


lazy val commonSettings = Seq(
  organizationName := "notionfys",
  scalafmtOnCompile := true,
  libraryDependencies ++= Seq(
    Libraries.cats,
    Libraries.catsEffect,
    Libraries.catsMtl,
    Libraries.osLib,
    Libraries.decline,
    Libraries.scalaTest  % Test,
    Libraries.scalaCheck % Test,
    compilerPlugin(Libraries.kindProjector),
    compilerPlugin(Libraries.betterMonadicFor)
  ),
  graalVMNativeImageGraalVersion := Some("20.0.0"),
)

lazy val root =
  (project in file("."))
  .settings(commonSettings: _*)
