package org.dmonix.area51.akka.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import org.dmonix.area51.akka.Configuration

import scala.collection.mutable

/**
  * @author Peter Nerg.
  */
class ClusterMember(cluster:Cluster) extends Actor with ActorLogging  {

  private var members = mutable.ListBuffer[Member]()

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    case MemberUp(member) =>
      members += member
      log.info(s"Member is Up: [${member.address} with roles [${member.roles}]], now active members [{$sortedMembers}]")

    case UnreachableMember(member) =>
      log.info(s"Member detected as unreachable: [$member]")
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Member is Removed: [{$member.address}] after [$previousStatus], now active members [{$sortedMembers}]")
      members -= member
    case _: MemberEvent => // ignore
  }

  private def sortedMembers = {
      members.sorted(Member.ageOrdering).map(_.address)
  }

}

object StartClusterMember extends App with ClusterSettings {
  val actorSystem = ActorSystem(actorSystemName, Configuration.memberCfg)

  val cluster = Cluster(actorSystem)

  //join in to the cluster
  cluster.joinSeedNodes(seedNodes)

  actorSystem.actorOf(Props(new ClusterMember(cluster)), "ClusterMember")

  sys.ShutdownHookThread {
    println("Starting Shutdown")
    cluster.leave(cluster.selfAddress)
    Thread.sleep(10000)
    println("Finished Shutdown")
  }
}

object StartClusterSeed extends App with ClusterSettings {
  val actorSystem = ActorSystem(actorSystemName, Configuration.seedCfg(6969))

  val cluster = Cluster(actorSystem)

  //join in to the cluster
  cluster.joinSeedNodes(seedNodes)

  actorSystem.actorOf(Props(new ClusterMember(cluster)), "ClusterMember")

  sys.ShutdownHookThread {
    println("Starting Shutdown")
    cluster.leave(cluster.selfAddress)
    println("Finished Shutdown")
  }

}