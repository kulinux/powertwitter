package com.powertwitter.cassandra.cassandra

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global


object  MainCassandra extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val cass = new Cassandra()

  cass.initSchema()
  cass.insert().onComplete( x => cass.test() )



}
