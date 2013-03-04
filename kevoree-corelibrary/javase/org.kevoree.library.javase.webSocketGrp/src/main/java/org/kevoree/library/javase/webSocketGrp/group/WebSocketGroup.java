package org.kevoree.library.javase.webSocketGrp.group;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.library.BasicGroup;
import org.kevoree.library.javase.webSocketGrp.WebSocketComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Library(name = "JavaSE", names = "Android")
@GroupType
@DictionaryType({
        @DictionaryAttribute(name = "interval", defaultValue = "20000", optional = true)
})
public class WebSocketGroup extends BasicGroup implements WebSocketComponent {
	
	protected Logger logger = LoggerFactory.getLogger(WebSocketGroup.class);
	
	@Start
	public void startWebSocketGroup() {
		Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));
		
	}
	
	@Stop
	public void stopWebSocketGroup() {
		
	}

	@Override
	public String getAddress(String remoteNodeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int parsePortNumber(String nodeName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void localNotification(Object data) {
		// TODO Auto-generated method stub
		
	}


}
