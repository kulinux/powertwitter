package controllers

import akka.actor.{Actor, ActorRef, Props}
import com.powertwitter.model.TwitterData
import play.api.libs.json.Json
import rabbitmq.RabbitActor

class ListTwitterActor(out: ActorRef, rb: ActorRef) extends Actor {

  val me = self

  override def preStart(): Unit = {
    super.preStart()
  }


  override def postStop(): Unit = {
    super.postStop()
    println("Actor dies")
  }

  override def receive: Receive = {
    case msg: String => {
      rb ! RabbitActor.SubscribeMe(me)
    }
    case td: TwitterData => {
      out ! Json.toJson(td).toString()
      println("send json to client")
    }
    case umsg => println(s"Unknow message $umsg")

  }


}

object ListTwitterActor {
  def props(out: ActorRef, rb: ActorRef) =
    Props(new ListTwitterActor(out, rb) )
}
