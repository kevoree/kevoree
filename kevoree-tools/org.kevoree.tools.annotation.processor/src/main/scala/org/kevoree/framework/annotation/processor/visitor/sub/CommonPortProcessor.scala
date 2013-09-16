package org.kevoree.framework.annotation.processor.visitor.sub

import org.kevoree._
import scala.collection.JavaConversions._
import scala.Some

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/09/13
 * Time: 15:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
trait CommonPortProcessor {

  protected def definedInheritance(newPortType: PortType, inheritedTypes: java.util.List[PortType], previousPortType: PortType): Option[PortType] = {
    if (newPortType.getClass == previousPortType.getClass) {
      newPortType match {
        case portType: ServicePortType =>
          val alreadyExistingType = inheritedTypes.find(inheritedType => {
            inheritedType.modelEquals(previousPortType)
          })
          if (alreadyExistingType.isDefined) {
            //            cleanSuperTypes(inheritedTypes, alreadyExistingType.get)
            newPortType.removeSuperTypes(alreadyExistingType.get)
            newPortType.addSuperTypes(previousPortType)
            //            inheritedTypes.add(previousPortType)
            Some(previousPortType)
          } else if (isSuperType(newPortType, previousPortType)) {
            Some(newPortType)
          } else {
            None
          }
        case portType: MessagePortType => None
        case _ =>
          None
      }
    } else {
      None
    }
  }

  protected def isSuperType(superType: PortType, typeDefinition: TypeDefinition): Boolean = {
    typeDefinition.getSuperTypes.exists(inheritedType => {
      inheritedType.modelEquals(superType)
    } || (inheritedType.isInstanceOf[PortType] && isSuperType(superType, inheritedType.asInstanceOf[PortType])))
  }

}
