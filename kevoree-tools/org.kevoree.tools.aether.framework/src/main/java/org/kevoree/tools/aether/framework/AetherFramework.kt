package org.kevoree.tools.aether.framework

import org.slf4j.LoggerFactory
import java.io.File
import org.sonatype.aether.artifact.Artifact
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.kevoree.*
import org.sonatype.aether.repository.Authentication
import org.sonatype.aether.repository.RemoteRepository
import org.kevoree.framework.KevoreePlatformHelper
import org.sonatype.aether.resolution.VersionRequest
import org.sonatype.aether.resolution.ArtifactRequest
import java.util
import org.slf4j.Logger
import java.util.ArrayList
import java.util.HashSet


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/05/12
 * Time: 14:06
 */

trait AetherFramework: TempFileCacheManager, AetherRepositoryHandler, CorruptedFileChecker {

    var logger: Logger

    fun resolveMavenArtifact(unitName: String, groupName: String, version: String, repositoriesUrl: List<String>): File? {
        return resolveMavenArtifact(unitName, groupName, version, null, repositoriesUrl)
    }

    fun resolveMavenArtifact(unitName: String, groupName: String, version: String, extension: String?, repositoriesUrl: List<String>): File? {
        val artifact: Artifact = if (extension == null) {
            DefaultArtifact(groupName.trim() + ":" + unitName.trim() + ":" + version.trim())
        } else {
            DefaultArtifact(groupName.trim(), unitName.trim(), extension.trim(), version.trim())
        }
        val artifactRequest = ArtifactRequest()
        artifactRequest.setArtifact(artifact)
        val repositories = java.util.ArrayList<RemoteRepository>()
        for(repository in repositoriesUrl) {
            val repo = RemoteRepository()
            val purl = repository.trim().replace(':', '_').replace('/', '_').replace('\\', '_')
            repo.setId(purl)
            repo.setUrl(repository)
            repo.setContentType("default")
            repositories.add(repo)
        }
        artifactRequest.setRepositories(repositories)
        try {
            var artefactResult = getRepositorySystem()!!.resolveArtifact(getRepositorySystemSession(), artifactRequest)

            val corruptedFile = artefactResult!!.getArtifact()
            if (corruptedFile == null || !checkFile(corruptedFile.getFile()!!)) {
                //OPTIONAL REMOVE : _maven.repositories
                clearRepoCacheFile(getRepositorySystemSession(), artifactRequest.getArtifact()!!)
                artefactResult = getRepositorySystem()!!.resolveArtifact(getRepositorySystemSession(), artifactRequest)
            }

            if (checkFile(artefactResult!!.getArtifact()!!.getFile()!!)) {
                return installInCache(artefactResult!!.getArtifact()!!)
            } else {
                logger.warn("Aether return bad Corrupted File after second try , abording")
                return null
            }
        } catch (t: Throwable) {
            logger.debug("Unable to resolve artifact", t);
            return null
        }

    }


    fun resolveDeployUnit(du: DeployUnit): File? {
        var artifact: Artifact? = null
        if (du.getUrl() != null && du.getUrl().contains("mvn:")) {
            artifact = DefaultArtifact(du.getUrl().replaceAll("mvn:", "").replace("/", ":"))
        } else {
            artifact = DefaultArtifact(du.getGroupName().trim() + ":" + du.getUnitName().trim() + ":" + du.getVersion().trim())
        }
        val artifactRequest = ArtifactRequest()
        artifactRequest.setArtifact(artifact)
        var urls: List<String>? = null
        if (System.getProperty("kevoree.offline") != null && System.getProperty("kevoree.offline").equals("true")) {
            urls = ArrayList<String>()
        } else {
            urls = buildPotentialMavenURL(du.eContainer() as ContainerRoot)
        }
        val repositories: MutableList<RemoteRepository> = ArrayList<RemoteRepository>()
        for(url in urls!!) {
            val repo = RemoteRepository()
            val purl = url.trim().replace(':', '_').replace('/', '_').replace('\\', '_')
            repo.setId(purl)
            repo.setContentType("default")
            repo.setUrl(url)
            repositories.add(repo)
        }
        try {
            artifactRequest.setRepositories(repositories)
            var artefactResult = getRepositorySystem()!!.resolveArtifact(getRepositorySystemSession(), artifactRequest)
            val corruptedFile = artefactResult!!.getArtifact()
            if (corruptedFile == null || !checkFile(corruptedFile.getFile()!!)) {
                //OPTIONAL REMOVE : _maven.repositories
                clearRepoCacheFile(getRepositorySystemSession(), artifactRequest.getArtifact()!!)
                artefactResult = getRepositorySystem()!!.resolveArtifact(getRepositorySystemSession(), artifactRequest)
            }
            if (checkFile(artefactResult!!.getArtifact()!!.getFile()!!)) {
                return installInCache(artefactResult!!.getArtifact()!!)
            } else {
                logger.warn("Aether return bad Corrupted File after second try , abording")
                return null
            }
        } catch(e: Exception) {
            try {
                logger.debug("Error while resolving {} -> second try", du.getUnitName().trim(), e)
                clearRepoCacheFile(getRepositorySystemSession(), artifactRequest.getArtifact()!!)
                val artefactResult = getRepositorySystem()!!.resolveArtifact(getRepositorySystemSession(), artifactRequest)
                if (checkFile(artefactResult!!.getArtifact()!!.getFile()!!)) {
                    return installInCache(artefactResult!!.getArtifact()!!)
                } else {
                    logger.warn("Aether return bad Corrupted File after second try , abording")
                    return null
                }
            } catch(e: Exception){
                logger.debug("Error while resolving {}", du.getUnitName().trim(), e)
                return null
            }

        }
    }

    fun buildPotentialMavenURL(root: ContainerRoot): List<String> {
        var result = HashSet<String>()
        //BUILD FROM ALL REPO
        if (root != null && root.getRepositories() != null) {
            for(repo in root.getRepositories()) {
                val nurl = repo.getUrl()
                if(nurl != null && nurl != ""){
                    result.add(nurl)
                }
            }
        }
        return result.toList()
    }


    fun resolveVersion(groupName: String, unitName: String, versionProperty: String, repositoryUrls: List<String>): String {
        val artifact: Artifact = DefaultArtifact(groupName.trim() + ":" + unitName.trim() + ":" + versionProperty.trim())
        val versionRequest = VersionRequest()
        versionRequest.setArtifact(artifact)

        val repositories = java.util.ArrayList<RemoteRepository>();
        for(repository in repositoryUrls) {
            val repo = RemoteRepository()
            val purl = repository.trim().replace(':', '_').replace('/', '_').replace('\\', '_')
            repo.setId(purl)
            repo.setUrl(repository)
            repo.setContentType("default")
            repositories.add(repo)
        }
        versionRequest.setRepositories(repositories)
        val versionResult = getRepositorySystem()!!.resolveVersion(getRepositorySystemSession(), versionRequest)
        return versionResult!!.getVersion()!!
    }

}
