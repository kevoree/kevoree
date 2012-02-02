package org.kevoree.library.defaultNodeTypes.osgi.deploy

import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeMapping

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 01/02/12
 * Time: 21:10
 * To change this template use File | Settings | File Templates.
 */

case class KevoreeOSGIMapping(override val name : String,override val objClassName : String,override val ref : Any,bundleID : Long) extends KevoreeMapping(name,objClassName,ref) {

}