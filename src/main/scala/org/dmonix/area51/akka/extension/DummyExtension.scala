package org.dmonix.area51.akka.extension

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}

object DummyExtension extends ExtensionId[DummyExtension]
    with ExtensionIdProvider {

  //The lookup method is required by ExtensionIdProvider,
  // so we return ourselves here, this allows us
  // to configure our extension to be loaded when
  // the ActorSystem starts up
  override def lookup = DummyExtension

  //This method will be called by Akka
  // to instantiate our Extension
  override def createExtension(system: ExtendedActorSystem) = new DummyExtension

  /**
    * Java API: retrieve the Count extension for the given system.
    */
  override def get(system: ActorSystem): DummyExtension = super.get(system)
}

/**
  * @author Peter Nerg
  */
class DummyExtension extends Extension {
  //Since this Extension is a shared instance
  // per ActorSystem we need to be threadsafe
  private val counter = new AtomicLong(0)

  //This is the operation this Extension provides
  def increment() = counter.incrementAndGet()
}

/**
  * @author Peter Nerg
  */
class DummyActor extends Actor {

  private val dummyExtension = DummyExtension(context.system)

  override def receive:Receive = {
    case _ =>
      println(self.path+":"+dummyExtension.increment())
  }

}

/**
  * Dummy application to start the [[DummyActor]] and perform some testing of the Extension
  * @author Peter Nerg
  */
object DummyApp extends App {
  import org.dmonix.area51.akka.Configuration._

  private val actorSystem = ActorSystem("TestingExtensions", cfg)
  private val dummyActor1 = actorSystem.actorOf(Props(new DummyActor), "DummyActor-1")
  private val dummyActor2 = actorSystem.actorOf(Props(new DummyActor), "DummyActor-2")

  dummyActor1 ! "whatever"
  dummyActor2 ! "whatever"
  dummyActor1 ! "whatever"

  Thread.sleep(1000)
  actorSystem.terminate()

  private def cfg = s"""akka {
                       |extensions = ["org.dmonix.area51.akka.extension.DummyExtension"]                           |
                       |}""".asCfg




}
