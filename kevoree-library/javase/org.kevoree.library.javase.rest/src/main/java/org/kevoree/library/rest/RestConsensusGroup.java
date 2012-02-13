package org.kevoree.library.rest;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 09:56
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@DictionaryType({
        @DictionaryAttribute(name = "lock_timeout", defaultValue = "1000", optional = false)
})
@GroupType
@Library(name = "JavaSE")
public class RestConsensusGroup extends RestGroup {

	@Override
	public boolean lock () {
		// TODO lock the node
		return false;
	}
}
