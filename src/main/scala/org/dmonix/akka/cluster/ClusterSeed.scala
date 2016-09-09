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
package org.dmonix.akka.cluster

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object ClusterSeed {
  def props() = Props(new ClusterSeed)
}

/**
  * @author Peter Nerg
  */
class ClusterSeed extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }
}

object StartClusterSeed extends App {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-seed-tcp.conf");

  val actorSystem = ActorSystem("ClusterSeed")

  actorSystem.actorOf(ClusterSeed.props())
}
