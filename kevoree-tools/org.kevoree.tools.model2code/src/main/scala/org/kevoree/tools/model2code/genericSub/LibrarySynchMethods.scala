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
import japa.parser.ast.expr.{StringLiteralExpr, MemberValuePair, NameExpr, NormalAnnotationExpr}
import japa.parser.ast.CompilationUnit
import org.kevoree.annotation.Library
import org.kevoree.{ContainerRoot, TypeLibrary}

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 26/07/11
 * Time: 11:00
 */

trait LibrarySynchMethods extends ImportSynchMethods{

  def compilationUnit : CompilationUnit

  def synchronizeLibrary(root : ContainerRoot, td: TypeDeclaration, typedElementName : String) {
    root.getLibraries.find({
      libraryType =>
        libraryType.getSubTypes.find({
          subType => subType.getName.equals(typedElementName)
        }) match {
          case Some(s) => true
          case None => false
        }
    }) match {
      case Some(lib : TypeLibrary) => {
        //Check Annotation
        checkOrAddLibraryAnnotation(td, lib)
      }
      case None =>
    }
  }

  def checkOrAddLibraryAnnotation(td : TypeDeclaration, lib : TypeLibrary) {
    td.getAnnotations.find({annot => annot.getName.toString.equals(classOf[Library].getSimpleName)}) match {
      case Some(annot) => {

        }
      case None => {
          td.getAnnotations.add(createLibraryAnnotation(lib))
        }
    }
    checkOrAddImport(classOf[Library].getName)
  }

  def createLibraryAnnotation(lib : TypeLibrary) : NormalAnnotationExpr = {
    val newAnnot = new NormalAnnotationExpr(new NameExpr(classOf[Library].getSimpleName), null)

    val pairs = new ArrayList[MemberValuePair]

    val portName = new MemberValuePair("name", new StringLiteralExpr(lib.getName))
    pairs.add(portName)

    newAnnot.setPairs(pairs)
    newAnnot
  }

}