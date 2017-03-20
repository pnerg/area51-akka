package org.dmonix.area51.akka

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import org.dmonix.area51.akka.cluster.SameThreadExecutionContext

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class ExplodeLogActor extends Actor with ActorLogging {
  import context.dispatcher
//  log.info("start")
  Future {
    Thread.sleep(750)
  }.onComplete{case _ =>
    //will explode d
    log.info("F complete")
  }

  override def receive = {
    case _ =>
  }
}

object T extends App with SameThreadExecutionContext {
  val actorSystem = ActorSystem("WTF")
  actorSystem.actorOf(Props(new ExplodeLogActor))

  val res = actorSystem.actorSelection("*/Whatever").resolveOne(5.seconds)
  def time = System.currentTimeMillis()

  res.onComplete({case _ => println(System.currentTimeMillis()-time)})

  Thread.sleep(200)
//  Await.ready(actorSystem.terminate(), 2.seconds)
}
