package org.dmonix.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Put

import scala.collection.immutable

object ServiceProvider {
  def props = Props(new ServiceProvider)
}
/**
  * Emulates a provider of a service registered on Akka Cluster/Distributed
  *
  * @author Peter Nerg
  */
class ServiceProvider extends Actor with ActorLogging {
  override def preStart():Unit = {
    println(s"Starting [$self]")
  }

  def receive = {
    case a:Any ⇒
      log.info(s"Service provider [$self] Got [$a]")
      sender ! "ACK"
  }

}



object ServiceProviderStarter extends App {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-seed-tcp.conf");
  val actorSystem = ActorSystem("ClusterTest")
  val cluster = Cluster(actorSystem)
  //even the seed node must join in as otherwise it won't be part of the cluster
  cluster.joinSeedNodes(immutable.Seq(Address("akka.tcp", "ClusterTest", "127.0.0.1", 6969)))

  val mediator = DistributedPubSub(actorSystem).mediator




  def registerService(serviceName:String): Unit = {
    val actor = actorSystem.actorOf(ServiceProvider.props, serviceName)
    mediator ! Put(actor)
  }

  registerService("ServiceA")
  registerService("ServiceB")
}
