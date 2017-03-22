package org.dmonix.area51.akka

import akka.actor.{Actor, ActorLogging, ActorSystem, Inbox, Props}

import scala.concurrent.duration.DurationInt
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
case object Ping
case object Pong

class PongActor extends Actor
  with ActorLogging {
  override def receive = {
    case Ping =>
      log.info("Received Ping")
      Thread.sleep(7000)
      sender ! Pong
  }
}


/**
  * @author Peter Nerg
  */
object SendingFromNonActor extends App {
  val as = ActorSystem("SendingFromNonActor")
  val ref = as.actorOf(Props(new PongActor), "Pong")

  implicit val timeout = 5.seconds
  val a = ask(ref, Ping)(Timeout(timeout))
  val res = Await.ready(a, 10.seconds)

//  implicit val inbox = Inbox.create(as)
//  ref ! Ping
//  inbox.receive(5.seconds)
  println("Got:"+res)
  as.terminate()
}
