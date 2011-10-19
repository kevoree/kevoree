package org.kevoree.merger.resolver

import org.kevoree.framework.aspects.KevoreeAspects._
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/10/11
 * Time: 11:03
 * To change this template use File | Settings | File Templates.
 */

trait DictionaryAttributeResolver {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def resolveDictionaryAttribute(model : ContainerRoot){
    model.getAllInstances.foreach{ instance =>
        instance.getDictionary.map{ dictionaryInstance =>
          dictionaryInstance.getValues.foreach{ value =>
            value.getAttribute match {
              case UnresolvedDictionaryAttribute(attName)=> {
                instance.getTypeDefinition.getDictionaryType match {
                  case Some(dictionaryType)=> {
                    dictionaryType.getAttributes.find(att => att.getName == attName) match {
                      case Some(attFound)=> value.setAttribute(attFound)
                      case None => {
                        logger.error("Unconsitent dictionary type , att not found for name "+attName)
                      }
                    }
                  }
                  case None => logger.error("Unconsistent dictionary")
                }
              }
              case _ @ e => {
                logger.error("Already resolved Dictionary Attribute "+e)
              }
            }
          }
        }
    }
    
    
  }
  
}