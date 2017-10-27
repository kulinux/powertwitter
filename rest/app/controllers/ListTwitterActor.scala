package controllers

import akka.actor.{Actor, ActorRef, Props}

class ListTwitterActor(out: ActorRef) extends Actor {


  override def receive: Receive = {
    case msg: String => out ! s"String received $msg"
  }

}

object ListTwitterActor {
  def props(out : ActorRef) = Props(new ListTwitterActor(out) )
}
