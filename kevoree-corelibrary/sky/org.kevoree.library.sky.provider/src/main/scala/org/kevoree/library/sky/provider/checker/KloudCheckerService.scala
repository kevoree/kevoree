package org.kevoree.library.sky.provider.checker

import org.kevoree.api.service.core.checker.CheckerService

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/06/12
 * Time: 13:59
 *
 * @author Erwan Daubert
 * @version 1.0
 */

trait KloudCheckerService extends CheckerService{

  private var login: String = ""

    def setLogin (login: String) {
      this.login = login
    }

  def getLogin: String = {
    login
  }

}
