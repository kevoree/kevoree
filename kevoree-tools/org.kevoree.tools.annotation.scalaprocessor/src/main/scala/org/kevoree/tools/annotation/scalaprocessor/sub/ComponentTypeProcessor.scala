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
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.annotation.scalaprocessor.sub

import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.annotation.{Provides, Requires, Library}
import org.kevoree.tools.annotation.scalaprocessor.{LocalUtility, KevoreeAnnotationProcessor}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 21:54
 */


trait ComponentTypeProcessor extends TypeDefinitionProcessor {
  this: KevoreeAnnotationProcessor =>

  import global._

  def generateComponentType(className: String, annots: List[AnnotationInfo], model: ContainerRoot) {

    LocalUtility.root = model

    val ct = KevoreeFactory.createComponentType
    ct.setName(className)
    model.addTypeDefinitions(ct)
    generateTypeDef(className,annots,ct)

    annots.filter(annot => annot.atp.toString() == classOf[Requires].getName).foreach(annot => {
      val arrayRPort = annot.assocs.find(assoc => assoc._1.toString() == "value").get._2.asInstanceOf[ArrayAnnotArg]
      arrayRPort.args.foreach{arg =>
        val typeName =arg.asInstanceOf[NestedAnnotArg].annInfo.asInstanceOf[CompleteAnnotationInfo].args
        val assocs =arg.asInstanceOf[NestedAnnotArg].annInfo.asInstanceOf[CompleteAnnotationInfo].assocs
        val portTypeRef = KevoreeFactory.eINSTANCE.createPortTypeRef
        assocs.foreach(assoc =>{
          assoc._1.toString() match {
            case "name" => portTypeRef.setName(assoc._2.asInstanceOf[LiteralAnnotArg].const.stringValue)
            case "type" => {
              assoc._2.asInstanceOf[LiteralAnnotArg].const.stringValue match {
                case "value MESSAGE"=> {
                  val pt = KevoreeFactory.eINSTANCE.createMessagePortType
                  portTypeRef.setRef(LocalUtility.getOraddPortType(pt))


                  // model.addTypeDefinitions(pt)
                }
              }
            }
            case "optional" => {
              assoc._2.asInstanceOf[LiteralAnnotArg].const.stringValue match {
                case "true"=> {
                  portTypeRef.setOptional(true)
                }
                case "false" => {
                  portTypeRef.setOptional(false)
                }
              }
            }

            case _ => println(assoc._1+"->"+assoc._2+"|"+assoc._2.getClass)
          }

            //println(assoc._1+"->"+assoc._2+"|"+assoc._2.getClass)
        })

        ct.addRequired(portTypeRef)

      }

    })



    annots.filter(annot => annot.atp.toString() == classOf[Provides].getName).foreach(annot => {
      val arrayRPort = annot.assocs.find(assoc => assoc._1.toString() == "value").get._2.asInstanceOf[ArrayAnnotArg]
      arrayRPort.args.foreach{arg =>
        val typeName =arg.asInstanceOf[NestedAnnotArg].annInfo.asInstanceOf[CompleteAnnotationInfo].args
        val assocs =arg.asInstanceOf[NestedAnnotArg].annInfo.asInstanceOf[CompleteAnnotationInfo].assocs
        val portTypeRef = KevoreeFactory.eINSTANCE.createPortTypeRef
        assocs.foreach(assoc =>{
          assoc._1.toString() match {
            case "name" => portTypeRef.setName(assoc._2.asInstanceOf[LiteralAnnotArg].const.stringValue)
            case "type" => {
              assoc._2.asInstanceOf[LiteralAnnotArg].const.stringValue match {
                case "value MESSAGE"=> {
                  val pt = KevoreeFactory.eINSTANCE.createMessagePortType
                  portTypeRef.setRef(LocalUtility.getOraddPortType(pt))
                }
              }
            }
            case _ => println(assoc._1+"->"+assoc._2+"|"+assoc._2.getClass)
          }

          //println(assoc._1+"->"+assoc._2+"|"+assoc._2.getClass)
        })

        ct.addProvided(portTypeRef)

      }

    })



  }

}
