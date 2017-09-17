package com.powertwitter.model


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
