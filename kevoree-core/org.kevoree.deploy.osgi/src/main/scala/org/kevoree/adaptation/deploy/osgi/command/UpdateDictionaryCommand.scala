/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.adaptation.deploy.osgi.command

import org.kevoree._
import framework.{PrimitiveCommand, Constants, KevoreeActor}
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.framework.message.UpdateDictionaryMessage
import org.slf4j.LoggerFactory

case class UpdateDictionaryCommand(c: Instance, ctx: KevoreeDeployManager, nodeName: String) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute(): Boolean = {
    //BUILD MAP
    val dictionary: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
    if (c.getTypeDefinition.getDictionaryType.isDefined) {
      if (c.getTypeDefinition.getDictionaryType.get.getDefaultValues != null) {
        c.getTypeDefinition.getDictionaryType.get.getDefaultValues.foreach {
          dv =>
            dictionary.put(dv.getAttribute.getName, dv.getValue)
        }
      }
    }

    if (c.getDictionary.isDefined) {
      c.getDictionary.get.getValues.foreach {
        v =>
          if (v.getAttribute.getFragmentDependant) {
            if (v.getTargetNode == nodeName) {
              dictionary.put(v.getAttribute.getName, v.getValue)
            }
          } else {
            dictionary.put(v.getAttribute.getName, v.getValue)
          }
      }
    }

    ctx.bundleMapping.find(map => map.objClassName == c.getClass.getName && map.name == c.getName) match {
      case None => false
      case Some(mapfound) => {
        val componentBundle = ctx.getBundleContext().getBundle(mapfound.bundleId)
        if (componentBundle.getRegisteredServices != null) {
          componentBundle.getRegisteredServices.find({
            sr => sr.getProperty(Constants.KEVOREE_NODE_NAME) == nodeName && sr.getProperty(Constants.KEVOREE_INSTANCE_NAME) == c.getName
          }) match {
            case None => {
              logger.error("Registered Service not found in bundleID="+componentBundle.getBundleId)
              false
            }
            case Some(sr) => (componentBundle.getBundleContext.getService(sr).asInstanceOf[KevoreeActor] !? UpdateDictionaryMessage(dictionary)).asInstanceOf[Boolean]
          }
        } else {
          logger.error("Registered Service for bundle ID="+componentBundle.getBundleId+" are null, not started instance")
          false
        }

      }
    }

  }

  def undo() {
    logger.warn("Not implemented")
  }

}
