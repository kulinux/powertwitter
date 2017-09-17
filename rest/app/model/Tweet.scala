package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Tweet(tweet : String, metadata : String)

object Tweet {
  /**
    * Mapping to write a PostResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[Tweet] {
    def writes(post: Tweet): JsValue = {
      Json.obj(
        "tweet" -> post.tweet,
        "metadata" -> post.metadata
      )
    }
  }

  implicit val implicitReads: Reads[Tweet] = (
    (JsPath \ "tweet").read[String] and
      (JsPath \ "metadata").read[String]
    )(Tweet.apply _)
}
