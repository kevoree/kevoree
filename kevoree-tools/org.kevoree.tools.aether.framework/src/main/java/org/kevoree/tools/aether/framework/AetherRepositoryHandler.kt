
package org.kevoree.tools.aether.framework

import org.sonatype.aether.RepositorySystemSession
import org.slf4j.LoggerFactory
import org.sonatype.aether.RepositorySystem

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/04/12
 * Time: 22:06
 */

trait AetherRepositoryHandler {

  var _repositorySystem: RepositorySystem?
  var _repositorySession: RepositorySystemSession?


    fun setRepositorySystem(rs: RepositorySystem) {
    _repositorySystem = rs
  }

  fun getRepositorySystem(): RepositorySystem? {
    if (_repositorySystem == null) {
      return AetherRepositoryStandalone.newRepositorySystem()
    } else {
      return _repositorySystem
    }
  }

  fun setRepositorySystemSession(rs: RepositorySystemSession) {
    _repositorySession = rs
  }

  fun getRepositorySystemSession() : RepositorySystemSession {
    if (_repositorySession == null) {
      return AetherRepositoryStandalone.newRepositorySystemSession()
    } else {
      return _repositorySession!!
    }
  }

}
