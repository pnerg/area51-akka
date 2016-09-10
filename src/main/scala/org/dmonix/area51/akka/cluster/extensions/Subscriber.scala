package org.dmonix.area51.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import org.dmonix.area51.akka.cluster.ClusterSettings
import org.dmonix.area51.akka.cluster.Messages.{Message, Response}

/**
  * Works as a subscriber in the publish/subscribe pattern provided by the cluster extensions
  * @author Peter Nerg
  */
class Subscriber extends Actor with ActorLogging {

  def receive = {
    case Message(msg) =>
      log.info(s"Subscriber [$self] Got [$msg] from [$sender]")
      sender ! Response(s"Response to [$msg]")
    case SubscribeAck(Subscribe(topic, group, _)) =>
      log.info(s"Subscriber [$self] is now subscribed to topic [$topic] with group [$group]");
    case a:Any =>
      log.warning(s"Subscriber [$self] got unexpected message [$a] from [$sender]")
  }

}

object SubscriberStarter extends App with ClusterSettings {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-ext-member-tcp.conf");
  val actorSystem = ActorSystem(actorSystemName)
  val cluster = Cluster(actorSystem)
  cluster.joinSeedNodes(seedNodes)

  val mediator = DistributedPubSub(actorSystem).mediator

  def registerService(serviceName:String): Unit = {
    val actor = actorSystem.actorOf(Props(new Subscriber), serviceName)
    //Get different behavior if subscribed with a group or not.
//    val group = Option("group")
    val group = None
    mediator.tell(Subscribe(serviceName, group, actor),actor) //registers the actor to subscribe to the topic with the provided service name
  }

  registerService("ServiceA")
  registerService("ServiceB")
}
