package org.kevoree.tools.annotation.bytecodeprocessor

import org.clapper.classutil.ClassFinder
import java.io.File
import org.kevoree.framework.AbstractComponentType

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/10/12
 * Time: 16:13
 */
object Tester extends App {

  println("Hello")

  val finder = ClassFinder(List(new File("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-corelibrary/javase/org.kevoree.library.javase.fakeDomo/target/classes")))
  val classes = finder.getClasses().filter(_.superClassName == classOf[AbstractComponentType].getName)
  classes.foreach {
    cl => {

      cl.modifiers.foreach{
        m => m.id
      }
      cl.methods.foreach{
        m=> m.name
      }

      /*
      cl.fields.foreach{
        f => println(f.name)
      } */

      println(cl.toString)
    }
  }

}
