package org.kevoree.tools.aether.framework

import java.io.File
import org.kevoree.*
import org.kevoree.framework.KevoreePlatformHelper
import java.util
import java.util.ArrayList
import java.util.HashSet
import org.kevoree.resolver.MavenResolver


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/05/12
 * Time: 14:06
 */

trait AetherFramework {

    var resolver : MavenResolver

    fun resolveMavenArtifact(unitName: String, groupName: String, version: String, repositoriesUrl: List<String>): File? {
        return resolveMavenArtifact(unitName, groupName, version, null, repositoriesUrl)
    }

    fun resolveMavenArtifact(unitName: String, groupName: String, version: String, extension: String?, repositoriesUrl: List<String>): File? {
         var localextension = extension
        if(localextension == null){
            localextension = "jar"
        }
        return resolver.resolve(groupName, unitName, version, localextension, repositoriesUrl)
    }


    fun resolveDeployUnit(du: DeployUnit): File? {

        var urls: List<String>? = null
        if (System.getProperty("kevoree.offline") != null && System.getProperty("kevoree.offline").equals("true")) {
            urls = ArrayList<String>()
        } else {
            urls = buildPotentialMavenURL(du.eContainer() as ContainerRoot)
        }
        return resolver.resolve(du.getGroupName(), du.getUnitName(), du.getVersion(), "jar", urls)
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

    /*
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
    } */

}
