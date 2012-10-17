package org.kevoree.library.defaultChannels;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.ThreadStrategy;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 17/10/12
 * Time: 16:48
 */
@Library(name = "JavaSE")
@ChannelTypeFragment(theadStrategy = ThreadStrategy.SHARED_THREAD)
public class defSERVICET extends defSERVICE {
}
