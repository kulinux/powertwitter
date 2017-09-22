name := "powertwitter-cassandra"

version := "1.0"

scalaVersion := "2.12.3"

lazy val akkaVersion = "2.5.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "0.10",
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.11",
  "com.powertwitter" %% "model" % "0.1.0-SNAPSHOT"
)

mainClass in (Compile, run) := Some("com.powertwitter.cassandra.rabbit.MainConsumer")
