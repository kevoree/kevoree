package org.kevoree.library.socketChannel;


import org.kevoree.framework.message.Message;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 11/10/11
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class SocketMessage extends Message implements Serializable {

	private String uuid;

	public String getUuid () {

		return uuid;
	}

	public void setUuid (String uuid) {

		this.uuid = uuid;
	}


	private void writeObject (java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(getContent());
		out.writeObject(getPassedNodes());
		out.writeBoolean(getInOut());
		out.writeLong(getTimeout());
		out.writeBytes(getDestNodeName());

	}

	private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		setContent(in.readObject());
		setPassedNodes(((List <String>) in.readObject()));
		setInOut(in.readBoolean());
		setTimeout(in.readLong());
		setDestNodeName(in.readUTF());
	}


}


