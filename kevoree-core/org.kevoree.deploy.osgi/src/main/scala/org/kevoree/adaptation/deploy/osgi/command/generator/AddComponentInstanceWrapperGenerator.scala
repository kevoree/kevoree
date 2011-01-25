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

package org.kevoree.adaptation.deploy.osgi.command.generator

import org.kevoree.ComponentInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.xml._
import org.kevoree._
import org.kevoree.framework.aspects.KevoreeAspects._

object AddComponentInstanceWrapperGenerator {
/*
	
	 var internalLog : Logger = LoggerFactory.getLogger("org.kevoree.deploy.osgi.AddComponentInstanceWrapperGenerator")
	 
  def generate(c : ComponentInstance) : String = {
    var content =
      //  <?xml version="1.0" encoding="UTF-8"?>
    <blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.osgi.org/xmlns/blueprint/v1.0.0 http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd">
      <reference id="cf" interface="javax.jms.ConnectionFactory" />
      {
        var required : scala.collection.mutable.ArrayBuffer[scala.xml.NodeBuffer] = new scala.collection.mutable.ArrayBuffer[scala.xml.NodeBuffer]
        c.getRequired.foreach{ rp =>
          if(rp.isBind){
            rp.getPortTypeRef.getRef match {

              case spt : ServicePortType => {
                  c.eContainer.eContainer.asInstanceOf[ContainerRoot].getBindings.find({b=> b.isUsingPort(rp) }) match {
                    case Some(b) => {
                        var remotePort = b.opositePort(rp).get
                        var remoteNode = remotePort.eContainer.eContainer.asInstanceOf[ContainerNode]

                        if(remoteNode.isModelEquals(c.eContainer.asInstanceOf[ContainerNode])){
                          //collocated binding, no proxy injection
                        } else {
                          required.add(
                            <bean id={rp.getPortTypeRef.getName+"proxyProducer"} class="org.kevoree.runtime.proxy.Factory" factory-method="createProducer" init-method="init" destroy-method="stop">
                              <property name="uri" value={remotePort.getProxyURI} />
                              <property name="hubType" value={rp.getProxyHubType} />
                              <property name="cf"><ref component-id="cf" /></property>
                            </bean>

                            <bean id={rp.getPortTypeRef.getName+"proxy"} class={c.getComponentType.getFactoryBean} factory-method={"create"+c.getComponentType.getName+"PORTPROXY"+rp.getPortTypeRef.getName}>
                              <property name="delegate"><ref component-id={rp.getPortTypeRef.getName+"proxyProducer"} /></property>
                            </bean>

                            <service ref={rp.getPortTypeRef.getName+"proxy"} interface={rp.getPortTypeRef.getRef.getName}>
                              <service-properties>
                                <entry key="artPortName" value={rp.getPortTypeRef.getName} />
                                <entry key="artComponentName" value={remotePort.eContainer.asInstanceOf[ComponentInstance].getName} />
                              </service-properties>
                            </service>
                          )
                        }
                      }
                    case None =>
                  }
                }
              case mpt : MessagePortType => {
                  var portBinding= c.eContainer.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach{mb=> mb.getPort.isModelEquals(rp)  }
                  required.add(

                    <bean id={rp.getPortTypeRef.getName+"proxyProducer"} class="org.kevoree.runtime.proxy.Factory" factory-method="createProducer" init-method="init" destroy-method="stop">
                      <property name="uri" value={rp.getProxyURI} />
                      <property name="hubType" value={rp.getProxyHubType} />
                      <property name="cf"><ref component-id="cf" /></property>
                    </bean>

                    <bean id={rp.getPortTypeRef.getName+"proxy"} class={c.getComponentType.getFactoryBean} factory-method={"create"+c.getComponentType.getName+"PORTPROXY"+rp.getPortTypeRef.getName}>
                      <property name="delegate"><ref component-id={rp.getPortTypeRef.getName+"proxyProducer"} /></property>
                    </bean>

                    <service ref={rp.getPortTypeRef.getName+"proxy"} interface="org.kevoree.framework.MessagePort">
                      <service-properties>
                        <entry key="artPortName" value={rp.getPortTypeRef.getName} />
                        <entry key="artComponentName" value={c.getName} />
                      </service-properties>
                    </service>
                  )
                }
              case _ @ uncatch => internalLog.error(uncatch.toString())
            }
          }
        }
        required
      }

      
    </blueprint>;
    content.toString
  }
*/
}
