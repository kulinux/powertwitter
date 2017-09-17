package rabbitmq

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.alpakka.amqp._
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import play.api.libs.json.{JsValue, Json, Writes}
import rabbitmq.RabbitActor.exchangeName

import scala.concurrent.ExecutionContext.Implicits.global

import com.powertwitter.model._


object RabbitActor {
  val exchangeName = "powertwitter"

  /**
    * Mapping to write a PostResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[TwitterData] {
    def writes(post: TwitterData): JsValue = {
      Json.obj(
        "id" -> post.id.underlying,
        "tweet" -> post.tweet,
        "metadata" -> post.metadata
      )
    }
  }

}

object MainProducer extends App {
  implicit val system = ActorSystem.create("system")
  val rb = system.actorOf(Props[RabbitActor])


  1 to 20000 foreach( x => {
    val td = TwitterData(TwitterId(x.toString), "tweet info", "{mono}")
    rb ! td
    Thread.sleep(100)
  })
}


class RabbitActor extends Actor {
  import RabbitActor._

  override def receive = {
    case td : TwitterData => sender(td)
    case _ => ???

  }

  //implicit val system = ActorSystem.create("system")
  implicit val materializer = ActorMaterializer()

  val exchangeDeclaration = ExchangeDeclaration(exchangeName, "fanout")

  val connectionSettings =
    AmqpConnectionDetails(List(("localhost", 5672)))

  val amqpSink = AmqpSink.simple(
    AmqpSinkSettings(connectionSettings)
      .withExchange(exchangeName)
      .withDeclarations(exchangeDeclaration)
  )

  def sender(nt : TwitterData) = {

    val json : String = Json.toJson(nt).toString()
    val input = Vector(json)
    val done = Source(input)
      .map(s => ByteString(s))
      .runWith(amqpSink)
    done.onComplete( f => println("finised " + f))
  }
}
