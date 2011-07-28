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
package org.kevoree.tools.model2code.sub

import scala.collection.JavaConversions._
import japa.parser.ast.body.BodyDeclaration
import japa.parser.ast.expr._
import java.util.ArrayList
import japa.parser.ast.CompilationUnit
import org.kevoree.annotation._

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 26/07/11
 * Time: 10:36
 */

trait AnnotationsSynchMethods extends ImportSynchMethods {


  def compilationUnit : CompilationUnit
  def componentType : ComponentType


  def checkOrRemoveAnnotation(declaration : BodyDeclaration, annQName : String) {
    if(declaration.getAnnotations != null) {
      val annSimpleName = annQName.substring(annQName.lastIndexOf(".")+1)
      declaration.getAnnotations.find({ann => ann.getName.toString.equals(annSimpleName)}) match {

        case Some(annot : NormalAnnotationExpr) => {

            //Remove imports of internal annotations recursively if necessary
            annot.getPairs.foreach{memberPair =>
              memberPair.getValue match {
                case internalAnnot : AnnotationExpr => //TODO
                case _ =>
              }
            }

            //Remove annotation
            declaration.getAnnotations.remove(annot)

            //Remove Import
            checkOrRemoveImport(annot.getName.toString)
          }
        case Some(annot : SingleMemberAnnotationExpr) => {

            //Remove member
            annot.getMemberValue match {
              case annot : AnnotationExpr =>
              case member => printf("AnnotationMember type not foreseen(" + member.getClass.getName + ")")
            }

            //Remove annotation
            declaration.getAnnotations.remove(annot)

            //Remove Import
            checkOrRemoveImport(annot.getName.toString)
          }
        case Some(annot : MarkerAnnotationExpr) => {
            //Remove annotation
            declaration.getAnnotations.remove(annot)

            //Remove Import
            checkOrRemoveImport(annot.getName.toString)
          }
        case None =>
      }
    }
  }

  def checkOrAddMarkerAnnotation(declaration : BodyDeclaration, annQName : String) {
    if(declaration.getAnnotations == null) {
      declaration.setAnnotations(new ArrayList[AnnotationExpr])
    }

    val annSimpleName = annQName.substring(annQName.lastIndexOf(".")+1)

    declaration.getAnnotations.find({ann => ann.getName.toString.equals(annSimpleName)}) match {
      case None => {
          declaration.getAnnotations.add(new MarkerAnnotationExpr(new NameExpr(annSimpleName)))
        }
      case Some(s)=>
    }

    //Adding import declaration
    checkOrAddImport(annQName)
  }

}