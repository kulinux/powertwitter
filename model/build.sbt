
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.powertwitter",
      scalaVersion := "2.11.11",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "model"
  )
