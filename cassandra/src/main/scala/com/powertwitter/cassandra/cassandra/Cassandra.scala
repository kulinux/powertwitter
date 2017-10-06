package com.powertwitter.cassandra.cassandra

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSink, CassandraSource}
import akka.stream.scaladsl.{Sink, Source}
import com.datastax.driver.core.{Cluster, PreparedStatement, Row, SimpleStatement}
import com.powertwitter.model.TwitterData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Cassandra {
  implicit val session = Cluster.builder.addContactPoint("127.0.0.1").withPort(9042).build.connect()

  def mapSelect(r : Row): TwitterData = {
    TwitterData( r.getObject("id").toString,
      r.getString("tweet"),
      r.getString("metadata"))
  }

}

class Cassandra(implicit system : ActorSystem,
                implicit val mat : ActorMaterializer) {

  import Cassandra._

  initSchema();

  def select() = {
    val stmt = new SimpleStatement("SELECT * FROM Twitter.tweet")
      .setFetchSize(20)

    CassandraSource(stmt)
      .map( mapSelect _ )

  }


  def insert() = {
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

}
