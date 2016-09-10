package org.dmonix.area51.akka.cluster.extensions

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Send, SendToAll}
import org.dmonix.area51.akka.cluster.Messages.{Broadcast, Message, Response, Unicast}
import org.dmonix.area51.akka.cluster.{ClusterSettings, SameThreadExecutionContext}

/**
  * Simple client class sending requests to be consumed by the ServiceProvider.
  * The class uses the Cluster Mediator to either unicast or broadcast message to services registered by the ServiceProvider
  * Both the Send and SendAll messages take the actor name as argument. The actor itself may then live locally or remotely, it's all hidden for the client/user.
  * @param mediator
  */
class ServiceConsumer(mediator:ActorRef) extends Actor with ActorLogging {

  def receive = {
    //sends the provided message to ONE instance of all the provided service names
    case Unicast(serviceNames, message) =>
      serviceNames.foreach(sName => mediator ! Send("/user/"+sName,message,false))
    //sends the provided message to ALL instances of all the provided service names
    case Broadcast(serviceNames, message) =>
      serviceNames.foreach(sName => mediator ! SendToAll("/user/"+sName,message,false))
    case Response(rsp) =>
      log.info(s"Service consumer [$self] got response [$rsp] from [$sender]")
    case a:Any ⇒
      log.warning(s"Service consumer [$self] got unexpected message [$a] from [$sender]")
  }

}

object ServiceConsumerStarter extends App with SameThreadExecutionContext with ClusterSettings {
  System.setProperty("config.file", "src/main/resources/akka-cfg/cluster-ext-member-tcp.conf");

  val actorSystem = ActorSystem(actorSystemName)
  val mediator = DistributedPubSub(actorSystem).mediator
  val cluster = Cluster(actorSystem)
  cluster.joinSeedNodes(seedNodes)

  val actor = actorSystem.actorOf(Props(new ServiceConsumer(mediator)))

  //appears that this sleep is needed for the communication to be up
  //sending something too early just causes the message to disappear into void without any trace nor log
  Thread.sleep(2000)

  actor ! Unicast(Seq("ServiceA", "ServiceB"), Message("Hello!!!"))
  actor ! Broadcast(Seq("ServiceA"), Message("Hello Again!!!"))

  //stupid pause to allow for responses to propagate and be logged
  Thread.sleep(2000)
  actorSystem.terminate().onComplete(_ => System.exit(1))
}
