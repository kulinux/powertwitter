package rabbitmq

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.alpakka.amqp._
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import play.api.libs.json.{JsValue, Json, Writes}

import scala.concurrent.ExecutionContext.Implicits.global

import com.powertwitter.model._


object RabbitActor {
  val exchangeNameSelect = "powertwitter_select"
  val exchangeNameInsert = "powertwitter_insert"
}

object MainProducer extends App {
  implicit val system = ActorSystem.create("system")
  val rb = system.actorOf(Props[RabbitActor])


  1 to 20000 foreach( x => {
    val td = TwitterData(x.toString, "tweet info", "{mono}")
    rb ! td
    Thread.sleep(100)
  })
}


class RabbitActor extends Actor {
  import RabbitActor._
  import com.powertwitter.model.TwitterData._



  override def receive = {
    case td : TwitterData => sender(td)
    case _ => ???

  }

  override def preStart(): Unit = {
    super.preStart()
    this.receiveFromRabbit()
  }

  implicit val materializer = ActorMaterializer()

  val exchangeDeclarationInsert = ExchangeDeclaration(exchangeNameInsert, "fanout")
  val exchangeDeclarationSelect = ExchangeDeclaration(exchangeNameInsert, "fanout")

  val connectionSettings =
    AmqpConnectionDetails(List(("localhost", 5672)))

  val amqpSink = AmqpSink.simple(
    AmqpSinkSettings(connectionSettings)
      .withExchange(exchangeNameInsert)
      .withDeclarations(exchangeDeclarationInsert)
  )

  def sender(nt : TwitterData) = {

    val json : String = Json.toJson(nt).toString()
    val input = Vector(json)
    val done = Source(input)
      .map(s => ByteString(s))
      .runWith(amqpSink)
    done.onComplete( f => println("finised " + f))
  }

  def receiveFromRabbit(): Unit = {
    val done = AmqpSource(
      TemporaryQueueSourceSettings(
        DefaultAmqpConnection,
        exchangeNameSelect
      ).withDeclarations(exchangeDeclarationSelect),
      bufferSize = 1
    ).map( x => x.bytes.utf8String )
      .runForeach(x => println(s"Received $x"))
  }
}
