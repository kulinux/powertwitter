package controllers

import akka.actor.{ActorSystem, Props}
import rabbitmq.RabbitActor
import javax.inject.Singleton


@Singleton
class Boot {
  implicit val system = ActorSystem.create("system")
  val rb = system.actorOf(Props[RabbitActor])
}
