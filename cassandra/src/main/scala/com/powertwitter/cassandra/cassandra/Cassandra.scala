package com.powertwitter.cassandra.cassandra

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSink, CassandraSource}
import akka.stream.scaladsl.{Sink, Source}
import com.datastax.driver.core.{Cluster, PreparedStatement, SimpleStatement}
import com.powertwitter.model.TwitterData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Cassandra {
  implicit val session = Cluster.builder.addContactPoint("127.0.0.1").withPort(9042).build.connect()

}

class Cassandra(implicit system : ActorSystem,
                implicit val mat : ActorMaterializer) {

  import Cassandra._

  def insert() = {

    val sink: Sink[String, Future[Done]] = sinkInsert

    //val source = Source(Vector(1, 2, 3))
    val source: Source[String, NotUsed] = Source( Vector("cuatro", "cinco", "seis") )
    source.runWith( sink )

  }

  def sinkInsert() = {
    val preparedStatement =
      session.prepare("INSERT INTO Twitter.tweet(id, tweet) VALUES (now(), ?)")

    val statementBinder =
      (tweet: String, statement: PreparedStatement) => {
        statement.bind(tweet)
      }

    CassandraSink[String](parallelism = 2,
      preparedStatement, statementBinder)

  }

  def sinkInsertTweet() = {
    val preparedStatement =
      session.prepare(
        """
          |INSERT INTO Twitter.tweet(id, tweet, metadata)
          |VALUES (now(), ?, ?)
          |""".stripMargin)

    val statementBinder =
      (tweet: TwitterData, statement: PreparedStatement) => {
        statement
          .bind(tweet.tweet, tweet.metadata)
      }

    CassandraSink[TwitterData](parallelism = 2,
      preparedStatement, statementBinder)

  }

  def initSchema() = {
    session.execute(
      """
        |CREATE KEYSPACE IF NOT EXISTS Twitter
        |  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };
      """.stripMargin)

    session.execute(
      """
    |CREATE TABLE IF NOT EXISTS Twitter.tweet
    |(id uuid PRIMARY KEY, tweet text, metadata text);
      """.stripMargin)

  }

  def test() = {
    val stmt = new SimpleStatement("SELECT * FROM Twitter.tweet")
      .setFetchSize(20)

    val rows = CassandraSource(stmt).runWith(Sink.seq)

    rows.onComplete(
      y => y.foreach( u => u.foreach(println) )
    )
  }


}
