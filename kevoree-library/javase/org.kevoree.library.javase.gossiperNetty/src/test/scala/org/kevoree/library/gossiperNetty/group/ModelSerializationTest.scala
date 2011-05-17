package org.kevoree.library.gossiperNetty.group

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.ContainerRoot
import java.io.ByteArrayOutputStream
import java.util.Date
import org.eclipse.emf.ecore.util.EcoreUtil
import actors.DaemonActor

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/05/11
 * Time: 17:50
 */

class ModelSerializationTest extends AssertionsForJUnit {

  object MyHandler extends DaemonActor {

    val containerRoot = KevoreeXmiHelper.load ("src/test/resources/concurrentModel.kev")
    start ()

    case class GETTOTO ()

    def get (): ContainerRoot = {
      (this !? GETTOTO ()).asInstanceOf[ContainerRoot]
    }

    /* PRIVATE PROCESS PART */
    def act () {
      loop {
        react {
          case GETTOTO () => {
            println("ça va bloquer")
            reply (EcoreUtil.copy (containerRoot))
            println("en fait nan")

          }
        }
      }
    }
  }

  @Test def concurrentSerialization () {


    println ("toto")

    val t1 = new Thread () {
      override def run () {
        println ("t1" + System.currentTimeMillis ())
        serialize (MyHandler.get ())
        println ("t1" + System.currentTimeMillis ())
      }
    }
    val t2 = new Thread () {
      override def run () {
        //println("t2" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t2" + System.currentTimeMillis())
      }
    }
    val t3 = new Thread () {
      override def run () {
        //println("t3" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t3" + System.currentTimeMillis())
      }
    }
    val t4 = new Thread () {
      override def run () {
        //println("t4" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t4" + System.currentTimeMillis())
      }
    }
    val t5 = new Thread () {
      override def run () {
        //println("t5" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t5" + System.currentTimeMillis())
      }
    }
    val t6 = new Thread () {
      override def run () {
        //println("t6" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t6" + System.currentTimeMillis())
      }
    }
    val t7 = new Thread () {
      override def run () {
        //println("t7" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t7" + System.currentTimeMillis())
      }
    }
    val t8 = new Thread () {
      override def run () {
        //println("t8" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t8" + System.currentTimeMillis())
      }
    }
    val t9 = new Thread () {
      override def run () {
        println ("t9" + System.currentTimeMillis ())
        serialize (MyHandler.get ())
        println ("t9" + System.currentTimeMillis ())
      }
    }
    val t10 = new Thread () {
      override def run () {
        println ("t10" + System.currentTimeMillis ())
        serialize (MyHandler.get ())
        println ("t10" + System.currentTimeMillis ())
      }
    }
    val t11 = new Thread () {
      override def run () {
        //println("t11" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t11" + System.currentTimeMillis())
      }
    }
    val t12 = new Thread () {
      override def run () {
        //println("t12" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t12" + System.currentTimeMillis())
      }
    }
    val t13 = new Thread () {
      override def run () {
        //println("t13" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t13" + System.currentTimeMillis())
      }
    }
    val t14 = new Thread () {
      override def run () {
        //println("t14" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t14" + System.currentTimeMillis())
      }
    }
    val t15 = new Thread () {
      override def run () {
        //println("t15" + System.currentTimeMillis())
        serialize (MyHandler.get ())
        //println("t15" + System.currentTimeMillis())
      }
    }
    val t16 = new Thread () {
      override def run () {
        println ("t16 " + System.currentTimeMillis ())
        serialize (MyHandler.get ())
        println ("t16 " + System.currentTimeMillis ())
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
    t16.start ()


  }

  private def serialize (model: ContainerRoot) {
    println ("start serilaierr ...")
    val out = new ByteArrayOutputStream
    KevoreeXmiHelper.saveStream (out, model)
    out.flush ()
    val bytes = out.toByteArray
    out.close ()
    val lastSerialization = new Date (System.currentTimeMillis)
    println ("end serilaierr ...")
  }

}