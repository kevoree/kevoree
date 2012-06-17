package org.kevoree.library.socketChannel;

import java.io.*;
import java.net.Socket;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/06/12
 * Time: 23:35
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class DataManager {

	private SocketChannel channel;

	public DataManager (SocketChannel channel) {
		this.channel = channel;
	}

	public void writeData (Socket socket, Object msg) throws IOException {
		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(msg);
		oos.flush();
	}

	public Object readData (Socket client) throws IOException, ClassNotFoundException {
		InputStream stream = client.getInputStream();
		ObjectInputStream ois = new ObjectInputStreamImpl(stream, channel);
		return ois.readObject();
	}
}
