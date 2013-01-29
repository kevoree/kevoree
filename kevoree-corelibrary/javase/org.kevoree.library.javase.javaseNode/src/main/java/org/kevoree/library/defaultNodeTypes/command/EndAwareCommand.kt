package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.api.PrimitiveCommand

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/08/12
 * Time: 16:25
 */

trait EndAwareCommand : PrimitiveCommand {

  fun doEnd()

}
