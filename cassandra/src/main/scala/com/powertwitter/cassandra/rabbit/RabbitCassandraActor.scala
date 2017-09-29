package com.powertwitter.cassandra.rabbit

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp._
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.util.ByteString
import com.powertwitter.cassandra.cassandra.Cassandra
import com.powertwitter.model.TwitterData
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global


object RabbitCassandraActor {
  val exchangeNameSelect = "powertwitter_select"
  val exchangeNameInsert = "powertwitter_insert"

  val exchangeDeclarationInsert = ExchangeDeclaration(exchangeNameInsert, "fanout")
  val exchangeDeclarationSelect = ExchangeDeclaration(exchangeNameSelect, "fanout")

  def props(): Props = Props(new RabbitCassandraActor())
}

class RabbitCassandraActor extends Actor {

  import RabbitCassandraActor._
  import com.powertwitter.model.TwitterData._

  implicit val materializer = ActorMaterializer()
  implicit val system = context.system

  val connectionSettings =
    AmqpConnectionDetails(List(("localhost", 5672)))

  val cassandra = new Cassandra()

  val amqpSink = AmqpSink.simple(
    AmqpSinkSettings(connectionSettings).
      withDeclarations(exchangeDeclarationInsert)
  )

  val amqpSelect = AmqpSink.simple(
    AmqpSinkSettings(connectionSettings)
      .withExchange(exchangeNameSelect)
      .withDeclarations(exchangeDeclarationSelect)
  )


  def fromRabbitToCassandra(): Unit =  {

    cassandra.initSchema()

    val cassandraSink = cassandra.insert()

    val done = AmqpSource(
        TemporaryQueueSourceSettings(
          DefaultAmqpConnection,
          exchangeNameInsert
        ).withDeclarations(exchangeDeclarationInsert),
        bufferSize = 1
    ).map( x => x.bytes.utf8String )
    .map( mapToTwitterData )
    .runWith( cassandraSink )

    done.failed.onComplete( x => x.get.printStackTrace() )
    done.onComplete( println )

    println( done )

  }

  def fromCassandraToRabbit() = {

    val done = cassandra.select()
      .map( TwitterData.implicitWrites.writes _ )
      .map( Json.stringify _ )
      .map(s => ByteString(s))
      .runWith( amqpSelect )

    done.onComplete( x => println(s"read $x") )

  }

  def mapToTwitterData(str : String): TwitterData = {
    val js = Json.parse(str)
    val tw = TwitterData.implicitReads.reads(js)
    tw.get
  }



  def receive = {
    case "FROM_RABBIT_TO_CASSANDRA" => fromRabbitToCassandra()
    case "FROM_CASSANDRA_TO_RABBIT" => fromCassandraToRabbit()
  }

  override def preStart(): Unit = {

    import scala.concurrent.duration._
    self ! "FROM_RABBIT_TO_CASSANDRA"
    self ! "FROM_CASSANDRA_TO_RABBIT"

    system.scheduler.schedule(1 seconds, 10 seconds, self, "FROM_CASSANDRA_TO_RABBIT")
  }
}


object MainConsumer extends App {
  implicit val system = ActorSystem.create("system")
  val rb = system.actorOf(RabbitCassandraActor.props())
}


