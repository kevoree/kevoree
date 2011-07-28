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

import japa.parser.ast.body.{MethodDeclaration, TypeDeclaration}
import org.kevoree.annotation.{ComponentType, Update, Stop, Start}
import scala.collection.JavaConversions._
import japa.parser.ast.CompilationUnit

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 26/07/11
 * Time: 10:10
 */

trait LifeCycleSynchMethods
  extends GenericSynchMethods
  with AnnotationsSynchMethods {

    def compilationUnit : CompilationUnit
  def componentType : ComponentType

  /**
   * Synchronizes the start method
   */
   def synchronizeStart(td : TypeDeclaration) {
    //Check method presence
    var startMethod : MethodDeclaration = null

    if(componentType.getStartMethod == null || componentType.getStartMethod.equals("")) {

      startMethod = new MethodDeclaration

      //No start method in the model
      val possibleMethodNames = List("start", "startComponent", "startKevoreeComponent")
      val methods = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]})

      //Check available names for start method
      val availableNames = possibleMethodNames.filter({name =>
          methods.filter({method =>
              method.asInstanceOf[MethodDeclaration].getName.equals(name)}).size == 0})


      startMethod = availableNames.head match {
        case name : String => addDefaultMethod(td, name)
        case _=> {
            printf("No name found for Start method name generation. Please add the start method, and annotation by hand.")
            null
          }
      }

    } else {
      startMethod = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
          method.asInstanceOf[MethodDeclaration].getName.equals(componentType.getStartMethod)}) match {
        case Some(m:MethodDeclaration) => m
        case None=> addDefaultMethod(td, componentType.getStartMethod)
      }
    }

    //Check annotation
    if(startMethod != null) {
      checkOrAddMarkerAnnotation(startMethod, classOf[Start].getName)
    }
  }

   def synchronizeStop(td : TypeDeclaration) {
    //Check method presence
    var stopMethod : MethodDeclaration = null

    if(componentType.getStopMethod == null || componentType.getStopMethod.equals("")) {

      stopMethod = new MethodDeclaration

      //No start method in the model
      val possibleMethodNames = List("stop", "stopComponent", "stopKevoreeComponent")
      val methods = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]})

      //Check available names for start method
      val availableNames = possibleMethodNames.filter({name =>
          methods.filter({method =>
              method.asInstanceOf[MethodDeclaration].getName.equals(name)}).size == 0})


      stopMethod = availableNames.head match {
        case name : String => addDefaultMethod(td, name)
        case _=> {
            printf("No name found for Stop method name generation. Please add the stop annotation by hand.")
            null
          }
      }

    } else {
      stopMethod = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
          method.asInstanceOf[MethodDeclaration].getName.equals(componentType.getStopMethod)}) match {
        case Some(m:MethodDeclaration) => m
        case None=> addDefaultMethod(td, componentType.getStopMethod)
      }
    }

    //Check annotation
    if(stopMethod != null) {
      checkOrAddMarkerAnnotation(stopMethod, classOf[Stop].getName)
    }
  }

  def synchronizeUpdate(td : TypeDeclaration) {
    //Check method presence
    var updateMethod = new MethodDeclaration

    if(componentType.getUpdateMethod == null || componentType.getUpdateMethod.equals("")) {

      //No start method in the model
      val possibleMethodNames = List("update", "updateComponent", "updateKevoreeComponent")
      val methods = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]})

      //Check available names for start method
      val availableNames = possibleMethodNames.filter({name =>
          methods.filter({method =>
              method.asInstanceOf[MethodDeclaration].getName.equals(name)}).size == 0})


      updateMethod = availableNames.head match {
        case name : String => addDefaultMethod(td, name)
        case _=> {
            printf("No name found for Update method name generation. Please add the update annotation by hand.")
            null
          }
      }

    } else {
      updateMethod = td.getMembers.filter({member => member.isInstanceOf[MethodDeclaration]}).find({method =>
          method.asInstanceOf[MethodDeclaration].getName.equals(componentType.getStopMethod)}) match {
        case Some(m:MethodDeclaration) => m
        case None=> addDefaultMethod(td, componentType.getUpdateMethod)
      }
    }

    //Check annotation
    if(updateMethod != null) {
      checkOrAddMarkerAnnotation(updateMethod, classOf[Update].getName)
    }
  }


}