package org.dmonix.area51.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import org.dmonix.area51.akka.cluster.ClusterSettings

/**
  * Acts as a registry and cluster seed for both the ServiceProvider and ServiceConsumer classes
  * That is a connection point for all other members to join in to.
  * It doesn't do anything apart from logging who joins/leaves.
  * This class needs to be started before any of the classes: ServiceConsumer, ServiceProvider, Subscriber, Publisher
  * @author Peter Nerg
  */
class ClusterExtSeed(cluster:Cluster) extends Actor with ActorLogging {
  /**
   *Subscribe to cluster changes, re-subscribe when restart
   */
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      log.info(s"Member is Up: [${member.address}] with roles [${member.roles}]] and status [${member.status}] and unique address [${member.uniqueAddress}]")
    case UnreachableMember(member) =>
      log.info(s"Member detected as unreachable: [$member]")
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member [$member] is removed, prev status [$previousStatus]")
    case e:MemberEvent =>
      log.info(s"Got member event [$e]")
    case a:Any ⇒
      log.warning(s"Cluster seed [$self] got unexpected message [$a] from [$sender]")
  }
}

object StartClusterExtSeed extends App with ClusterSettings {
  //the only config file with hard wired port
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-ext-seed-tcp.conf");

  val actorSystem = ActorSystem(actorSystemName)

  val cluster = Cluster(actorSystem)

  //even the seed node must join in as otherwise it won't be part of the cluster
  cluster.joinSeedNodes(seedNodes)

  //this is needed as otherwise this actor system will spew logs about dropped messages from the remote mediator instances
  DistributedPubSub(actorSystem).mediator

  actorSystem.actorOf(Props(new ClusterExtSeed(cluster)))
}

