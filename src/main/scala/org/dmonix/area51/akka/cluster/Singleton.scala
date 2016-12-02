package org.dmonix.area51.akka.cluster

import akka.actor.{Actor, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}

case object Terminated
case class Request(msg:String)
case class Response(msg:String)

class Electee(groupName:String) extends Actor {
  println(s"Created Electee for [$groupName] [$self]")
  override def receive = {
    case Request(msg) => {
      println(s"Electee [$self] received [$msg]")
      sender ! Response(msg)
    }
    case a:Any => {
      println(s"Electee [$self] received [$a]")
    }
  }
}

abstract class Singleton extends App with ClusterSettings {

  val singletonName = "Group-1"

  def joinCluster(actorSystem: ActorSystem): Unit = {
    val cluster = Cluster(actorSystem)
    //join in to the cluster
    cluster.joinSeedNodes(seedNodes)
  }

  def startSingleton(actorSystem: ActorSystem, name:String): Unit = {
    actorSystem.actorOf(ClusterSingletonManager.props(Props(classOf[Electee], name), Terminated, ClusterSingletonManagerSettings(actorSystem).withSingletonName("singleton")), name)
  }

}

/**
  * First start an instance of a seed node
  */
object StartSingletonSeed extends Singleton {
  val actorSystem = ActorSystem(actorSystemName, Configuration.seedCfg(6969))
  joinCluster(actorSystem)
  startSingleton(actorSystem, singletonName)
}

/**
  * Then start an (or multiple) instance(s) of a member
  */
object StartSingletonMember extends Singleton {
  val actorSystem = ActorSystem(actorSystemName, Configuration.memberCfg)
  joinCluster(actorSystem)
  startSingleton(actorSystem, singletonName)
}

/**
  * Run this class to send a message to the active singleton
  */
object MessageSender extends Singleton {
  val actorSystem = ActorSystem(actorSystemName, Configuration.memberCfg)
  joinCluster(actorSystem)
  val proxy = actorSystem.actorOf(ClusterSingletonProxy.props("/user/Group-1", ClusterSingletonProxySettings(actorSystem).withSingletonName("singleton")),"SingletonProxy")
  proxy ! Request("Hello!")
}