package v1.twitter

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying post information.
  */
case class TwitterResource(id: String, tweet: String, metadata: String)

object TwitterResource {

  /**
    * Mapping to write a PostResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[TwitterResource] {
    def writes(post: TwitterResource): JsValue = {
      Json.obj(
        "id" -> post.id,
        "tweet" -> post.tweet,
        "metadata" -> post.metadata
      )
    }
  }
}

/**
  * Controls access to the backend data, returning [[TwitterResource]]
  */
class TwitterResourceHandler @Inject()(
    routerProvider: Provider[TwitterRouter],
    postRepository: TwitterRepository)(implicit ec: ExecutionContext) {

  def create(postInput: PostFormInput)(implicit mc: MarkerContext): Future[TwitterResource] = {
    val data = TwitterData(TwitterId("999"), postInput.title, postInput.body)
    // We don't actually create the post, so return what we have
    postRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[TwitterResource]] = {
    val postFuture = postRepository.get(TwitterId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[TwitterResource]] = {
    postRepository.list().map { postDataList =>
      postDataList.map(postData => createPostResource(postData))
    }
  }

  private def createPostResource(p: TwitterData): TwitterResource = {
    TwitterResource(p.id.toString, p.tweet, p.metadata)
  }

}
