package com.powertwitter.model


import play.api.libs.json._
import play.api.libs.functional.syntax._

object TwitterData {
  implicit val implicitWrites = new Writes[TwitterData] {
    def writes(post: TwitterData): JsValue = {
      Json.obj(
        "id" -> ("" + post.id),
        "tweet" -> post.tweet,
        "metadata" -> post.metadata
      )
    }
  }

  implicit val implicitReads: Reads[TwitterData] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "tweet").read[String] and
      (JsPath \ "metadata").read[String]
    )((x, y, z) => TwitterData(x, y, z) )
}

final case class TwitterData(id: String, tweet: String, metadata: String)

object TestTwitterData extends App {
  val td = TwitterData("444", "tweet", "metadata")

  val res = TwitterData.implicitWrites.writes(td)

  val js = TwitterData.implicitReads.reads(res)

  println(res)
  println(js.get)
}
