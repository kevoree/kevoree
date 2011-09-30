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

package org.kevoree.core.impl

import org.kevoree.api.configuration.ConfigurationService
import java.io.File
import org.kevoree.api.configuration.ConfigConstants.ConfigConstant
import org.kevoree.api.configuration.ConfigConstants
import org.slf4j.LoggerFactory

class KevoreeConfigServiceBean extends ConfigurationService {

  var logger = LoggerFactory.getLogger(this.getClass);
      /*
  if(System.getProperty(ConfigConstants.KEVOREE_CONFIG.getValue)!= null){
    val configF = new File(System.getProperty(ConfigConstants.KEVOREE_CONFIG.getValue))
    if(configF.exists){
      logger.info("Configure Kevoree Core with config file =>"+configF)
     // Configgy.configure(configF.getAbsolutePath)
     // config = Some(Configgy.config)
    }
  }  */

  def getProperty(constant : ConfigConstant)={
    if(System.getProperty(constant.getValue) != null){
      System.getProperty(constant.getValue)
    } else {
      /*
      config match {
        case None => constant.getDefaultValue
        case Some(config) => {
            config.getString(constant.getValue) match {
              case Some(v) => v
              case None => constant.getDefaultValue
            }
          }
      } */
      constant.getDefaultValue

    }
  }

}
