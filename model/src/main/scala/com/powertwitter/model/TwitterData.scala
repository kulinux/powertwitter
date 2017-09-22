package com.powertwitter.model


import play.api.libs.json._
import play.api.libs.functional.syntax._

object TwitterData {
  implicit val implicitWrites = new Writes[TwitterData] {
    def writes(post: TwitterData): JsValue = {
      Json.obj(
        "id" -> post.id.underlying,
        "tweet" -> post.tweet,
        "metadata" -> post.metadata
      )
    }
  }

  implicit val implicitReads: Reads[TwitterData] = (
    (JsPath \ "id").read[Int] and
      (JsPath \ "tweet").read[String] and
      (JsPath \ "metadata").read[String]
    )((x, y, z) => TwitterData(TwitterId(x.toString), y, z) )
}

final case class TwitterData(id: TwitterId, tweet: String, metadata: String)

class TwitterId private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object TwitterId {
  def apply(raw: String): TwitterId = {
    require(raw != null)
    new TwitterId(Integer.parseInt(raw))
  }
}
object TestTwitterData extends App {
  val td = TwitterData(TwitterId("444"), "tweet", "metadata")

  val res = TwitterData.implicitWrites.writes(td)

  val js = TwitterData.implicitReads.reads(res)

  println(res)
  println(js.get)
}
