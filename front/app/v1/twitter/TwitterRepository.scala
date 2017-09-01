package v1.twitter

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}
import rabbitmq.RabbitActor

import scala.concurrent.Future

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


class PostExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait TwitterRepository {
  def create(data: TwitterData)(implicit mc: MarkerContext): Future[TwitterId]

  def list()(implicit mc: MarkerContext): Future[Iterable[TwitterData]]

  def get(id: TwitterId)(implicit mc: MarkerContext): Future[Option[TwitterData]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class TwitterRepositoryImpl @Inject()()(implicit ec: PostExecutionContext) extends TwitterRepository {

  implicit val system = ActorSystem.create("system")
  val rb = system.actorOf(Props[RabbitActor])

  private val logger = Logger(this.getClass)

  private val postList = List(
    TwitterData(TwitterId("1"), "title 1", "blog post 1"),
    TwitterData(TwitterId("2"), "title 2", "blog post 2"),
    TwitterData(TwitterId("3"), "title 3", "blog post 3"),
    TwitterData(TwitterId("4"), "title 4", "blog post 4"),
    TwitterData(TwitterId("5"), "title 5", "blog post 5")
  )

  override def list()(implicit mc: MarkerContext): Future[Iterable[TwitterData]] = {
    Future {
      logger.trace(s"list: ")
      postList
    }
  }

  override def get(id: TwitterId)(implicit mc: MarkerContext): Future[Option[TwitterData]] = {
    Future {
      logger.trace(s"get: id = $id")
      postList.find(post => post.id == id)
    }
  }

  def create(data: TwitterData)(implicit mc: MarkerContext): Future[TwitterId] = {
    Future {
      logger.trace(s"create: data = $data")
      rb ! data
      data.id
    }
  }

}
