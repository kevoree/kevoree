
package org.kevoree.tools.aether.framework

import org.sonatype.aether.RepositorySystem
import org.sonatype.aether.RepositorySystemSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * User: ffouquet
 * Date: 25/07/11
 * Time: 15:06
 */

object AetherUtil : AetherFramework {
    override var logger: Logger = LoggerFactory.getLogger(this.javaClass)!!
    override var _repositorySystem: RepositorySystem? = null
    override var _repositorySession: RepositorySystemSession? = null


}