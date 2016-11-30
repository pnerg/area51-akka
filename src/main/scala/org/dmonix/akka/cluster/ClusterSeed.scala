package org.dmonix.akka.cluster

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object ClusterSeed {
  def props = Props(new ClusterSeed)
}

/**
  * @author Peter Nerg
  */
class ClusterSeed extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

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

object StartClusterSeed extends App {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-seed-tcp.conf");

  val actorSystem = ActorSystem("ClusterTest")

  actorSystem.actorOf(ClusterSeed.props)
}
