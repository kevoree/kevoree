package org.kevoree.tools.aether.framework.android

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

import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory
import org.sonatype.aether.spi.locator.ServiceLocator
import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository

/**
 * User: ffouquet
 * Date: 04/08/11
 * Time: 21:43
 */

class WagonRepositoryConnectorFactoryFork extends WagonRepositoryConnectorFactory {

  override def initService(locator: ServiceLocator) {
    super.initService(locator)
    setWagonProvider(new ManualWagonProvider)

  }

  override def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = {
    super.newInstance(session, repository)
  }
}