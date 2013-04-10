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
package org.kevoree.tools.aether.framework.min

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

import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import java.io.File
import org.sonatype.aether.artifact.Artifact
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.connector.wagon.WagonProvider

import org.kevoree.tools.aether.framework.AetherFramework
import org.sonatype.aether.RepositorySystem
import org.sonatype.aether.RepositoryCache
import org.kevoree.tools.aether.framework.NoopCache
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManagerFactory
import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RepositoryPolicy
import org.sonatype.aether.ConfigurationProperties
import org.sonatype.aether.repository.LocalRepository
import org.slf4j.LoggerFactory


//import org.sonatype.aether.connector.wagon.WagonProvider

/**
 * User: ffouquet
 * Date: 25/07/11
 * Time: 15:06
 */

object AetherUtil : AetherFramework {
    override var _repositorySystem: RepositorySystem? = null
    override var _repositorySession: RepositorySystemSession? = null
    override var logger : org.slf4j.Logger= LoggerFactory.getLogger(this.javaClass)!!

    override fun getRepositorySystem(): RepositorySystem {
        val locator = DefaultServiceLocator()
        locator.setService(javaClass<RepositoryCache>(), javaClass<NoopCache>())
        locator.addService(javaClass<LocalRepositoryManagerFactory>(), javaClass<SimpleLocalRepositoryManagerFactory>())
        locator.addService(javaClass<RepositoryConnectorFactory>(), javaClass<FileRepositoryConnectorFactory>())
        locator.setServices(javaClass<WagonProvider>(), ManualWagonProvider())
        locator.addService(javaClass<RepositoryConnectorFactory>(), javaClass<WagonRepositoryConnectorFactoryFork>())
        return locator.getService(javaClass<RepositorySystem>())!!
    }

    override fun getRepositorySystemSession(): RepositorySystemSession {
        val session = MavenRepositorySystemSession()
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
        session.setLocalRepositoryManager(getRepositorySystem().newLocalRepositoryManager(LocalRepository(System.getProperty("user.home").toString() + "/.m2/repository")))
        session.getConfigProperties()!!.put(ConfigurationProperties.REQUEST_TIMEOUT, 2000 as Int)
        session.getConfigProperties()!!.put(ConfigurationProperties.CONNECT_TIMEOUT, 2000 as Int)
        return session
    }

    override fun installInCache(jarArtifact: Artifact) : File? {
        return jarArtifact.getFile()
    }


}