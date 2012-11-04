package org.kevoree.library.sky.provider.checker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot
import java.util

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/06/12
 * Time: 13:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class RootKloudChecker extends KloudCheckerService {

  private val logger = LoggerFactory.getLogger(this.getClass)

  var subcheckers: List[KloudCheckerService] = List(new NodeNameKloudChecker())

  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    val result: java.util.List[CheckerViolation] = new util.ArrayList()
    val beginTime = System.currentTimeMillis()
    subcheckers.foreach({
      sub =>
        sub.setId(getId)
        result.addAll(sub.check(model))
    })
    logger.debug("Model checked in " + (System.currentTimeMillis() - beginTime))
    result
  }
}
