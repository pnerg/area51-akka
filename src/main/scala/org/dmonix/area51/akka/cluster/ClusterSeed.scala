package org.dmonix.area51.akka.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

/**
  * This class just acts as a cluster seed.
  * That is a connection point for all other members to join in to.
  * It doesn't do anything apart from logging who joins/leaves.
  * @author Peter Nerg
  */
class ClusterSeed(cluster:Cluster) extends Actor with ActorLogging {


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

object StartClusterSeed extends App with ClusterSettings {
  val actorSystem = ActorSystem(actorSystemName, Configuration.seedCfg(6969))

  val cluster = Cluster(actorSystem)

  //even the seed node must join in as otherwise it won't be part of the cluster
  cluster.joinSeedNodes(seedNodes)

  actorSystem.actorOf(Props(new ClusterSeed(cluster)))

}
