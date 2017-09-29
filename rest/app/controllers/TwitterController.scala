package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}
import model.Tweet
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.libs.json.Json
import rabbitmq.RabbitActor

import com.powertwitter.model._

@Singleton
class TwitterController @Inject()(boot: Boot, cc: ControllerComponents) extends AbstractController(cc) {

  import Tweet._

  def all() = Action { implicit request: Request[AnyContent] =>
    ???
  }

  def put() = Action(parse.json) { request =>
    val tweet = request.body.as[Tweet]
    val tweetData = new TwitterData("-1", tweet.tweet, tweet.metadata)
    boot.rb ! tweetData
    Ok(Json.toJson( tweet ))
  }

}
