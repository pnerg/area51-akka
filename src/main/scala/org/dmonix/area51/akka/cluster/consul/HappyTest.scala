package org.dmonix.area51.akka.cluster.consul
import java.net.InetAddress
import java.util.UUID
import java.util.concurrent.Executors

import consul.Consul
import consul.v1.agent.service.Check
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
/**
  * @author Peter Nerg (epknerg).
  */
object HappyTest extends App {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val consul = Consul.standalone(InetAddress.getByName("localhost"), 8500, None)
//  val consul = new Consul(InetAddress.getLocalHost, 8500, None)
  import consul.v1._

  val check = Option(Check(None, None, None, Some("")))
  val reg = agent.service.LocalService(ServiceId("ID"),ServiceType("SomeService"),Set(ServiceTag(UUID.randomUUID().toString)),None,check)
  agent.service.register(reg)

  val x = Await.result(catalog.services(None), 5.seconds)
  println(x)
}
