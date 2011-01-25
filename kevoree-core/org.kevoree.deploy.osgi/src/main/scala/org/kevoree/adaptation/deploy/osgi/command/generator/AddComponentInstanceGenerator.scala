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


object AddComponentInstanceGenerator {
/*
	 var internalLog : Logger = LoggerFactory.getLogger("org.kevoree.deploy.osgi.AddComponentInstanceGenerator")
	 
  def generate(c : ComponentInstance) : String = {
    var content =
      //  <?xml version="1.0" encoding="UTF-8"?>
    <blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.osgi.org/xmlns/blueprint/v1.0.0 http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd">
      <reference id="cf" interface="javax.jms.ConnectionFactory" />
      {
        var provided : scala.collection.mutable.ArrayBuffer[scala.xml.NodeBuffer] = new scala.collection.mutable.ArrayBuffer[scala.xml.NodeBuffer]
        var required : scala.collection.mutable.ArrayBuffer[scala.xml.NodeBuffer] = new scala.collection.mutable.ArrayBuffer[scala.xml.NodeBuffer]
        c.getProvided.foreach{ pp =>
          if(pp.isBind){
            provided.add(
              <bean id={pp.getPortTypeRef.getName} class={c.getComponentType.getFactoryBean} factory-method={"create"+c.getComponentType.getName+"PORT"+pp.getPortTypeRef.getName  } />
              <service ref={pp.getPortTypeRef.getName} interface={pp.getPortTypeRef.getRef.getName} depends-on={c.getName}>
                <service-properties>
                  <entry key="artPortName" value={pp.getPortTypeRef.getName} />
                  <entry key="artComponentName" value={c.getName} />
                </service-properties>
              </service>
              <bean id={pp.getPortTypeRef.getName+"proxy"} class={c.getComponentType.getFactoryBean} factory-method={"create"+c.getComponentType.getName+"PORTPROXY"+pp.getPortTypeRef.getName}>
                <property name="delegate"><ref component-id={pp.getPortTypeRef.getName} /></property>
              </bean>

              <bean id={pp.getPortTypeRef.getName+"proxyConsumer"} class="org.kevoree.runtime.proxy.Factory" factory-method="createConsumer" init-method="init" destroy-method="stop">
                <property name="uri" value={pp.getProxyURI} />
                <property name="cf" ref="cf" />
                <property name="hubType" value={pp.getProxyHubType} />
                <property name="delegate"><ref component-id={pp.getPortTypeRef.getName+"proxy"} /></property>
              </bean>
            )
          }
        }
        c.getRequired.foreach{ rp =>
          if(rp.isBind){
            rp.getPortTypeRef.getRef match {
              
              case spt : ServicePortType => {
                  c.eContainer.eContainer.asInstanceOf[ContainerRoot].getBindings.find({b=> b.isUsingPort(rp) }) match {
                    case Some(b) => {
                        var remotePort = b.opositePort(rp).get
                        var remoteNode = remotePort.eContainer.eContainer.asInstanceOf[ContainerNode]
                        required.add(
                          <!-- -->
                          <reference id={rp.getPortTypeRef.getName} interface={rp.getPortTypeRef.getRef.getName} 
                            filter={"&(artComponentName="+remotePort.eContainer.asInstanceOf[ComponentInstance].getName+")(artPortName="+remotePort.getPortTypeRef.getName+")"} 
                          />
                        )

                      }
                    case None =>
                  }
                }
              case mpt : MessagePortType => {
                  required.add(
                    <!-- -->
                    <reference id={rp.getPortTypeRef.getName} interface="org.kevoree.framework.MessagePort" filter={"&(artComponentName="+c.getName+")(artPortName="+rp.getPortTypeRef.getName+")"} />
                  )
                }
              case _ @ uncatch => internalLog.error(uncatch.toString)
            }
          }
        }
        provided++required
      }
      <!-- create the bean -->
      <bean id={c.getName} class={c.getComponentType.getFactoryBean} factory-method={"create"+c.getComponentType.getName}  init-method={c.getComponentType.getStartMethod} destroy-method={c.getComponentType.getStopMethod}>

        <property name="dictionary">
          <map>
            <entry key="art.name"><ref component-id={c.getName} /></entry>
            <entry key="osgi.bundle"><ref component-id="blueprintBundle" /></entry>
          </map>
        </property>

        <property name="hostedPorts">
          <map>
            {
              var entries = new scala.collection.mutable.ArrayBuffer[scala.xml.Elem]
              c.getProvided.foreach{hp =>
                if(hp.isBind){
                  entries.append(
                    <entry key={hp.getPortTypeRef.getName}>
                      <ref component-id={hp.getPortTypeRef.getName} />
                    </entry>
                  )
                }
              }
              entries
            }
          </map>
        </property>
        <property name="neededPorts">
          <map>
            {
              var entries = new scala.collection.mutable.ArrayBuffer[scala.xml.Elem]
              c.getRequired.foreach{np =>
                if(np.isBind){
                  entries.append(
                    <entry key={np.getPortTypeRef.getName}>
                      <ref component-id={np.getPortTypeRef.getName} />
                    </entry>
                  )
                } 
              }
              entries
            }
          </map>
        </property>
      </bean>
    </blueprint>;
    content.toString
  }
*/
}
