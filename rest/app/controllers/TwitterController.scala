package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}
import model.Tweet
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.libs.json.Json
import akka.pattern.ask
import akka.util.Timeout
import com.powertwitter.model._
import rabbitmq.RabbitActor

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TwitterController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  import Tweet._

  implicit val system = ActorSystem.create("system")
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

}
