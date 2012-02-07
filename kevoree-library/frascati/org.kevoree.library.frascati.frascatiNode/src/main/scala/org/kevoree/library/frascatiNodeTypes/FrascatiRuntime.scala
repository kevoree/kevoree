package org.kevoree.library.frascatiNodeTypes

import actors.Actor
import org.ow2.frascati.FraSCAti
import org.ow2.frascati.util.FrascatiClassLoader
import org.eclipse.emf.ecore.EPackage
import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Enumeration
import java.lang.Class

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
          Thread.currentThread().setContextClassLoader(classOf[FraSCAti].getClassLoader);
          internal_frascati = FraSCAti.newFraSCAti();
          val f_cl = new FrascatiClassLoader(classOf[FraSCAti].getClassLoader){
            override def loadClass(p1: String): Class[_] = {
              println("fload ======>"+p1)
              super[FrascatiClassLoader].loadClass(p1)
            }

            override def getResources(p1: String): Enumeration[URL] = {
              println("GetResss=="+p1)
              super[FrascatiClassLoader].getResources(p1)
            }

            override def getResource(p1: String): URL = {
              println("GetRes="+p1)
              super[FrascatiClassLoader].getResource(p1)
            }
          }
          internal_frascati.setClassLoader(f_cl)
          internal_frascati.getClassLoaderManager().setClassLoader(f_cl)

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
              logger.error("", e); reply(false)
            }
          }
        }
        case UndoContextCommand(cmd: PrimitiveCommand) => {
          try {
            cmd.undo()
            reply(true)
          } catch {
            case _@e => {
              logger.error("", e); reply(false)
            }
          }
        }
      }
    }
  }
}
