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

package org.kevoree.adaptation.deploy.osgi

import org.kevoree.adaptation.deploy.osgi.command.PrimitiveCommand
import org.slf4j.LoggerFactory

class KevoreeDeployPhase {

  var logger = LoggerFactory.getLogger(this.getClass);

  var executed : List[PrimitiveCommand] = List()

  def rollback={
    executed.reverse.foreach(c=>{
        try{
          c.undo
        } catch {
          case _ @ e => logger.error("Kevoree Phase ROLLBACK Error !!!! DEPLOYERROR=",e);
        }
      })
    executed = List()
  }

  def phase(cmds: List[PrimitiveCommand],desc:String):Boolean = {
    logger.debug(desc+"="+cmds.size)
    var intermediate = cmds.forall(c=> {
        logger.debug("Execute command "+c.getClass.getName)
        try{ c.execute } catch { case _ @ e => logger.error("Kevoree DEPLOY ERROR=",e);false }
      })

    if(intermediate){
      intermediate = cmds.filter(c=> c.mustBeStarted).forall(c=> {
          try{
            c.getLastExecutionBundle match {
              case None => false
              case Some(b) => b.start;true
            }
          } catch {
            case _ @ e => logger.error("Kevoree START ERROR="+e);false
          }
        })
    }
    if(!intermediate){
      cmds.foreach(c=>{
          try{
            c.undo
          } catch {
            case _ @ e => logger.error("Kevoree PHase ROLLBACK !!!! DEPLOYERROR="+e);
          }
        })
    }
    executed = executed ++ cmds
    logger.debug("Result : "+intermediate)
    intermediate
  }

}
