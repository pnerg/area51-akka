package org.dmonix.akka.cluster

import java.util.concurrent.Executor

import scala.concurrent.ExecutionContext

/**
  * @author Peter Nerg
  */
trait SameThreadExecutionContext   {
  implicit val executionContext = ExecutionContext.fromExecutor(new Executor() {
    override def execute(command: Runnable): Unit = command.run()
  })
}
