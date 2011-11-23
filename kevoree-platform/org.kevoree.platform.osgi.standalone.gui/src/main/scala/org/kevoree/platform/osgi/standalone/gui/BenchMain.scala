package org.kevoree.platform.osgi.standalone.gui

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/11/11
 * Time: 20:22
 * To change this template use File | Settings | File Templates.
 */

/**
 * Simple actor performance test, based upon
 * http://github.com/jboner/akka/blob/master/akka-core/src/test/scala/PerformanceTest.scala.
 * Modified to change timeout behavior for longer running tests, add a warm-up period,
 * and run a single set of tests on different VMs (rather than comparing different
 * actor implementations).
 */
object ActorPerfTest {

  import scala.actors._
  import scala.actors.Actor._

  val main = Actor.self

  def stressTestScalaActors(nrOfMessages: Int, nrOfActors: Int): Long = {
    abstract class Colour
    case object RED extends Colour
    case object YELLOW extends Colour
    case object BLUE extends Colour
    case object FADED extends Colour

    val colours: Array[Colour] = Array(BLUE, RED, YELLOW)

    case class Meet(colour: Colour)
    case class Change(colour: Colour)
    case class MeetingCount(count: Int)

    case class Time(time: Long)

    class Mall(var n: Int, numChameneos: Int) extends Actor {
      var waitingChameneo: Option[OutputChannel[Any]] = None
      var startTime: Long = 0L

      start()

      def startChameneos(): Unit = {
        startTime = System.currentTimeMillis
        var i = 0
        while (i < numChameneos) {
          Chameneo(this, colours(i % 3), i).start()
          i = i + 1
        }
      }

      def act() {
        var sumMeetings = 0
        var numFaded = 0
        loop {
          react {

            case MeetingCount(i) => {
              numFaded = numFaded + 1
              sumMeetings = sumMeetings + i
              if (numFaded == numChameneos) {
                main ! Time(System.currentTimeMillis - startTime)
                exit()
              }
            }

            case msg@Meet(c) => {
              if (n > 0) {
                waitingChameneo match {
                  case Some(chameneo) =>
                    n = n - 1
                    chameneo.forward(msg)
                    waitingChameneo = None
                  case None =>
                    waitingChameneo = Some(sender)
                }
              } else {
                waitingChameneo match {
                  case Some(chameneo) =>
                    chameneo ! Exit(this, "normal")
                  case None =>
                }
                sender ! Exit(this, "normal")
              }
            }

          }
        }
      }
    }

    case class Chameneo(var mall: Mall, var colour: Colour, id: Int) extends Actor {
      var meetings = 0

      def act() {
        loop {
          mall ! Meet(colour)
          react {
            case Meet(otherColour) =>
              colour = complement(otherColour)
              meetings = meetings + 1
              sender ! Change(colour)
            case Change(newColour) =>
              colour = newColour
              meetings = meetings + 1
            case Exit(_, _) =>
              colour = FADED
              sender ! MeetingCount(meetings)
              exit()
          }
        }
      }

      def complement(otherColour: Colour): Colour = {
        colour match {
          case RED => otherColour match {
            case RED => RED
            case YELLOW => BLUE
            case BLUE => YELLOW
            case FADED => FADED
          }
          case YELLOW => otherColour match {
            case RED => BLUE
            case YELLOW => YELLOW
            case BLUE => RED
            case FADED => FADED
          }
          case BLUE => otherColour match {
            case RED => YELLOW
            case YELLOW => RED
            case BLUE => BLUE
            case FADED => FADED
          }
          case FADED => FADED
        }
      }

      override def toString() = id + "(" + colour + ")"
    }

    val mall = new Mall(nrOfMessages, nrOfActors)
    mall.startChameneos
    main receive { case Time(totalTime) => totalTime }
  }

  def runTest(nrOfMessages: Int, nrOfActors: Int): Long = {
    stressTestScalaActors(nrOfMessages, nrOfActors)
  }

  def main(args: Array[String]) {



    //System.setProperty("actors.enableForkJoin", "false")

   //  scala.actors.Scheduler.impl = new scala.actors.scheduler.ResizableThreadPoolScheduler(false, false)

    println("===========================================")
    println("== Benchmark Scala Actors ==")
    println("== JVM: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")")
    println("== Scheduler: " + scala.actors.Scheduler.impl.getClass.getName)

    val nrOfMessages = 5000 // varied by test
    val nrOfActors = 10
    val runs = 20

    var i = 0
    println("Warming up....")
    while(i < runs * 3) {
      println("  Iteration time: " + runTest(nrOfMessages, nrOfActors) + " ms")
      i = i + 1
    }

    i = 0
    println("Running tests....")
    var totTime = 0L
    while(i < runs) {
      val time = runTest(nrOfMessages, nrOfActors)
      println("  Iteration time: " + time + " ms")
      totTime = totTime + time
      i = i + 1
    }
    val scalaTime = totTime.toDouble / runs

    println("\tNr of messages:\t" + nrOfMessages)
    println("\tNr of actors:\t" + nrOfActors)
    println("\tNr of runs:\t" + runs)
    println("\tScala Actors:\t" + scalaTime + " ms (avg)")
    println("===========================================")
  }
}

