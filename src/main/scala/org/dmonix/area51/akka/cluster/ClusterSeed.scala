/*
 * Created : 9/9/16
 *
 * Copyright (c) 2016 Ericsson AB, Sweden.
 * All rights reserved.
 * The Copyright to the computer program(s) herein is the property of Ericsson AB, Sweden.
 * The program(s) may be used and/or copied with the written permission from Ericsson AB
 * or in accordance with the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */
package org.dmonix.area51.akka.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

/**
  * This class just acts as a cluster seed.
  * That is a connection point for all other member to join in to.
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
  //the only config file with hard wired port
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-seed-tcp.conf");

  val actorSystem = ActorSystem(actorSystemName)

  val cluster = Cluster(actorSystem)

  //even the seed node must join in as otherwise it won't be part of the cluster
  cluster.joinSeedNodes(seedNodes)

  actorSystem.actorOf(Props(new ClusterSeed(cluster)))

}
