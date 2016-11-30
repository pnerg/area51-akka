package org.dmonix.area51.akka.cluster

import akka.actor.{Actor, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}

case object Terminated

class Electee extends Actor {
  override def receive = {
    case a:Any => println(a)
  }
}

abstract class MasterElection extends App with ClusterSettings {

  def joinElection(actorSystem: ActorSystem): Unit = {
    val cluster = Cluster(actorSystem)
    //join in to the cluster
    cluster.joinSeedNodes(seedNodes)

    //  actorSystem.actorOf(ClusterSingletonManager.props(Props(new Electee), Terminated, ClusterSingletonManagerSettings(actorSystem).withSingletonName("Test")))
    actorSystem.actorOf(ClusterSingletonManager.props(Props(classOf[Electee]), Terminated, ClusterSingletonManagerSettings(actorSystem).withSingletonName("Test")))
  }

}

/**
  * @author Peter Nerg
  */
object StartMasterElectionSeed extends MasterElection {
  val actorSystem = ActorSystem(actorSystemName, Configuration.seedCfg(6969))
  joinElection(actorSystem)
}

/**
  * @author Peter Nerg
  */
object StartMasterElectionMember extends MasterElection {
  val actorSystem = ActorSystem(actorSystemName, Configuration.memberCfg)
  joinElection(actorSystem)
}

