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

package org.kevoree.tools.marShellTransform

import ast._
import org.kevoree.tools.marShell.ast._
import java.util.{Properties}
import collection.immutable.HashSet
import org.kevoree.{ContainerRoot}
import scala.collection.JavaConversions._
import org.kevoree.log.Log

object KevScriptWrapper {

  val paramSep = ":"
  val instrSep = "/"
  /**
   * jedartois@gmail.com
   * Transforms a compressed script extracted from the arduino eeprom to ast kevScript
   * @param cscript   to optain the cscript you need to send $g to arduino
   * @return  KevScript
   */
  def checksumArduino(value: String): Long = {
    val data: Array[Byte] = value.getBytes("US-ASCII")
    var checksum: Long = 0L
    for (b <- data) {
      checksum += b
    }
    checksum = checksum % 256
    return checksum
  }

  def checksum_csript(cscript : String) : Boolean = {
    var rt  : Boolean = false
    try
    {
      val parser = new ParserPush()
      val result = parser.parseAdaptations(cscript)
      var checksum : Long = 0

      var checksum_header : Long = 0
      var checksum_typedefinition : Long = 0
      var checksum_portdefinition : Long = 0
      var checksum_properties : Long = 0
      checksum_header +=checksumArduino(result.nodeName.toLowerCase)
      checksum_header +=checksumArduino(result.nodeTypeName.toLowerCase)

      if (result.definitions != None)
      {
        var i : Int = 0

        if ( result.definitions.get != null)
        {
          // checksum typdefinition
          while (i <      result.definitions.get.typedefinition.size())
          {
            val typedef = result.definitions.get.typedefinition.get(i)
            checksum_typedefinition +=checksumArduino(typedef.toLowerCase)
            i += 1;
          }
        }

        i = 0
        // checksum of properties
        while (i < result.definitions.get.properties.size())
        {
          val propertie = result.definitions.get.properties.get(i)
          checksum_properties +=checksumArduino(propertie.toLowerCase)
          i += 1;
        }
        i = 0
        // checksum of port definition
        while (i <      result.definitions.get.portdefinition.size())
        {
          val portdef = result.definitions.get.portdefinition.get(i)
          checksum_portdefinition +=checksumArduino(portdef.toLowerCase)
          i += 1;
        }

        checksum +=checksum_header
        //   checksum +=checksum_typedefinition
        // checksum +=checksum_portdefinition
        // checksum += checksum_properties

        if (checksum == result.checksum)
        {
          rt = true
        }else
        {
          Log.warn("checksum "+checksum+" != "+result.checksum)
          rt = false
        }

      }  else
      {
        // todo
        rt = true
      }

    } catch  {
      case e: Exception => {
        println("Exception ",e)
        rt = false
      }
    }

    rt
  }
  def generateKevScriptFromCompressed(cscript: String, baseModel: ContainerRoot): Script = {
    var statments = new scala.collection.mutable.ListBuffer[Statment]
    var blocks = new HashSet[TransactionalBloc]
    try {
      val parser = new ParserPush()
      val result = parser.parseAdaptations(cscript)

      val nodeName = result.nodeName

      statments += AddNodeStatment(nodeName, result.nodeTypeName, new Properties())

      if (result.definitions != None)
      {
        result.adaptations.toArray.foreach(s => {
          s match {
            case classOf: UDI => {
              // UpdateDictionaryStatement
              Log.debug("Detect UpdateDictionaryStatement")
              val props = new java.util.Properties()

              if(s.asInstanceOf[UDI].params != None){
                s.asInstanceOf[UDI].getParams.toArray.foreach(p => {
                  val prop = result.definitions.get.getPropertieById(p.asInstanceOf[PropertiePredicate].dictionnaryID)
                  props.put(prop, p.asInstanceOf[PropertiePredicate].value.toString)


                })
              }
              val fraProperties = new java.util.HashMap[String, java.util.Properties]
              fraProperties.put(result.nodeName.toString, props)

              statments += UpdateDictionaryStatement(s.asInstanceOf[UDI].getIDPredicate().getinstanceID, Some(nodeName), fraProperties)
            }

            case classOf: ABI => {
              Log.debug("Detect AddBindingStatment")
              val cid = new ComponentInstanceID(s.asInstanceOf[ABI].getIDPredicate().getinstanceID, Some(nodeName))
              val idPort = result.definitions.get.getPortdefinitionById(s.asInstanceOf[ABI].getportIDB)

              statments += AddBindingStatment(cid, idPort, s.asInstanceOf[ABI].getchID())
            }
            case classOf: AIN => {

              val cid = new ComponentInstanceID(s.asInstanceOf[AIN].getIDPredicate().getinstanceID, Some(nodeName))
              val typeIDB = result.definitions.get.getTypedefinitionById(s.asInstanceOf[AIN].getTypeIDB())

              /* Feed prop value */
              val props = new java.util.Properties()

              if(s.asInstanceOf[AIN].params != None){
                s.asInstanceOf[AIN].getParams.toArray.foreach(p => {
                  val prop = result.definitions.get.getPropertieById(p.asInstanceOf[Propertie].getdictionnaryID())
                  props.put(prop, p.asInstanceOf[PropertiePredicate].value)
                })
              }

              baseModel.getTypeDefinitions.find(td => td.getName == typeIDB) match {
                case Some(td_found) => {
                  td_found match {
                    case i : org.kevoree.ComponentType => {
                      Log.debug("Detect AddComponentInstanceStatment " + s.asInstanceOf[AIN].getIDPredicate().getinstanceID)
                      statments += AddComponentInstanceStatment(cid, typeIDB, props)
                    }
                    case i : org.kevoree.ChannelType => {
                      Log.debug("Detect AddChannelInstanceStatment " + s.asInstanceOf[AIN].getIDPredicate().getinstanceID+" "+props)
                      statments += AddChannelInstanceStatment(s.asInstanceOf[AIN].getIDPredicate().getinstanceID, typeIDB, props)
                    }
                  }
                }
                case None => {
                  throw new Exception("KevScript uncompression error")
                }
              }
            }

            case classOf: RIN => {
              Log.debug("Detect RemoveComponentInstanceStatment")
              val cid = new ComponentInstanceID(s.asInstanceOf[RIN].getInsID, Some(nodeName))

              statments += RemoveComponentInstanceStatment(cid)
            }
            case classOf: RBI => {
              Log.debug("Detect RemoveBindingStatment")
              val cid = new ComponentInstanceID(s.asInstanceOf[RBI].getIDPredicate().getinstanceID, Some(nodeName))
              val idPort = result.definitions.get.getPortdefinitionById(s.asInstanceOf[RBI].getportIDB)
              statments += RemoveBindingStatment(cid, idPort, s.asInstanceOf[RBI].getchID())
            }
            case _ => {
              Log.error("This Statment is not managed " + s.getClass.getName)
              None
            }
          }
        }
        )
      }
      blocks += TransactionalBloc(statments.toList)
      Log.debug(blocks.toString())
    } catch {
      case e: IndexOutOfBoundsException => {
        Log.error("The Arduino globals definitions (properties or typedefinition or portdefinition)  are not compliant to the adaptations")
        new Script(blocks.toList)
      }
      case e: java.lang.Exception => {
        Log.error("Fail to parse the script : "+cscript,e)
        new Script(blocks.toList)
      }
    }

    new Script(blocks.toList)
  }


  def generateDictionaryString(dictionary: java.util.Properties): String = {
    if (dictionary == null) {
      return ""
    }
    val content = new StringBuilder
    var first = true
    import scala.collection.JavaConversions._
    dictionary.foreach {
      dic =>
        if (first) {
          content append dic._1 + "=" + dic._2
          first = false
        } else {
          content append "," + dic._1 + "=" + dic._2
        }
    }
    content.toString()
  }

  def generateKevScriptCompressed(script: Script, targetNodeName: String): String = {
    if (script.blocks.isEmpty) return ""

    val content = new StringBuilder
    content append "{"
    var firstStatment = true
    script.blocks.foreach {
      block =>
        block.l.foreach {
          statement =>
            statement match {
              case s: UpdateDictionaryStatement => {
                if (!firstStatment) {
                  content append instrSep
                }
                firstStatment = false
                val dic = new java.util.Properties()
                if (s.fraProperties.containsKey("*")) {
                  dic.putAll(s.fraProperties.get("*"))
                }
                if (s.fraProperties.containsKey(targetNodeName)) {
                  dic.putAll(s.fraProperties.get(targetNodeName))
                }
                content append Op.UDI_C + paramSep + s.instanceName + paramSep + generateDictionaryString(dic)
              }
              case s: AddComponentInstanceStatment => {
                if (!firstStatment) {
                  content append instrSep
                }
                firstStatment = false
                content append Op.AIN_C + paramSep + s.cid.componentInstanceName + paramSep + s.typeDefinitionName
                if (s.dictionary != null) {
                  content append paramSep + generateDictionaryString(s.dictionary)
                }
              }
              case s: AddChannelInstanceStatment => {
                if (!firstStatment) {
                  content append instrSep
                }
                firstStatment = false
                content append Op.AIN_C + paramSep + s.channelName + paramSep + s.channelType
                if (s.dictionary != null) {
                  content append paramSep + generateDictionaryString(s.dictionary)
                }
              }
              case s: RemoveComponentInstanceStatment => {
                if (!firstStatment) {
                  content append instrSep
                }
                firstStatment = false
                content append Op.RIN_C + paramSep + s.cid.componentInstanceName
              }
              case s: RemoveChannelInstanceStatment => {
                if (!firstStatment) {
                  content append instrSep
                }
                firstStatment = false
                content append Op.RIN_C + paramSep + s.channelName
              }
              case s: AddBindingStatment => {
                if (!firstStatment) {
                  content append instrSep
                }
                firstStatment = false
                content append Op.ABI_C + paramSep + s.cid.componentInstanceName + paramSep + s.bindingInstanceName + paramSep + s.portName
              }
              case s: RemoveBindingStatment => {
                if (!firstStatment) {
                  content append instrSep
                }
                firstStatment = false
                content append Op.RBI_C + paramSep + s.cid.componentInstanceName + paramSep + s.bindingInstanceName + paramSep + s.portName
              }
              case _@s => Log.warn("Uncatch " + s) //DO NOTHING FOR OTHER STATEMENT
            }


        }
    }
    content append "}"
    content.toString()
  }

  def miniPlanKevScript(s: Script): Script = {
    var resultStatmentList: List[Statment] = List()

    //AddNode BINDING FIRST
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[AddNodeStatment])
    }

    //REMOVE BINDING FIRST
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[RemoveBindingStatment])
    }
    //REMOVE CHANNEL INST
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[RemoveChannelInstanceStatment])
    }
    //REMOVE COMPONENT INST 
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[RemoveComponentInstanceStatment])
    }
    //ADD COMPONENT INST 
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[AddComponentInstanceStatment])
    }
    //ADD CHANNEL INST 
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[AddChannelInstanceStatment])
    }
    //ADD BINDING INST 
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[AddBindingStatment])
    }
    //Update Param INST 
    s.blocks.foreach {
      block =>
        resultStatmentList = resultStatmentList ++ block.l.filter(statement => statement.isInstanceOf[UpdateDictionaryStatement])
    }
    Script(List(TransactionalBloc(resultStatmentList)))
  }


}
