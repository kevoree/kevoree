package org.kevoree.tools.aether.framework

import java.io.File
import org.slf4j.LoggerFactory
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.installation.InstallRequest
import org.sonatype.aether.artifact.Artifact
import org.sonatype.aether.impl.RepositoryEventDispatcher
import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RepositoryPolicy
import org.sonatype.aether.ConfigurationProperties
import org.sonatype.aether.impl.internal.DefaultInstaller
import org.sonatype.aether.RepositoryEvent
import org.sonatype.aether.RepositorySystem
import org.sonatype.aether.impl.internal.EnhancedLocalRepositoryManagerFactory
import org.sonatype.aether.spi.locator.ServiceLocator
import org.sonatype.aether.repository.LocalRepository


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 05/03/12
 * Time: 18:42
 */

trait MavenCacheManager {

    var cache_repository: File

    fun getCacheRepository(): File {
        if (cache_repository == null) {
            cache_repository = File.createTempFile("kevoree_cache", "kevoree_cache")
            cache_repository.delete()
            cache_repository.mkdirs()
        }
        return cache_repository
    }

    fun newCacheRepositorySystemSession(): RepositorySystemSession {
        val session = MavenRepositorySystemSession()
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_NEVER)
        session.setConfigProperty("aether.connector.ahc.provider", "jdk")
        //DEFAULT VALUE
        session.setLocalRepositoryManager(newCacheRepositorySystem()!!.newLocalRepositoryManager(LocalRepository(getCacheRepository())))
        session.getConfigProperties()!!.put(ConfigurationProperties.REQUEST_TIMEOUT, 3000 as Int)
        session.getConfigProperties()!!.put(ConfigurationProperties.CONNECT_TIMEOUT, 5000 as Int)
        return session
    }

    private var locator: DefaultServiceLocator

    fun newCacheRepositorySystem(): RepositorySystem? {
        locator = DefaultServiceLocator()
        locator.setServices(javaClass<Logger>(), AetherLogger()) // Doesn't work to JdkAsyncHttpProvider because this class uses its own logger and not the one provided by plexus and set with this line
        locator.setService(javaClass<LocalRepositoryManagerFactory>(), javaClass<EnhancedLocalRepositoryManagerFactory>())
        locator.setService(javaClass<RepositoryConnectorFactory>(), javaClass<FileRepositoryConnectorFactory>())
        locator.setService(javaClass<RepositoryConnectorFactory>(), javaClass<AsyncRepositoryConnectorFactory>())
        return locator.getService(javaClass<RepositorySystem>())
    }

    fun installInCache(jarArtifact: Artifact): File? {
        val installRequest = InstallRequest()
        installRequest.addArtifact(jarArtifact)
        val initInstaller = DefaultInstaller()
        initInstaller.initService(locator)
        val dispatcher = RepositoryEventDispatcherInstalled()
        initInstaller.setRepositoryEventDispatcher(dispatcher)
        initInstaller.install(newCacheRepositorySystemSession(), installRequest)
        return dispatcher.getInstalledFile()
    }


}
