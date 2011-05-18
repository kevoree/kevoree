package org.kevoree.library.gossiperNetty.group

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.ContainerRoot
import java.io.ByteArrayOutputStream
import java.util.Date
import org.eclipse.emf.ecore.util.EcoreUtil
import actors.{Actor, DaemonActor}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/05/11
 * Time: 17:50
 */

class ModelSerializationTest extends AssertionsForJUnit {

  class CMyHandler extends Actor {
    //start()

    println ("deamon start")
    val containerRoot = KevoreeXmiHelper.load (this.getClass.getClassLoader.getResource ("concurrentModel.kev").getFile)


    case class GETTOTO ()

    case class STOP ()

    def getmodel (): ContainerRoot = {
      (this !? GETTOTO ()).asInstanceOf[ContainerRoot]
    }

    /* PRIVATE PROCESS PART */
    def act () {

      loop {

        react {


          case GETTOTO () => {
            try {

              reply (EcoreUtil.copy (containerRoot))

              //reply(containerRoot)

            } catch {
              case _@e => e.printStackTrace ()
            }


          }
          case STOP () => exit ()
        }


      }
    }
  }

  @Test def concurrentSerialization () {

    val MyHandler = new CMyHandler
    MyHandler.start ()

    MyHandler.getmodel ()
    println ("toto")

    val threads = new Array[Thread](10000)

    var i = 0
    while (i < 10000) {
      val t = new Thread () {
        override def run () {
          serialize (MyHandler.getmodel ())
        }
      }
      threads(i) = t
      i += 1
    }

    i = 0
    while (i < 10000) {
      threads(i).start()
      i += 1
    }


    /*val t1 = new Thread () {
      override def run () {

        serialize (MyHandler.getmodel ())

      }
    }
    val t2 = new Thread () {
      override def run () {
        //println("t2" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t2" + System.currentTimeMillis())
      }
    }
    val t3 = new Thread () {
      override def run () {
        //println("t3" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t3" + System.currentTimeMillis())
      }
    }
    val t4 = new Thread () {
      override def run () {
        //println("t4" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t4" + System.currentTimeMillis())
      }
    }
    val t5 = new Thread () {
      override def run () {
        //println("t5" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t5" + System.currentTimeMillis())
      }
    }
    val t6 = new Thread () {
      override def run () {
        //println("t6" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t6" + System.currentTimeMillis())
      }
    }
    val t7 = new Thread () {
      override def run () {
        //println("t7" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t7" + System.currentTimeMillis())
      }
    }
    val t8 = new Thread () {
      override def run () {
        //println("t8" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t8" + System.currentTimeMillis())
      }
    }
    val t9 = new Thread () {
      override def run () {
        //println("t9" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        // println("t9" + System.currentTimeMillis())
      }
    }
    val t10 = new Thread () {
      override def run () {

        serialize (MyHandler.getmodel ())

      }
    }
    val t11 = new Thread () {
      override def run () {
        //println("t11" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t11" + System.currentTimeMillis())
      }
    }
    val t12 = new Thread () {
      override def run () {
        //println("t12" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t12" + System.currentTimeMillis())
      }
    }
    val t13 = new Thread () {
      override def run () {
        //println("t13" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t13" + System.currentTimeMillis())
      }
    }
    val t14 = new Thread () {
      override def run () {
        //println("t14" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t14" + System.currentTimeMillis())
      }
    }
    val t15 = new Thread () {
      override def run () {
        //println("t15" + System.currentTimeMillis())
        serialize (MyHandler.getmodel ())
        //println("t15" + System.currentTimeMillis())
      }
    }
    val t16 = new Thread () {
      override def run () {
        serialize (MyHandler.getmodel ())
      }
    }

    t1.start ()
    t2.start ()
    t3.start ()
    t4.start ()
    t5.start ()
    t6.start ()
    t7.start ()
    t8.start ()
    t9.start ()
    t10.start ()
    t11.start ()
    t12.start ()
    t13.start ()
    t14.start ()
    t15.start ()
    t16.start ()*/


    Thread.sleep (3000)


  }

  var i = 0

  private def serialize (model: ContainerRoot) {

    try {
      println ("start " + i)
      val out = new ByteArrayOutputStream
      KevoreeXmiHelper.saveStream (out, model)
      out.flush ()
      val bytes = out.toByteArray
      out.close ()
      val lastSerialization = new Date (System.currentTimeMillis)
      println ("Stop " + i)
      i = i + 1
    } catch {
      case _@e => e.printStackTrace ()
    }


  }

}