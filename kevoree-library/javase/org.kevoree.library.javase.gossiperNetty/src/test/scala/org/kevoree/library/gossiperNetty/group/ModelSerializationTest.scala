package org.kevoree.library.gossiperNetty.group

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.ContainerRoot
import java.io.ByteArrayOutputStream
import java.util.Date

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/05/11
 * Time: 17:50
 */

class ModelSerializationTest extends AssertionsForJUnit {

  @Test def concurrentSerialization () {

    val containerRoot = KevoreeXmiHelper.load ("src/test/resources/concurrentModel.kev")

    val t1 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t2 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t3 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t4 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t5 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t6 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t7 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t8 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t9 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t10 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t11 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t12 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t13 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t14 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t15 = new Thread () {
      override def run () {
        serialize (containerRoot)
      }
    }
    val t16 = new Thread () {
      override def run () {
        serialize (containerRoot)
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
    val out = new ByteArrayOutputStream
    KevoreeXmiHelper.saveStream (out, model)
    out.flush ()
    val bytes = out.toByteArray
    out.close ()
    val lastSerialization = new Date (System.currentTimeMillis)
  }
}