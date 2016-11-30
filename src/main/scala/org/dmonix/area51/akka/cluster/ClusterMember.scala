package org.dmonix.area51.akka.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

/**
  * @author Peter Nerg.
  */
class ClusterMember(cluster:Cluster) extends Actor with ActorLogging  {

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)
  def receive = {
    case MemberUp(member) =>
      log.info(s"Member is Up: [${member.address} with roles [${member.roles}]]")
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case _: MemberEvent => // ignore
  }
}

object StartClusterMember extends App with ClusterSettings {
  val actorSystem = ActorSystem(actorSystemName, Configuration.memberCfg)

  val cluster = Cluster(actorSystem)

  //join in to the cluster
  cluster.joinSeedNodes(seedNodes)

  actorSystem.actorOf(Props(new ClusterMember(cluster)))
  actorSystem.actorOf(Props(new ClusterMember(cluster)))

}