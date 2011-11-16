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

package org.kevoree.tools.marShell.interpreter.utils

import org.kevoree.{ContainerRoot, ContainerNode, Instance, KevoreeFactory}


object Merger {

  
  def mergeFragmentDictionary(inst: Instance, fragmentProps: java.util.HashMap[String,java.util.Properties]) = {
    import scala.collection.JavaConversions._
    
    fragmentProps.keySet().foreach { propKey =>
      println("Merge key "+propKey)
      
      propKey match {
        case "*"=> mergeDictionary(inst,fragmentProps.get(propKey),None)
        case _ @ searchNodeName => {
          inst.getTypeDefinition.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == searchNodeName) match {
            case Some(nodeFound)=> {
              mergeDictionary(inst,fragmentProps.get(propKey),Some(nodeFound))
            }
            case None => {
              throw new Exception("Unknown nodeName for name "+searchNodeName)
            }
          }
        }
      }
    }
  }
  
  
  /* Goal of this method is to merge dictionary definition with already exist instance defintion */
  def mergeDictionary(inst: Instance, props: java.util.Properties, targetNode : Option[ContainerNode]) = {
    import scala.collection.JavaConversions._
    props.keySet.foreach {
      key =>
        val newValue = props.get(key)

        var dictionary = inst.getDictionary
        if (dictionary.isEmpty) {
          dictionary = Some(KevoreeFactory.eINSTANCE.createDictionary)
          inst.setDictionary(dictionary)
        }

        inst.getDictionary.get.getValues.find(value => { (value.getAttribute.getName == key) && (if(targetNode.isDefined){ value.getTargetNode.isDefined && value.getTargetNode.get.getName == targetNode.get.getName } else { value.getTargetNode.isEmpty })}) match {
          //UPDATE VALUE CASE
          case Some(previousValue) => {
            previousValue.setValue(newValue.toString)
          }
          //MERGE NEW Dictionary Attribute
          case None => {
            //CHECK IF ATTRIBUTE ALREADY EXISTE WITHOUT VALUE
            val att = inst.getTypeDefinition.getDictionaryType.get.getAttributes.find(att => att.getName == key) match {
              case None => {
                val newDictionaryValue = KevoreeFactory.eINSTANCE.createDictionaryAttribute
                newDictionaryValue.setName(key.toString)
                inst.getTypeDefinition.getDictionaryType.get.addAttributes(newDictionaryValue)
                newDictionaryValue
              }
              case Some(previousAtt) => previousAtt
            }
            val newDictionaryValue = KevoreeFactory.eINSTANCE.createDictionaryValue
            newDictionaryValue.setValue(newValue.toString)
            newDictionaryValue.setAttribute(att)
            newDictionaryValue.setTargetNode(targetNode)
            inst.getDictionary.get.addValues(newDictionaryValue)
          }
        }

    }

  }

}
