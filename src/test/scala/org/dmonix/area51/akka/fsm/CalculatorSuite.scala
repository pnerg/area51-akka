package org.dmonix.area51.akka.fsm

import org.dmonix.area51.akka.BaseActorTestKit
import org.dmonix.area51.akka.fsm.Calculator._

import scala.util.Random

/**
  * @author Peter Nerg
  */
class CalculatorSuite extends BaseActorTestKit("CalculatorSuite"){

  private def actorName = "Calculator-"+Random.nextInt(100)

  "The calculator" must {
    "Add two values correctly" in {
      val calculator = system.actorOf(Calculator.props(), actorName)

      calculator ! FirstValue(1)
      calculator ! Add(1)
      calculator ! Equals

      expectMsg(Result(2))
    }

    "Subtract two values correctly" in {
      val calculator = system.actorOf(Calculator.props(), actorName)

      calculator ! FirstValue(2)
      calculator ! Subtract(1)
      calculator ! Equals

      expectMsg(Result(1))
    }

    "Perform multiple arithmetic operations correctly" in {
      val calculator = system.actorOf(Calculator.props(), actorName)

      calculator ! FirstValue(10)
      calculator ! Add(10)
      calculator ! Subtract(5)
      calculator ! Add(5)
      calculator ! Equals

      expectMsg(Result(20))
    }
  }

}
