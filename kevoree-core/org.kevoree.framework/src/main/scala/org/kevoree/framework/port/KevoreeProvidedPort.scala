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

package org.kevoree.framework.port

import org.kevoree.framework.KevoreePort

trait KevoreeProvidedPort extends KevoreePort {

  /* Provided Port paused by default */

  pauseState = true

  override def act() = {
    react {
      case RESUME_ACTOR => {
          pauseState = false
          loop {
            react {
              case PAUSE_ACTOR => {
                  pauseState = true
                  react {
                    case RESUME_ACTOR => pauseState = false //NOTHING TO DO
                    case STOP_ACTOR(f) => pauseState = false ; stopRequest(f)
                  }
                }
              case STOP_ACTOR(f) => stopRequest(f)
              case _ @ msg => internal_process(msg)
            }
          }
        }
    }
  }


}
