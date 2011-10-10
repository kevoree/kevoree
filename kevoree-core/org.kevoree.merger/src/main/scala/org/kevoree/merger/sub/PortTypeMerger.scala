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

package org.kevoree.merger.sub

import org.kevoree.framework.aspects.KevoreeAspects._
import org.slf4j.LoggerFactory
import org.kevoree._

trait PortTypeMerger {

  private val logger = LoggerFactory.getLogger(this.getClass);

  //PORT TYPE DEFINITION MERGER
  def mergePortType(actualModel: ContainerRoot, portType: PortType): PortType = {
    actualModel.getTypeDefinitions.filter({
      td => td.isInstanceOf[PortType]
    }).find({
      pt => pt.getName == portType.getName
    }) match {
      case Some(existPT) => {
        //CONSISTENCY CHECK
        existPT match {
          case spt: ServicePortType => {

            println("ok found "+spt.getOperations.size)

            if (portType.isInstanceOf[ServicePortType]) {
              //CLEAR OLD METHOD , NEW DEFINITION WILL REPLACE OTHER
              val remoteOps = portType.asInstanceOf[ServicePortType].getOperations
             // spt.removeAllOperations()
              remoteOps.foreach {
                op =>
                 // val newOperation = KevoreeFactory.createOperation
                  //newOperation.setName(op.getName)
                  op.setReturnType(Some(mergeDataType(actualModel, op.getReturnType.get)))
                  op.getParameters.foreach {
                    para =>
                     // val newparam = KevoreeFactory.createParameter
                    //  newparam.setName(para.getName)
                      para.setType(Some(mergeDataType(actualModel, para.getType.get)))
                    //  newOperation.addParameters(newparam)
                  }
                //  spt.addOperations(newOperation)
              }

            } else {
              logger.debug("New service Port Type can't replace and message port type !!!")
            }
          }
          case _ => // TODO MESSAGE PORT
        }


        existPT.asInstanceOf[PortType]
      }
      case None => {
        actualModel.addTypeDefinitions(portType)
        portType match {
          case spt: ServicePortType => {

            println("before "+spt.getOperations.size)

            val operations = spt.getOperations
          //  spt.removeAllOperations()
            spt.getOperations.foreach {
              op =>
               // val newOperation = KevoreeFactory.createOperation
               // newOperation.setName(op.getName)
                op.setReturnType(Some(mergeDataType(actualModel, op.getReturnType.get)))
                op.getParameters.foreach {
                  para =>
                   // val newparam = KevoreeFactory.createParameter
                   // newparam.setName(para.getName)
                    para.setType(Some(mergeDataType(actualModel, para.getType.get)))
                   // newOperation.addParameters(newparam)
                }
               // spt.addOperations(newOperation)
            }

            println("initial merge "+spt.getOperations.size)

          }
          case mpt: MessagePortType => {
            mpt.getFilters.foreach {
              dt => mergeDataType(actualModel, dt)
            }
          }
          case _@msg => logger.debug("Error uncatch type")
        }
        portType
      }
    }
  }

  //MERGE SIMPLE DATA TYPE
  private def mergeDataType(actualModel: ContainerRoot, datatype: TypedElement): TypedElement = {
    actualModel.getDataTypes.find({
      dt => dt.isModelEquals(datatype)
    }) match {
      case Some(existDT) => existDT
      case None => {
        var dts = actualModel.addDataTypes(datatype)
        val generics = datatype.getGenericTypes.toList ++ List()
        datatype.removeAllGenericTypes()
        generics.foreach {
          dt =>
            datatype.addGenericTypes(mergeDataType(actualModel, dt))
        }

        datatype
      }
    }
  }


}
