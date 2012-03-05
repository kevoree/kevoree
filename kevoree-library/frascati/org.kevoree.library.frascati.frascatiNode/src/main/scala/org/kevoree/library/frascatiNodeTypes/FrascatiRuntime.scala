package org.kevoree.library.frascatiNodeTypes;

import scala.actors.Actor  
import org.ow2.frascati.FraSCAti
import org.ow2.frascati.util.FrascatiClassLoader
import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Enumeration
import java.lang.Class
import org.kevoree.kcl.KevoreeJarClassLoader

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/02/12
 * Time: 01:13
 */
case class ExecuteContextCommand(cmd: PrimitiveCommand)

case class UndoContextCommand(cmd: PrimitiveCommand)
 
class FrascatiRuntime extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private var internal_frascati: FraSCAti = null

  def startRuntime: FraSCAti = {
    (this !? StartRuntime()).asInstanceOf[FraSCAti]
  }

  def stopRuntime: Boolean = {
    (this !? StopRuntime()).asInstanceOf[Boolean]
  }

  case class StartRuntime()

  case class StopRuntime()

  def act() {
    while (true) {
      receive {
        case StartRuntime() => {
                            /*
          val f_cl = new FrascatiClassLoader(classOf[FrascatiNode].getClassLoader) {

            override def loadClass(p1: String): Class[_] = {
              val cl = classOf[FrascatiNode].getClassLoader.loadClass(p1)
              logger.info("fload " + p1 + " ======> res " + cl)
              cl
            }


            override def getResourceAsStream(p1: String) = {
              logger.info("GetResssStream==" + p1)
              classOf[FrascatiNode].getResourceAsStream(p1)
            }

            override def getResources(p1: String): Enumeration[URL] = {

              //sval res2 = classOf[FraSCAti].getClassLoader.getResources(p1)
              // while(res2.hasNext)


              logger.info("GetResss==" + p1)
              println(Thread.currentThread().getContextClassLoader)
              println(classOf[FrascatiNode].getClassLoader)

              val res = classOf[FrascatiNode].getClassLoader.getResources(p1)

              import scala.collection.JavaConversions._

              println(res.size)
              println(res.asInstanceOf[sun.misc.CompoundEnumeration[_>:Any]].size)
              res.asInstanceOf[sun.misc.CompoundEnumeration[_>:Any]].foreach(p2=>  println((p2)))
              println("getresResult=" + res + "- ")
              res
            }

            override def getResource(p1: String): URL = {
              logger.info("GetRes=" + p1)
              classOf[FrascatiNode].getClassLoader.getResource(p1)
            }

            override def getURLs(): Array[java.net.URL] = {
              logger.info("GETURL CLASSLOADER")
              val urls = classOf[FrascatiNode].getClassLoader.asInstanceOf[KevoreeJarClassLoader].getLoadedURLs
              urls.toArray(new Array[java.net.URL](urls.size))
            }

            override def addURL(p1: URL) {
              println("add URL " + p1)
            }
          }   */

          Thread.currentThread().setContextClassLoader(classOf[FrascatiNode].getClassLoader);
         // FraSCAti.newFraSCAti()


          println("Lookfor=MembraneGeneration")
          val clazzres = classOf[FrascatiNode].getClassLoader.loadClass("org.ow2.frascati.component.factory.api.MembraneGeneration")
          println("Result="+clazzres)

          internal_frascati = FraSCAti.newFraSCAti(classOf[FrascatiNode].getClassLoader);
          //internal_frascati.setClassLoader(f_cl)
          //internal_frascati.getClassLoaderManager.setClassLoader(f_cl)

          reply(internal_frascati)
        }
        case StopRuntime() => {
          internal_frascati.close(internal_frascati.getComposite("org.ow2.frascati.FraSCAti"));
          internal_frascati = null;
          reply(true)
          exit()
        }
        case ExecuteContextCommand(cmd: PrimitiveCommand) => {
          try {
            reply(cmd.execute())
          } catch {
            case _@e => {
              logger.error("", e);
              reply(false)
            }
          }
        }
        case UndoContextCommand(cmd: PrimitiveCommand) => {
          try {
            cmd.undo()
            reply(true)
          } catch {
            case _@e => {
              logger.error("", e);
              reply(false)
            }
          }
        }
      }
    }
  }
}
