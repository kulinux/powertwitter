package com.powertwitter.cassandra.cassandra

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext.Implicits.global


object  MainCassandra extends App {

  implicit val system = ActorSystem()
  val cass = new Cassandra()

  cass.initSchema()
  cass.insert().onComplete( x => cass.test() )



}
