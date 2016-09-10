package org.dmonix.area51.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import org.dmonix.area51.akka.cluster.Messages.{Broadcast, Message, Response, Unicast}
import org.dmonix.area51.akka.cluster.{ClusterSettings, SameThreadExecutionContext}

/**
  * Works as a publisher in the publish/subscribe pattern provided by the cluster extensions.
  * The publish operation seems to work differently if the subscriber has subscribed with a group or not.
  * @author Peter Nerg
  */
class Publisher(mediator:ActorRef) extends Actor with ActorLogging{

  def receive = {
    //sends the provided message to ONE instance of all the provided service names
    case Unicast(serviceNames, message) =>
      serviceNames.foreach(sName => mediator ! Publish(sName,message,true))
    //sends the provided message to ALL instances of all the provided service names
    case Broadcast(serviceNames, message) =>
      serviceNames.foreach(sName => mediator ! Publish(sName,message,false))
    case Response(rsp) =>
      log.info(s"Publisher [$self] got response [$rsp] from [$sender]")
    case a:Any ⇒
      log.warning(s"Publisher [$self] got unexpected message [$a] from [$sender]")
  }

}

object PublisherStarter extends App with SameThreadExecutionContext with ClusterSettings {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-ext-member-tcp.conf");
  val actorSystem = ActorSystem(actorSystemName)
  val mediator = DistributedPubSub(actorSystem).mediator
  val cluster = Cluster(actorSystem)
  cluster.joinSeedNodes(seedNodes)

  val actor = actorSystem.actorOf(Props(new Publisher(mediator)), "Publisher")

  //appears that this sleep is needed for the communication to be up
  //sending something too early just causes the message to disappear into void without any trace nor log
  Thread.sleep(2000)

  actor ! Unicast(Seq("ServiceA", "ServiceB"), Message("Unicast Hello-1!!!"))
  actor ! Unicast(Seq("ServiceA", "ServiceB"), Message("Unicast Hello-2!!!"))
  actor ! Broadcast(Seq("ServiceA"), Message("Broadcast Hello Again!!!"))

  //stupid pause to allow for responses to propagate and be logged
  Thread.sleep(2000)
  actorSystem.terminate().onComplete(_ => System.exit(1))
}
