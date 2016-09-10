package org.dmonix.area51.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import org.dmonix.area51.akka.cluster.ClusterSettings

/**
  * Acts as a registry and cluster seed for both the ServiceProvider and ServiceConsumer classes
  * @author Peter Nerg
  */
class ServiceRegistry(cluster:Cluster) extends Actor with ActorLogging {
  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      log.info(s"Member is Up: [${member.address}] with roles [${member.roles}]] and status [${member.status}] and unique address [${member.uniqueAddress}]")
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case _: MemberEvent => // ignore
    case a:Any ⇒
      log.warning(s"Cluster seed [$self] got unexpected message [$a] from [$sender]")
  }
}

object StartServiceRegistry extends App with ClusterSettings {
  //the only config file with hard wired port
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-ext-seed-tcp.conf");

  val actorSystem = ActorSystem(actorSystemName)

  val cluster = Cluster(actorSystem)

  //even the seed node must join in as otherwise it won't be part of the cluster
  cluster.joinSeedNodes(seedNodes)
  DistributedPubSub(actorSystem).mediator

  actorSystem.actorOf(Props(new ServiceRegistry(cluster)))

}

