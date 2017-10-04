package rabbitmq

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.alpakka.amqp._
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import play.api.libs.json.{JsValue, Json, Writes}

import scala.concurrent.ExecutionContext.Implicits.global
import com.powertwitter.model._
import akka.util.Timeout

import scala.concurrent.duration._
import akka.pattern.ask

import scala.collection.mutable.ListBuffer



object RabbitActor {
  val ExchangeNameSelect = "powertwitter_select"
  val ExchangeNameInsert = "powertwitter_insert"
  val ExchangeDeclarationInsert = ExchangeDeclaration(ExchangeNameInsert, "fanout")
  val ExchangeDeclarationSelect = ExchangeDeclaration(ExchangeNameSelect, "fanout")
}


class RabbitActor extends Actor {
  import RabbitActor._
  import com.powertwitter.model.TwitterData._

  var all = ListBuffer[TwitterData]()

  implicit val Timeout: Timeout = 4 seconds


  override def receive = {
    case td : TwitterData => sendTwitterData(td)
    case "ALL" => sender ? all
    case _ => ???

  }

  override def preStart(): Unit = {
    super.preStart()
    this.receiveFromRabbit()
  }

  implicit val materializer = ActorMaterializer()

  val connectionSettings =
    AmqpConnectionDetails(List(("localhost", 5672)))

  val amqpSink = AmqpSink.simple(
    AmqpSinkSettings(connectionSettings)
      .withExchange(ExchangeNameInsert)
      .withDeclarations(ExchangeDeclarationInsert)
  )

  def sendTwitterData(nt : TwitterData) = {
    val json : String = Json.toJson(nt).toString()
    val done = Source( Vector(json) )
      .map(s => ByteString(s))
      .runWith(amqpSink)
    done.onComplete( f => println("finised " + f))
  }

  def receiveFromRabbit(): Unit = {
    val done = AmqpSource(
      TemporaryQueueSourceSettings(
        DefaultAmqpConnection,
        ExchangeNameSelect
      ).withDeclarations(ExchangeDeclarationSelect),
      bufferSize = 1
    ).map( x => x.bytes.utf8String )
      .map( x => if(x.length == 0) {all.clear(); Json.parse("{}")} else Json.parse(x) )
      .map( TwitterData.implicitReads.reads )
      .filter( x => x.isSuccess )
    .runForeach( x => all += x.get )

    done.onComplete( println )
  }
}
