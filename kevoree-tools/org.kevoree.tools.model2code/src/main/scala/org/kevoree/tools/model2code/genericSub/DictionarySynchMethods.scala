package org.kevoree.tools.model2code.genericSub

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

import japa.parser.ast.body.TypeDeclaration
import java.util.ArrayList
import japa.parser.ast.expr._
import org.kevoree.annotation.{DictionaryAttribute, DictionaryType}
import org.kevoree.TypeDefinition

import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 26/07/11
 * Time: 10:48
 */

trait DictionarySynchMethods extends ImportSynchMethods {

  def synchronizeDictionary(td: TypeDeclaration, typeDef : TypeDefinition) {

    if (typeDef.getDictionaryType != null) {
      val dic: SingleMemberAnnotationExpr = td.getAnnotations.find({
        annot => annot.getName.toString.equals(classOf[DictionaryType].getSimpleName)
      }) match {
        case Some(annot) => annot.asInstanceOf[SingleMemberAnnotationExpr]
        case None => {
          val newDic = createDictionaryAnnotation()
          td.getAnnotations.add(newDic)
          newDic
        }
      }
      checkOrUpdateDictionary(td, typeDef, dic)
    }
  }

  def checkOrUpdateDictionary(td : TypeDeclaration, typeDef : TypeDefinition, dicAnnot : SingleMemberAnnotationExpr) {
    //for each attribute in the model
    typeDef.getDictionaryType.get.getAttributes.foreach{dicAtt =>
      //retreive or create the attribute annotation
      dicAnnot.getMemberValue.asInstanceOf[ArrayInitializerExpr].getValues.find({member =>
          member.asInstanceOf[NormalAnnotationExpr].getPairs.find({pair =>
              pair.getName.equals("name") && pair.getValue.asInstanceOf[StringLiteralExpr].getValue.equals(dicAtt.getName)}) match {
            case Some(s)=>true
            case None=>false
          }}) match {

        case Some(ann)=>updateDictionaryAtribute(ann.asInstanceOf[NormalAnnotationExpr], dicAtt)
        case None => dicAnnot.getMemberValue.asInstanceOf[ArrayInitializerExpr].getValues.add(
            createDictionaryAttributeAnnotation(typeDef.getDictionaryType.get, dicAtt))
      }
    }
  }

  def updateDictionaryAtribute(attributeAnn : NormalAnnotationExpr, dictAttr : org.kevoree.DictionaryAttribute) {
    //TODO
  }

  def createDictionaryAnnotation() : SingleMemberAnnotationExpr = {
    val newAnnot = new SingleMemberAnnotationExpr(new NameExpr(classOf[DictionaryType].getSimpleName), null)
    val memberValue = new ArrayInitializerExpr
    memberValue.setValues(new ArrayList[Expression])
    newAnnot.setMemberValue(memberValue)
    checkOrAddImport(classOf[DictionaryType].getName)
    checkOrAddImport(classOf[DictionaryAttribute].getName)
    newAnnot
  }

  def createDictionaryAttributeAnnotation(dict : org.kevoree.DictionaryType, dictAttr : org.kevoree.DictionaryAttribute) : NormalAnnotationExpr = {
    val newAnnot = new NormalAnnotationExpr(new NameExpr(classOf[DictionaryAttribute].getSimpleName), null)

    val pairs = new ArrayList[MemberValuePair]

    val portName = new MemberValuePair("name", new StringLiteralExpr(dictAttr.getName))
    pairs.add(portName)

    if(dictAttr.getOptional) {
      val opt = new MemberValuePair("optional", new BooleanLiteralExpr(dictAttr.getOptional.booleanValue))
      pairs.add(opt)
    }

    dict.getDefaultValues.find({defVal => defVal.getAttribute.getName.equals(dictAttr.getName)}) match {
      case Some(defVal) => {
          val defValue = new MemberValuePair("defaultValue", new StringLiteralExpr(defVal.getValue))
          pairs.add(defValue)
        }
      case None =>
    }

    newAnnot.setPairs(pairs)
    newAnnot
  }

}