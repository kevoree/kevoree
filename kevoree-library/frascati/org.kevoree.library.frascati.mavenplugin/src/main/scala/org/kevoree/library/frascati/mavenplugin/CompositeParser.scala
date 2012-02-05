package org.kevoree.library.frascati.mavenplugin

import java.io.File
import xml.{ Node, XML }
import org.kevoree.{ KevoreeFactory, ContainerRoot }
import org.kevoree.PortTypeRef
import org.kevoree.ComponentType
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.ServicePortType

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */

object CompositeParser {

  def parseCompositeFile(file: File, version: String, groupId: String, artefactId: String,compositeFileName:String): ContainerRoot = {

    val root = KevoreeFactory.createContainerRoot

    val xmlnode = XML.loadFile(file)
    xmlnode.child.foreach { cNode =>
      cNode.label match {
        case "component" => {
          val newkev = KevoreeFactory.createComponentType
          cNode.attribute("name").map { nameAtt =>
            newkev.setName(nameAtt.text)
          }
          cNode.attribute("implementation.java").map { nameAtt =>
            newkev.setBean(nameAtt.text)
          }
          newkev.setStartMethod("start");
          newkev.setStopMethod("stop");
          newkev.setUpdateMethod("update");

          val dico = KevoreeFactory.createDictionaryType

          cNode.child.foreach(node => node.label match {
            case "service" => {
              newkev.addProvided(this.createPortRef(root, node))
            }
            case "reference" => {
              newkev.addRequired(this.createPortRef(root, node))
            }
            case "property" => {
              val attr = KevoreeFactory.createDictionaryAttribute
              attr.setOptional(false)
              attr.setFragmentDependant(false)
              attr.setState(false)
              node.attribute("name").map { nameAtt =>
                attr.setName(nameAtt.text)
              }
              //TODO
              attr.setDatatype("java.lang.String")
              dico.addAttributes(attr)

            }
            case _@ e =>

          })
          if (dico.getAttributes.size > 0)
            newkev.setDictionaryType(Option(dico))
          root.addTypeDefinitions(newkev)

        }
        case _@ e =>
      }

    }

    xmlnode.label match {
        case "composite" => {
          val newkev = KevoreeFactory.createComponentType
          newkev.setBean(compositeFileName)
          xmlnode.attribute("name").map { nameAtt =>
            newkev.setName(nameAtt.text)
          }
          xmlnode.child.foreach(node => node.label match {
            case "service" => {
              newkev.addProvided(this.createPortRefComposite(root, node))
            }
            case "reference" => {
              newkev.addRequired(this.createPortRefComposite( root, node))
            }
            case _@ e =>

          })
          
         root.addTypeDefinitions(newkev)
          

        }
        case _@ e =>

    }
    this.createRepo(root)
    this.addCurrentDeployUnit(root, version, groupId, artefactId)

    root

  }

  def createPortRefComposite(root: ContainerRoot, node: Node): PortTypeRef = {
    val portRef = KevoreeFactory.createPortTypeRef
    var promoteComponent: String = ""
    var promotePort: String = ""

    portRef.setOptional(true)
    portRef.setNoDependency(false)
    node.attribute("name").map { nameAtt =>
      portRef.setName(nameAtt.text)
    }
    node.attribute("promote").map { nameAtt =>
      {
        promoteComponent = nameAtt.text.split('/').apply(0)
        promotePort = nameAtt.text.split('/').apply(1)

        root.getTypeDefinitions.filter(e =>
          e.isInstanceOf[ComponentType]).filter(e =>
          e.asInstanceOf[ComponentType].getName.equals(promoteComponent)).foreach(e =>
          e.asInstanceOf[ComponentType].getProvided.union(e.asInstanceOf[ComponentType].getRequired).filter(p =>
            p.getName.equals(promotePort)).foreach(p1 => portRef.setRef(p1.asInstanceOf[PortTypeRef].getRef)))
      }

    }

    portRef
  }

  def createPortRef(root: ContainerRoot, node: Node): PortTypeRef = {
    val portRef = KevoreeFactory.createPortTypeRef
    portRef.setOptional(true)
    portRef.setNoDependency(false)
    node.attribute("name").map { nameAtt =>
      portRef.setName(nameAtt.text)
    }
    val service = KevoreeFactory.createServicePortType
    node.child.foreach(serv => serv.label match {
      case "interface.java" => {
        serv.attribute("interface").map { nameAtt =>
          service.setName(nameAtt.text)
          service.setInterface(nameAtt.text)
        }
      }
      case _@ e =>
    })
    root.addTypeDefinitions(service)
    portRef.setRef(service)

    portRef
  }

  def createRepo(root: ContainerRoot): ContainerRoot = {
    var rep = KevoreeFactory.createRepository
    rep.setUrl("http://maven.kevoree.org/archiva/repository/snapshots/")
    root.addRepositories(rep)
    rep = KevoreeFactory.createRepository
    rep.setUrl("http://scala-tools.org/repo-releases")
    root.addRepositories(rep)
    rep = KevoreeFactory.createRepository
    rep.setUrl("http://maven.kevoree.org/release")
    root.addRepositories(rep)
    rep = KevoreeFactory.createRepository
    rep.setUrl("http://maven.kevoree.org/snapshots")
    root.addRepositories(rep)
    rep = KevoreeFactory.createRepository
    rep.setUrl("http://repo1.maven.org/maven2")
    root.addRepositories(rep)
    root
  }

  /*
 <dataTypes xsi:type="kevoree:TypedElement" name="java.lang.String"></dataTypes>
 <libraries name="JavaSE" xsi:type="kevoree:TypeLibrary" subTypes="//@typeDefinitions.0 //@typeDefinitions.3 //@typeDefinitions.5 //@typeDefinitions.7"></libraries>
 <deployUnits type="bundle" version="1.5.1-SNAPSHOT" groupName="org.kevoree.library.javase" xsi:type="kevoree:DeployUnit" unitName="org.kevoree.library.javase.fakeDomo" hashcode="201202011013386" targetNodeType="//@typeDefinitions.1"></deployUnits>
 */

  def addCurrentDeployUnit(model: ContainerRoot, version: String, groupId: String, artefactId: String): ContainerRoot = {
    val nodeType = KevoreeFactory.createNodeType
    nodeType.setName("FrascatiNode")
    val dep = KevoreeFactory.createDeployUnit
    dep.setType("bundle")
    dep.setVersion(version)
    dep.setGroupName(groupId)
    dep.setUnitName(artefactId)
    dep.setHashcode("" + System.currentTimeMillis())
    dep.setTargetNodeType(Option(nodeType))
    model.addTypeDefinitions(nodeType)
    model.addDeployUnits(dep)
    model
  }

}