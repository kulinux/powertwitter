/*
lazy val root = (project in file("."))
  .aggregate(model, cassandra, front)
*/

lazy val model = (project in file("model"))

lazy val cassandra = (project in file("core"))

lazy val front  = (project in file("front"))
