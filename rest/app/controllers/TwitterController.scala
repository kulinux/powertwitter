package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}
import model.Tweet
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.libs.json.Json
import rabbitmq.RabbitActor

import com.powertwitter.model._

@Singleton
class TwitterController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  implicit val system = ActorSystem.create("system")
  val rb = system.actorOf(Props[RabbitActor])

  import Tweet._

  def index() = Action { implicit request: Request[AnyContent] =>
    val tweets = Seq( Tweet("uno", "metadata"), Tweet("dos", "metadata"))
    Ok(Json.toJson( tweets ) )
  }

  def put() = Action(parse.json) { request =>
    val tweet = request.body.as[Tweet]
    val tweetData = new TwitterData(TwitterId("-1"), tweet.tweet, tweet.metadata)
    rb ! tweetData
    Ok(Json.toJson( tweet ))
  }

}
