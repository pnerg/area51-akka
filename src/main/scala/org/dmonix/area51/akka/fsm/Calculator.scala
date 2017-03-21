package org.dmonix.area51.akka.fsm

import akka.actor.{ActorRef, FSM, Props}
import org.dmonix.area51.akka.fsm.Calculator._


object Calculator {
  case class FirstValue(value:Int)
  case class Add(value:Int)
  case class Subtract(value:Int)
  case class Result(value:Int)
  case object Equals

  // states
  sealed trait State
  case object Reset extends State
  case object Calculating extends State

  sealed trait Data
  case object Uninitialized extends Data
  final case class Value(value:Int, ref:ActorRef) extends Data

  def props() = Props(new Calculator)


}

/**
  * Playing around with Akka FSM to create a simple/stupid calculator.
  *
  * The calculator has two states [[Reset]] and [[Calculating]].
  * It will change state from [[Reset]] -> [[Calculating]] when receiving [[FirstValue]].
  * In state [[Calculating]] it will receive [[Add]] and [[Subtract]] operations.
  * These operations alter the internal state/value of the calculator.
  * Once [[Equals]] is sent the calculator will return back the result to the actor that initialized it.
  * @author Peter Nerg
  */
class Calculator extends FSM[State, Data] {
  startWith(Reset, Uninitialized)

  /**
    * In reset state.
    * Change -> Calculating when we receive the FirstValue
    */
  when(Reset) {
    case Event(Equals, Uninitialized) => stay using Uninitialized
    case Event(FirstValue(v), Uninitialized) => goto(Calculating) using Value(v, sender)
  }

  /**
    * In calculating state.
    * Keep adding/subtracting the state for each arihtmetic operation received.
    * Go to Reset state when we receive Equals
    */
  when(Calculating) {
    case Event(Add(v), state@Value(t,_)) => stay using state.copy(value = t+v)
    case Event(Subtract(v), state@Value(t, _)) => stay using state.copy(value = t-v)
    case Event(Equals, Value(_, _)) => goto(Reset) using Uninitialized
  }

  /**
    * Manages the state transition from Calculating -> Reset.
    * During this transition we emit the result to the actor that initiated the calculator
    */
  onTransition {
    case Calculating -> Reset =>
      stateData match {
        case Value(v, ref) =>
          ref ! Result(v)
        case _                => // nothing to do
      }
  }

  /**
    * Just managing unhandled messages
    */
  whenUnhandled {
//    // common code for both states
//    case Event(Queue(obj), t @ Todo(_, v)) =>
//      goto(Active) using t.copy(queue = v :+ obj)

    case Event(e, s) =>
      println(s"Received unhandled request [$e] in state [$stateName]/[$s]")
      stay
  }
}
