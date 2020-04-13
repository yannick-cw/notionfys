import Dependencies.Libraries

name := """notionfys"""

organization in ThisBuild := "notionfys"

scalaVersion in ThisBuild := "2.13.1"

mappings in (Compile, packageDoc) := Seq()

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
  )
)

lazy val root =
  (project in file("."))
  .settings(commonSettings: _*)
