package org.kevoree.framework.message

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

import scala.reflect.BeanProperty

class DiscoveryMessage {
  @BeanProperty
  var originNodeName = "default"

  @BeanProperty
  var networkType = "default"

  @BeanProperty
  var networkRate = "default"

  @BeanProperty
  var art2DispatcherPort = 0

  @BeanProperty
  var art2DiscoveryPort = 0

  @BeanProperty
  var remoteAddr = ""

}