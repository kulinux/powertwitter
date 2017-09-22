package com.powertwitter.cassandra.rabbit

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp._
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.scaladsl.Sink
import com.powertwitter.cassandra.cassandra.Cassandra
import com.powertwitter.model.TwitterData
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global


object RabbitCassandraActor {
  val exchangeName = "powertwitter"
  def props(): Props = Props(new RabbitCassandraActor())
}

class RabbitCassandraActor extends Actor {

  import RabbitCassandraActor._

  implicit val materializer = ActorMaterializer()
  implicit val system = context.system

  val cassandra = new Cassandra()


  def listenRabbitQueue(): Unit =  {
    val exchangeDeclaration = ExchangeDeclaration(exchangeName, "fanout")

    val connectionSettings =
      AmqpConnectionDetails(List(("localhost", 5672)))


    val amqpSink = AmqpSink.simple(
      AmqpSinkSettings(connectionSettings).
        withDeclarations(exchangeDeclaration)
    )

    cassandra.initSchema()

    val cassandraSink = cassandra.sinkInsertTweet()

    val done = AmqpSource(
        TemporaryQueueSourceSettings(
          DefaultAmqpConnection,
          exchangeName
        ).withDeclarations(exchangeDeclaration),
        bufferSize = 1
    ).map( x => x.bytes.utf8String )
    .map( mapToTwitterData )
    .runWith( cassandraSink )
      //.runWith(Sink.foreach( x => println(x.bytes.utf8String) ) )

    done.failed.onComplete( x => x.get.printStackTrace() )
    done.onComplete( println )


    println( done )

  }

  def mapToTwitterData(str : String): TwitterData = {
    val js = Json.parse(str)
    val tw = TwitterData.implicitReads.reads(js)
    tw.get
  }



  def receive = {
    case "START" => listenRabbitQueue()
  }

  override def preStart(): Unit = self ! "START"
}


object MainConsumer extends App {
  implicit val system = ActorSystem.create("system")
  val rb = system.actorOf(RabbitCassandraActor.props())
}


