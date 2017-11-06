package rabbitmq

import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.alpakka.amqp._
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import play.api.libs.json.{JsResult, JsValue, Json, Writes}

import scala.concurrent.ExecutionContext.Implicits.global
import com.powertwitter.model._
import akka.util.Timeout

import scala.concurrent.duration._
import akka.pattern.ask

import scala.collection.mutable.ListBuffer



object RabbitActor {
  val ExchangeNameSelect = "powertwitter_select"
  val ExchangeNameInsert = "powertwitter_insert"
  val ExchangeNameSelectUpdates = "powertwitter_select_updates"
  val ExchangeDeclarationInsert = ExchangeDeclaration(ExchangeNameInsert, "fanout")
  val ExchangeDeclarationSelect = ExchangeDeclaration(ExchangeNameSelect, "fanout")
  val ExchangeDeclarationSelectUpdates = ExchangeDeclaration(ExchangeNameSelectUpdates, "fanout")

  case class SubscribeMe(actorRef: ActorRef)
}


class RabbitActor extends Actor {
  import RabbitActor._
  import com.powertwitter.model.TwitterData._

  var all = ListBuffer[TwitterData]()
  var actors = List[ActorRef]()

  implicit val Timeout: Timeout = 4 seconds


  override def receive = {
    case td : TwitterData => sendTwitterData(td)
    case "ALL" => sender ? all
    case SubscribeMe( me ) => {
        actors = me :: actors
        receiveFromRabbit(me)
    }
    case _ => ???

  }

  override def preStart(): Unit = {
    super.preStart()
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

  def receiveFromRabbit( resActor : ActorRef ): Unit = {

    val sourceAll = AmqpSource(
      TemporaryQueueSourceSettings(
        DefaultAmqpConnection,
        ExchangeNameSelect
    ).withDeclarations(ExchangeDeclarationSelect),
    bufferSize = 1 )

    val sourceUpdate = AmqpSource(
      TemporaryQueueSourceSettings(
        DefaultAmqpConnection,
        ExchangeNameSelectUpdates
      ).withDeclarations(ExchangeDeclarationSelectUpdates),
      bufferSize = 1 )

    val sources = sourceAll ++ sourceUpdate


    val done =
      sources.map( x => x.bytes.utf8String )
      .map( x => if(x.length == 0) {all.clear(); Json.parse("{}")} else Json.parse(x) )
      .map( TwitterData.implicitReads.reads )
      .filter( x => x.isSuccess )
    .runForeach( x => returnToActor(resActor, x) )

  }

  def returnToActor(ar : ActorRef, js: JsResult[TwitterData]): Unit = {
    ar ! js.get
  }

}
