package com.powertwitter.cassandra.rabbit

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp._
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.scaladsl.Sink
import com.powertwitter.cassandra.cassandra.Cassandra


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

    val cassandraSink = cassandra.sinkInsert()

    val done = AmqpSource(
        TemporaryQueueSourceSettings(
          DefaultAmqpConnection,
          exchangeName
        ).withDeclarations(exchangeDeclaration),
        bufferSize = 1
    ).map( x => x.bytes.utf8String )
    .runWith( cassandraSink )
      //.runWith(Sink.foreach( x => println(x.bytes.utf8String) ) )

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


