package org.dmonix.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import org.dmonix.akka.cluster.SameThreadExecutionContext

import scala.collection.immutable

case class Message(s:String)
case class Response(s:String)

object ServiceConsumer {
  def props(mediator:ActorRef) = Props(new ServiceConsumer(mediator))
}

class ServiceConsumer(mediator:ActorRef) extends Actor with ActorLogging {

  mediator ! Send("/user/ServiceA", "Hello", false)
  mediator ! Send("/user/ServiceA", "Hello", false)
  mediator ! Send("/user/ServiceB", "Hello", false)

  def receive = {

    case m:Message =>
      mediator ! Send("/user/ServiceA",m,false)
      mediator ! Send("/user/ServiceB",m,false)
      mediator ! Send("/user/NO_SUCH_SERVICE",m,false) //This just seems to disappear into void, no logging or nothing
    case a:Any ⇒
      log.info(s"Service consumer [$self] Got [$a] from [$sender]")
  }

}

object ServiceConsumerStarter extends App with SameThreadExecutionContext {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-member-tcp.conf");

  val actorSystem = ActorSystem("ClusterTest")
  val mediator = DistributedPubSub(actorSystem).mediator
  val cluster = Cluster(actorSystem)
  //even the seed node must join in as otherwise it won't be part of the cluster
  cluster.joinSeedNodes(immutable.Seq(Address("akka.tcp", "ClusterTest", "127.0.0.1", 6969)))

  val actor = actorSystem.actorOf(ServiceConsumer.props(mediator))

  //appears that this sleep is needed for the communication to be up
  //sending something too early just causes the message to disappear into void
  Thread.sleep(2000)

  actor ! Message("Hello!!!")

  Thread.sleep(2000)
  actorSystem.terminate().onComplete(_ => System.exit(1))
}
