package org.kevoree.library.sky.api.checker

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

  private var id: String = ""

    def setId (id: String) {
      this.id = id
    }

  def getId: String = {
    id
  }

}
