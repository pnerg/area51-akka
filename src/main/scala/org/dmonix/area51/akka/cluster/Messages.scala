package org.dmonix.area51.akka.cluster

/**
  * Placeholder for all messages used.
  */
object Messages {

  /**
    * A unicast message intended to be sent to ONE instance of each provided service name
    * @constructor
    * @param serviceNames All service names to send the message to
    * @param message The message to send
    */
  case class Unicast(serviceNames:Seq[String], message:Message)
  /**
    * A broadcast message intended to be sent to ALL instances of each provided service name
    * @constructor
    * @param serviceNames All service names to send the message to
    * @param message The message to send
    */
  case class Broadcast(serviceNames:Seq[String], message:Message)

  /**
    * Container for a message to send to a service
    * @constructor
    * @param msg The message
    */
  case class Message(msg:String)

  /**
    * Response to a Message
    * @constructor
    * @param rsp The response
    */
  case class Response(rsp:String)


}
