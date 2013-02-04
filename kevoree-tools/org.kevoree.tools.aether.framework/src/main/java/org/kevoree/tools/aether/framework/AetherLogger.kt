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
package org.kevoree.tools.aether.framework

import org.sonatype.aether.spi.log.Logger
import org.slf4j.LoggerFactory

/**
 * User: ffouquet
 * Date: 18/08/11
 * Time: 15:15
 */

class AetherLogger: Logger {
    val logger = LoggerFactory.getLogger(this.javaClass)!!

    override fun debug(p1: String?) {
        //    logger.debug(p1)
    }

    override fun debug(p1: String?, p2: Throwable?) {
        //    logger.debug(p1,p2)
    }

    override fun isDebugEnabled() : Boolean { return false }

    override fun isWarnEnabled() : Boolean { return logger.isWarnEnabled()  }

    override fun warn(p1: String?) {
        return logger.warn(p1)
    }

    override fun warn(p1: String?, p2: Throwable?) {
        return logger.warn(p1, p2)
    }
}