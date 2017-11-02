package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}
import model.Tweet
import play.api.mvc._
import play.api.libs.json._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.powertwitter.model._
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import rabbitmq.RabbitActor

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TwitterController @Inject()(cc: ControllerComponents)
                                 (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  import Tweet._

  lazy val rb = system.actorOf(Props[RabbitActor])


  def all() = Action.async {
    implicit val timeout: Timeout = 4 seconds
    val f = rb ? "ALL"
    f.map( x => x.asInstanceOf[ListBuffer[TwitterData]])
      .map( x =>  Ok(Json.toJson( x )) )
  }

  def put() = Action(parse.json) { request =>
    val tweet = request.body.as[Tweet]
    val tweetData = new TwitterData("-1", tweet.tweet, tweet.metadata)
    rb ! tweetData
    Ok(Json.toJson( tweet ))
  }

  implicit val outEventFormat = Json.format[TwitterData]
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[String, TwitterData]


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      ListTwitterActor.props(out, rb)
    }
  }

}
