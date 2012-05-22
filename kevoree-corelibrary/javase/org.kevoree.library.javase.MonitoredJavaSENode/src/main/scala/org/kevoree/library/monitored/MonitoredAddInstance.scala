package org.kevoree.library.monitored

import org.kevoree.Instance
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.library.defaultNodeTypes.jcl.deploy.command.AddInstance
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeDeployManager}
import org.slf4j.LoggerFactory
import org.kevoree.framework.osgi.KevoreeChannelFragmentActivator
import org.kevoree.framework.{ChannelTypeFragment}
import org.kevoree.framework.event.MonitorEventHandler
import org.kevoree.api.PrimitiveCommand

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/05/12
 * Time: 20:46
 */

case class MonitoredAddInstance(eventH:MonitorEventHandler,c: Instance, nodeName: String,modelservice : KevoreeModelHandlerService,kscript : KevScriptEngineFactory,bs : org.kevoree.api.Bootstraper) extends PrimitiveCommand {

  val embedCmd = AddInstance(c,nodeName,modelservice,kscript,bs)
  val logger = LoggerFactory.getLogger(this.getClass)

  def execute(): Boolean = {
    if(embedCmd.execute()){

      KevoreeDeployManager.bundleMapping.find(mp => mp.name == c.getName && mp.objClassName == c.getClass.getName) match {
        case Some(map)=> {
          if(map.ref.isInstanceOf[KevoreeChannelFragmentActivator]){
            map.ref.asInstanceOf[KevoreeChannelFragmentActivator].channelActor.asInstanceOf[ChannelTypeFragment].eventHandler = eventH
          }
        }
        case None => {
          logger.error("Can't instrument instance, mapping not found")
        }
      }

      true
    } else {
      false
    }
  }

  def undo() {
    embedCmd.undo()
  }

}
