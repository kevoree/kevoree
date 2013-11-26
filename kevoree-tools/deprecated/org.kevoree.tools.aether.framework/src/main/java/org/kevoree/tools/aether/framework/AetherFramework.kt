package org.kevoree.tools.aether.framework

import java.io.File
import org.kevoree.*
import java.util.ArrayList
import java.util.HashSet
import org.kevoree.resolver.MavenResolver
import java.net.URL
import java.io.FileOutputStream

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/05/12
 * Time: 14:06
 */

trait AetherFramework {

    var resolver: MavenResolver

    fun resolveMavenArtifact(name: String, groupName: String, version: String, repositoriesUrl: List<String>): File? {
        return resolveMavenArtifact(name, groupName, version, null, repositoriesUrl)
    }

    fun resolveMavenArtifact(name: String, groupName: String, version: String, extension: String?, repositoriesUrl: List<String>): File? {
        var localextension = extension
        if(localextension == null){
            localextension = "jar"
        }
        return resolver.resolve(groupName, name, version, localextension, repositoriesUrl)
    }


    fun resolveDeployUnit(du: DeployUnit): File? {
        var urls: List<String>? = null
        if (System.getProperty("kevoree.offline") != null && System.getProperty("kevoree.offline").equals("true")) {
            urls = ArrayList<String>()
        } else {
            urls = buildPotentialMavenURL(du.eContainer() as ContainerRoot)
        }
        if (du.url != null && !"".equals(du.url)) {
            if (du.url!!.startsWith("file://") ) {
                val file = File(du.url!!.substring("file://".length()))
                if (file.exists()) {
                    return file
                } else {
                    return null
                }
            } else {
                return basicDownload(du.url!!, du.name!!, du.`type`!!)
            }
        } else {
            return resolver.resolve(du.groupName, du.name, du.version, "jar", urls)
        }
    }

    fun buildPotentialMavenURL(root: ContainerRoot): List<String> {
        var result = HashSet<String>()
        //BUILD FROM ALL REPO
        for(repo in root.repositories) {
            val nurl = repo.url
            if(nurl != null && nurl != ""){
                result.add(nurl)
            }
        }
        return result.toList()
    }

    fun basicDownload(url: String, duName: String, duType: String): File? {
        try {
            val tmpFile = File.createTempFile(duName, "." + duType)
            tmpFile.deleteOnExit()
            val remoteUrl = URL(url)
            val input = remoteUrl.openStream()
            val fos = FileOutputStream(tmpFile)
            val data = ByteArray(1024);
            var count = input!!.read(data)
            while (count != -1) {
                fos.write(data, 0, count)
                count = input.read(data)
            }
            input.close()
            fos.close()
            return tmpFile
        } catch (e: Throwable) {
            return null
        }
    }

}
