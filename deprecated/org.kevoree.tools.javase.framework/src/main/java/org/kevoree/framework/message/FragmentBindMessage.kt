
package org.kevoree.framework.message;

import org.kevoree.framework.KevoreeChannelFragment

data class FragmentBindMessage(var proxy : KevoreeChannelFragment?,var channelName : String,var fragmentNodeName:String)
