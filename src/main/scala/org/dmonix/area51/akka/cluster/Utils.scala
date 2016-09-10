package org.dmonix.area51.akka.cluster

import java.util.concurrent.Executor

import akka.actor.Address

import scala.collection.immutable
import scala.concurrent.ExecutionContext

/**
  * @author Peter Nerg
  */
trait SameThreadExecutionContext   {
  /** Same thread executor, i.e. no thread pool.*/
  implicit val executionContext = ExecutionContext.fromExecutor(new Executor() {
    override def execute(command: Runnable): Unit = command.run()
  })
}

trait ClusterSettings {
  /** The name of the actor system all must use.*/
  val actorSystemName = "ClusterTest"

  /** The seed nodes all members must use*/
  val seedNodes = immutable.Seq(Address("akka.tcp", "ClusterTest", "127.0.0.1", 6969))
}
