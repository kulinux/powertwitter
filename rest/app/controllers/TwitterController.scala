package controllers

import javax.inject.{Inject, Singleton}

import model.Tweet
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.libs.json.Json

@Singleton
class TwitterController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  import Tweet._

  def index() = Action { implicit request: Request[AnyContent] =>
    val tweets = Seq( Tweet("uno", "metadata"), Tweet("dos", "metadata"))
    Ok(Json.toJson( tweets ) )
  }

  def put() = Action(parse.json) { request =>
      val tweet = request.body.as[Tweet]
      Ok(Json.toJson( tweet ))
  }

}
