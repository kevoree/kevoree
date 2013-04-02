
package org.kevoree.tools.aether.framework

import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManagerFactory
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.transfer.TransferEvent
import org.sonatype.aether.repository.LocalRepository
import java.io.File
import org.slf4j.LoggerFactory
import org.sonatype.aether.RepositoryCache
import org.sonatype.aether.ConfigurationProperties
import org.sonatype.aether.RepositorySystem
import java.util
import org.sonatype.aether.repository.RepositoryPolicy
import org.sonatype.aether.RepositorySystemSession

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/04/12
 * Time: 22:44
 */

object AetherRepositoryStandalone {

  fun newRepositorySystem():RepositorySystem? {
    val locator = DefaultServiceLocator()
    locator.setServices(javaClass<Logger>(), AetherLogger()) // Doesn't work to JdkAsyncHttpProvider because this class uses its own logger and not the one provided by plexus and set with this line
    locator.setService(javaClass<RepositoryCache>(), javaClass<NoopCache>())
    locator.setService(javaClass<LocalRepositoryManagerFactory>(), javaClass<SimpleLocalRepositoryManagerFactory>())
    locator.setService(javaClass<RepositoryConnectorFactory>(), javaClass<FileRepositoryConnectorFactory>())
    locator.setService(javaClass<RepositoryConnectorFactory>(), javaClass<AsyncRepositoryConnectorFactory>())
    return locator.getService(javaClass<RepositorySystem>())
  }


  fun newRepositorySystemSession():RepositorySystemSession {
    val session = MavenRepositorySystemSession()
    //session.setTransferListener(SLF4JTransferListener())
    session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)
    session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN)
    //DEFAULT VALUE
    session.setLocalRepositoryManager(newRepositorySystem()!!.newLocalRepositoryManager(LocalRepository(System.getProperty("user.home").toString() + "/.m2/repository")))
    session.getConfigProperties()!!.put(ConfigurationProperties.REQUEST_TIMEOUT, 20000 as Int)
    session.getConfigProperties()!!.put(ConfigurationProperties.CONNECT_TIMEOUT, 1000 as Int)
    return session
  }

}
