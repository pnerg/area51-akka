package org.dmonix.area51.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import org.dmonix.area51.akka.cluster.ClusterSettings
import org.dmonix.area51.akka.cluster.Messages._

/**
  * Emulates a provider of a service registered on Akka Cluster/Distributed
  * A rather stupid service that receives the Message class and responds with Response.
  * The actor itself is not aware it's part of a cluster, it merely reacts to messages sent to it.
  * @author Peter Nerg
  */
class ServiceProvider extends Actor with ActorLogging {
  override def preStart():Unit = {
    println(s"Starting [$self]")
  }

  def receive = {
    case Message(msg) ⇒
      log.info(s"Service provider [$self] Got [$msg] from [$sender]")
      sender ! Response(s"Response to [$msg]")
    case a:Any ⇒
      log.warning(s"Service provider [$self] got unexpected message [$a] from [$sender]")
  }

}

object ServiceProviderStarter extends App with ClusterSettings {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-ext-member-tcp.conf");
  val actorSystem = ActorSystem(actorSystemName)
  val cluster = Cluster(actorSystem)
  //even the seed node must join in as otherwise it won't be part of the cluster
  cluster.joinSeedNodes(seedNodes)

  val mediator = DistributedPubSub(actorSystem).mediator

  def registerService(serviceName:String): Unit = {
    val actor = actorSystem.actorOf(Props(new ServiceProvider), serviceName)
    mediator ! Put(actor)
  }

  registerService("ServiceA")
  registerService("ServiceB")
}
