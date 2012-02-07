package org.kevoree.library.frascatiNodeTypes.primitives

import xml.Node
import scala.collection.mutable.Queue


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 23:33
 */

object ScaGenerator {

  def printService(serviceName: String, interfaceName: String): Node = {
    <service name={serviceName}>
        <interface.java interface={interfaceName}/>
    </service>
  }

  def printServiceComposite(componentName: String, serviceName: String): Node = {
      <service name={serviceName} promote={componentName + "/" + serviceName}/>
  }

  def printReferenceComposite(componentName: String, serviceName: String): Node = {
      <reference name={serviceName} promote={componentName + "/" + serviceName}/>
  }


  def printReference(serviceName: String, interfaceName: String): Node = {
    <reference name={serviceName}>
        <interface.java interface={interfaceName}/>
    </reference>
  }

  def properties(propertyName: String, value: String): Node = {
    <property name={propertyName}>
      {value}
    </property>
  }

  def generateComponent(componentName: String, componentJavaClass: String, instance: org.kevoree.ComponentInstance): String = {
    "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>" +
      <composite xmlns={"http://www.osoa.org/xmlns/sca/1.0"} name={componentName}>
        {var res = Queue[Node]();
      instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getProvided.foreach(e => res ++= printServiceComposite(componentName + "_internal_", e.getName))
      instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getRequired.foreach(e => res ++= printReferenceComposite(componentName + "_internal_", e.getName))
      res}<component name={componentName + "_internal_"}>
          <implementation.java class={componentJavaClass}/>{var res = Queue[Node]();
        instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getProvided.foreach(e => res ++= printService(e.getName, e.getRef.getName));
        instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getRequired.foreach(e => res ++= printReference(e.getName, e.getRef.getName))
        if (!instance.getDictionary.isEmpty) {
          instance.getDictionary.get.getValues.foreach(value => res ++= properties(value.getAttribute.getName, value.getValue))
        }
        res}
      </component>
      </composite>.toString()
  }

}
