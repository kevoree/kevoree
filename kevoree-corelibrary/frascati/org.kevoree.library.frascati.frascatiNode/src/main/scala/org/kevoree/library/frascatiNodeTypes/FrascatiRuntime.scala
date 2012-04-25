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


          //val fcl = new FrascatiClassLoader(classOf[FrascatiNode].getClassLoader) {
            val fcl = new FrascatiClassLoaderWrapper(classOf[FrascatiNode].getClassLoader.asInstanceOf[KevoreeJarClassLoader])
          Thread.currentThread().setContextClassLoader(fcl);

          internal_frascati = FraSCAti.newFraSCAti(fcl);
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
