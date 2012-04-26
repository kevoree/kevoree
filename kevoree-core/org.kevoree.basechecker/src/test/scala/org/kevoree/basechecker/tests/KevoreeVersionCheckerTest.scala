package org.kevoree.basechecker.tests

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.kevoree.core.basechecker.kevoreeVersionChecker.KevoreeVersionChecker
import collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/04/12
 * Time: 16:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeVersionCheckerTest extends AssertionsForJUnit with BaseCheckerSuite {

  @Test def checkKevoreeVersionOK () {
    val m = model("test_checker/kevoreeVersionChecker/validModel.kev")
    val kevoreeVersionChecker = new KevoreeVersionChecker
    val violations = kevoreeVersionChecker.check(m)
    assert(violations.size().equals(0))
  }

  @Test def checkKevoreeVersionKO () {
    val m = model("test_checker/kevoreeVersionChecker/invalidModel.kev")
    val kevoreeVersionChecker = new KevoreeVersionChecker
    val violations = kevoreeVersionChecker.check(m)
    /*violations.foreach {
      v => println(v.getMessage)
    }*/
    assert(violations.size().equals(2))
  }

  @Test def checkKevoreeVersionKO2 () {
    val m = model("test_checker/kevoreeVersionChecker/invalidModel2.kev")
    val kevoreeVersionChecker = new KevoreeVersionChecker
    val violations = kevoreeVersionChecker.check(m)
    /*violations.foreach {
      v => println(v.getMessage)
    }*/
    assert(violations.size().equals(2))
  }

  @Test def checkKevoreeVersionKO3 () {
      val m = model("test_checker/kevoreeVersionChecker/invalidModel3.kev")
      val kevoreeVersionChecker = new KevoreeVersionChecker
      val violations = kevoreeVersionChecker.check(m)
      /*violations.foreach {
        v => println(v.getMessage)
      }*/
      assert(violations.size().equals(1))
    }
}
