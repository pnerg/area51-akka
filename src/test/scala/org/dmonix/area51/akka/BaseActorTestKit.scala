package org.dmonix.area51.akka

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Base class for test suites needing an actor system.
 * Sets up an actor system and makes sure to destroy it after the suite is finished
 * @author Peter Nerg
 * @constructor
 * @param name The name of the test suite, will become the name of the actor system
 */
abstract class BaseActorTestKit(name: String) extends TestKit(ActorSystem(name, LogConfiguration.config))
    with LogConfiguration
    with WordSpecLike
    with Matchers
    with ImplicitSender
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  override def afterAll():Unit = { TestKit.shutdownActorSystem(system) }
}