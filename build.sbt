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
  "--initialize-at-build-time"
)

val nativeImagePath = sys.env.get("NATIVE_IMAGE_PATH")
  .map(path => Seq(graalVMNativeImageCommand := path))
  .getOrElse(Seq())

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
  graalVMNativeImageGraalVersion := sys.env.get("GRAAL_DOCKER_VERSION"), //e.g. 20.0.0
) ++ nativeImagePath

lazy val root =
  (project in file("."))
  .settings(commonSettings: _*)
