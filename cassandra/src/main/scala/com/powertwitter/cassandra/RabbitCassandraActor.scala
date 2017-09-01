package com.powertwitter.cassandra

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp._
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.scaladsl.{Sink, Source}


object RabbitCassandraActor {
  val exchangeName = "powertwitter"
  def props(): Props = Props(new RabbitCassandraActor())
}

class RabbitCassandraActor extends Actor {

  import RabbitCassandraActor._

  implicit val materializer = ActorMaterializer()


  def listenRabbitQueue(): Unit =  {
    val exchangeDeclaration = ExchangeDeclaration(exchangeName, "fanout")

    val connectionSettings =
      AmqpConnectionDetails(List(("localhost", 5672)))


    val amqpSink = AmqpSink.simple(
      AmqpSinkSettings(connectionSettings).
        withDeclarations(exchangeDeclaration)
    )

    val done = AmqpSource(
        TemporaryQueueSourceSettings(
          DefaultAmqpConnection,
          exchangeName
        ).withDeclarations(exchangeDeclaration),
        bufferSize = 1
    ).runWith(Sink.foreach( x => println(x.bytes.utf8String) ) )




    /*
    val fanoutSize = 4

    val mergedSources = (0 until fanoutSize).foldLeft(Source.empty[(Int, String)]) {
      case (source, fanoutBranch) =>
        source.merge(
          AmqpSource(
            TemporaryQueueSourceSettings(
              DefaultAmqpConnection,
              exchangeName
            ).withDeclarations(exchangeDeclaration),
            bufferSize = 1
          ).map(msg => (fanoutBranch, msg.bytes.utf8String))
        )
    }
    */


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


