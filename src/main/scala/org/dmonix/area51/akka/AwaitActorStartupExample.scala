package org.dmonix.area51.akka

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * A dummy actor that has a configurable delay for the start phase
  * @param startDelay The
  * @author Peter Nerg
  */
class SlowStartActorActor(startDelay:FiniteDuration) extends Actor with ActorLogging {

  log.info("Create")

  override def preStart(): Unit = {
    log.info(s"PreStart [$startDelay]")
    Thread.sleep(startDelay.toMillis)
    log.info("PreStarted")
  }

  override def receive: Receive = {
    case a: Any => log.info("PlayAroundActor:" + a)
  }

}

object ActorStartWaiter {
  def waitForActors(actors: Seq[ActorRef])(implicit as:ActorSystem, waitTime:FiniteDuration, ec:ExecutionContext):Boolean = {

    //results in a sequence with Future[Boolean]
    val resolveFutures = actors.
      map(resolve(_)). //maps each ActorRef by performing a resolve op
      map(isSuccess(_)) //maps the result of the resolve op to Boolean

    //results in a single Future[Boolean]
    val totalResult = Future.sequence(resolveFutures) //sequences the Seq[Future[Boolean]] to a single future with Seq[Boolean]
      .map(result => result.forall(_ == true)) //maps/reduces the Seq[Boolean] to a single boolean

    //double the wait time for the Future so we don't time out here
    //this timeout will never anyways be reached, it's the resolveOne timeout that dictates
    Await.result(totalResult, waitTime*2)
  }

  private def isSuccess(f:Future[ActorRef])(implicit ec:ExecutionContext):Future[Boolean] = {
    f.map(ref => {
      println(s"Resolved actor [${ref.path}]")
      true
    }).recover {case ex:Throwable =>
      println(s"Failed to resolve actor within specified time [${ex.getMessage}]")
      false
    }
  }

  private def resolve(actor:ActorRef)(implicit as:ActorSystem, waitTime:FiniteDuration):Future[ActorRef] = {
    as.actorSelection(actor.path).resolveOne(waitTime)
  }

}

/**
  * Simple example on how one can go about to verify that an actor (ActorRef) exists and is fully started.
  * Use case could be when bootstrapping a number of actors that potentially have long start phases.
  * @author Peter Nerg
  */
object AwaitActorStartup extends App {
  import ActorStartWaiter._
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val as = ActorSystem("AwaitActorStartup")
  implicit val maxWaitTime = 2.seconds

  //play around with the start delay and wait time to get various results
  val actors = Seq(
    as.actorOf(Props(new SlowStartActorActor(3.seconds)), "SlowStartActorActor-1"),
    as.actorOf(Props(new SlowStartActorActor(0.seconds)), "SlowStartActorActor-2"),
    as.actorOf(Props(new SlowStartActorActor(2.seconds)), "SlowStartActorActor-3")
  )

  //will block and wait for all actors to either start or the timeout to pass
  if(waitForActors(actors)) {
    println("Successfully resolved all actors")
  }
  else {
    println("Failed to resolve one or more actors")
  }

  //here only to cut the actor system after the above tests have been performed
  Await.ready(as.terminate(), 15.seconds)


}