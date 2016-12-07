package org.dmonix.area51.akka.cluster

import com.typesafe.config.{Config, ConfigFactory}

/**
  * @author Peter Nerg
  */
object Configuration {

  implicit class StringCfg(s: String) {
       def asCfg = ConfigFactory.parseString(s.stripMargin)
  }

  def remotingCfg(port:Int = 0) :Config = {
    s"""akka {
      |  actor {
      |    provider = "akka.remote.RemoteActorRefProvider"
      |  }
      |
      |  remote {
      |  	enabled-transports = ["akka.remote.netty.tcp"]
      |  	netty.tcp {
      |  		hostname = "127.0.0.1"
      |  		port = $port
      |  	}
      |  }
      |}""".asCfg
  }


  /**
    * Provides a actor system configuration for a seed node.
    * The difference to a member node is the fixed port number
    * @param port The port the actor system shall bind to
    * @return
    */
  def seedCfg(port:Int):Config = memberCfg(port, "seed")
  /**
    * Provides a actor system configuration for a seed node.
    * The difference to a seed node is the automated allocation of a port the actor system binds to
    * @return
    */
  def memberCfg:Config = memberCfg(0, "member")


  private def memberCfg(port:Int, role:String):Config =
    s"""akka {
      |  actor {
      |    provider = "akka.cluster.ClusterActorRefProvider"
      |  }
      |  remote {
      |    log-remote-lifecycle-events = off
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      port = $port
      |    }
      |  }
      |
      |  cluster {
      |    roles = ["$role"]
      |    auto-down-unreachable-after = 10s
      |  }
      |}""".asCfg


}
