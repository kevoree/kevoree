package org.kevoree.library.socketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 07/10/11
 * Time: 09:30
 * To change this template use File | Settings | File Templates.
 */

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
@DictionaryType({
		@DictionaryAttribute(name = "port", optional = false, fragmentDependant = true),
		@DictionaryAttribute(name = "maximum_size_messaging", defaultValue = "50", optional = false),
		@DictionaryAttribute(name = "timer", defaultValue = "2000", optional = false),
		@DictionaryAttribute(name = "replay", defaultValue = "true", optional = false, vals = {"true", "false"})
}
)
public class SocketChannel extends AbstractChannelFragment implements Runnable {

	private ServerSocket server = null;
	private List<Socket> localServerSockets = new ArrayList<Socket>();

	/* thread in charge for receiving messages   PRODUCTEUR  */
	private Thread reception_messages = null;
	private DeadMessageQueueThread sending_messages_node_dead;
	private boolean alive = false;
	/* Current ID of the message */


	private HashMap<String, Integer> fragments = null;
	final Semaphore sem = new java.util.concurrent.Semaphore(1);

	private Logger logger = LoggerFactory.getLogger(SocketChannel.class);

	public Map<String, Socket> getClientSockets () {
		return clientSockets;
	}

	private Map<String, Socket> clientSockets = new HashMap<String, Socket>();


	@Override
	public Object dispatch (Message message) {

		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
			forward(p, message);
		}
		for (KevoreeChannelFragment cf : getOtherFragments()) {
			if (!message.getPassedNodes().contains(cf.getNodeName())) {
				forward(cf, message);
			}
		}
		return null;
	}

	public String getAddress (String remoteNodeName) {
		String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
				org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	public int parsePortNumber (String nodeName) throws IOException {
		try {
			//logger.debug("look for port on " + nodeName);
			return KevoreeFragmentPropertyHelper
					.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
							nodeName);
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Start
	public void startChannel () {
		logger.debug("Socket channel is starting ");
		Integer maximum_size_messaging = Integer.parseInt(getDictionary().get("maximum_size_messaging").toString());
		Integer timer = Integer.parseInt(getDictionary().get("timer").toString());
		sending_messages_node_dead = new DeadMessageQueueThread(this, timer, maximum_size_messaging);
		reception_messages = new Thread(this);
		alive = true;
		reception_messages.start();
		sending_messages_node_dead.start();
		fragments = new HashMap<String, Integer>();
	}

	@Stop
	public void stopChannel () {

		sending_messages_node_dead.stopProcess();
		this.alive = false;
		logger.debug("Socket channel is closing ");
		try {
			reception_messages.interrupt();
			sending_messages_node_dead.interrupt();

		} catch (Exception e) {
			//logger.error(""+e);
		}
		for (Socket socket : clientSockets.values()) {
			try {
				socket.close();
			} catch (IOException e) {
				//logger.error("Error while trying to close socket", e);
			}
		}
		for (Socket socket : localServerSockets) {
			try {
				socket.close();
			} catch (IOException e) {
				// logger.error("Error while trying to close socket", e);
			}
		}
		// clean cache sockets
		clientSockets.clear();
		localServerSockets.clear();
		logger.debug("Socket channel is closed ");
	}

	@Update
	public void updateChannel () {
		stopChannel();
		startChannel(); // TODO CHECK MSG IN QUEUE
	}


	@Override
	public ChannelFragmentSender createSender (final String remoteNodeName, String remoteChannelName) {
		return new ChannelFragmentSender() {
			@Override
			public Object sendMessageToRemote (Message msg) {
				int port = 0;
				String host = "";
				Socket client_consumer = null;
				OutputStream os = null;
				InputStream is = null;
				ObjectOutputStream oos = null;
				ObjectInputStream ois = null;

				try {
					sem.acquire();
					logger.debug("Sending message to " + remoteNodeName);
					msg.setDestNodeName(remoteNodeName);
					host = getAddress(msg.getDestNodeName());
					port = parsePortNumber(msg.getDestNodeName());

					// adding the current node  to passedNodes
					if (!msg.getPassedNodes().contains(getNodeName())) {
						msg.getPassedNodes().add(getNodeName());
					}

					// create the link if not exist
					client_consumer = getOrCreateSocket(host, port);
					os = client_consumer.getOutputStream();
					oos = new ObjectOutputStream(os);
					oos.writeObject(msg);
					oos.flush();

					is = client_consumer.getInputStream();
					ois = new ObjectInputStreamImpl(is, SocketChannel.this);
					return ois.readObject();
				} catch (Exception e) {
					logger.warn("Unable to send message to " + msg.getDestNodeName() + e);

					delete_link(host, port);
					if (getDictionary().get("replay").toString().equals("true")) {
						sending_messages_node_dead.addToDeadQueue(msg);
					}
				} finally {
					try {
						if (ois != null) {
							ois.close();
						}
						if (oos != null) {
							oos.close();
						}
						if (is != null) {
							is.close();
						}
						if (os != null) {
							os.close();
						}
						if (client_consumer != null) {
							client_consumer.close();
						}
					} catch (IOException ignored) {
						// do nothing because we simply want to close streams and socket
					}
//					msg = null;
					sem.release();
				}

				return null;
			}
		};
	}


	public void nodeDown (Socket client) {
		logger.warn("Node is down ");
		localServerSockets.remove(client);
		try {
			client.close();
		} catch (IOException ignored) {

		}
	}


	@Override
	public void run () {
		try {
			int port = parsePortNumber(getNodeName());
			logger.debug("Running Socket server <" + getNodeName() + "> port <" + port + ">");
			server = new ServerSocket(port);
			server.setSoTimeout(2000);
			server.setReuseAddress(true);
			manageClient();
		} catch (IOException e) {
			logger.error("Unable to create ServerSocket", e);
		} finally {
			if (server != null) {
				logger.debug("The ServerSocket pool is closing");
				try {
					server.close();
				} catch (IOException ignored) {

				}
				logger.debug("ServerSocket is closed");
			}
		}
	}

	public void delete_link (String host, Integer port) {
		/// link is down
		clientSockets.remove(host + port);
	}

	public Socket getOrCreateSocket (String host, Integer port) throws IOException {
		Socket client_consumer;

		if (clientSockets.containsKey(host + port)) {
			//  logger.debug("the link exist");
			client_consumer = clientSockets.get(host + port);
		} else {

			//   logger.debug("no link in cache");
			client_consumer = new Socket(host, port);
			client_consumer.setTcpNoDelay(true);
			//When a TCP connection is closed the connection may remain in a timeout state for a period of time after the connection is closed (typically known as the TIME_WAIT state or 2MSL wait state). For applications using a well known socket address or port it may not be possible to bind a socket to the required SocketAddress if there is a connection in the timeout state involving the socket address or port.
			client_consumer.setReuseAddress(true);
			client_consumer.setSoTimeout(2000);
			client_consumer.setKeepAlive(true);

			clientSockets.put(host + port, client_consumer);
		}
		return client_consumer;
	}

	private void manageClient () {
		int maxConcurrentClients = 50;
		final Semaphore sem = new Semaphore(maxConcurrentClients);
		Executor pool = Executors.newFixedThreadPool(maxConcurrentClients);
		while (alive) {
			Socket client = null;
			InputStream stream = null;
			try {
				sem.acquire();
				/*} catch (InterruptedException e) {
				 continue;
			 }*/
//			try {
				client = server.accept();
				logger.debug("Message received");
				localServerSockets.add(client);
				stream = client.getInputStream();
				final InputStream tmpStream = stream;
				final Socket tmpClient = client;

				pool.execute(new Runnable() {
					@Override
					public void run () {
						readIncomingMessage(tmpStream, tmpClient);

					}
				});
			} catch (Exception ignored) {
			} finally {
				try {
					if (stream != null) {
						stream.close();
					}
					if (client != null) {
						client.close();
					}
				} catch (Exception e) {
					// Ignore
				}
			}
		}
	}

	private void readIncomingMessage (InputStream stream, Socket client) {
		boolean _alive = true;
		Message msg;
		ObjectInputStream ois = null;
		try {
			while (_alive) {
				if (stream != null) {
					ois = new ObjectInputStreamImpl(stream, SocketChannel.this);
					msg = (Message) ois.readObject();
				} else {
					// the remote node close the channel (update, down )
					nodeDown(client);
					_alive = false;
					msg = null;
				}

				if (msg != null) {
					logger.debug("Reading message from " + msg.getDestNodeName());

					if (!msg.getPassedNodes().contains(getNodeName())) {
						msg.getPassedNodes().add(getNodeName());
					}

					forwardMessage(msg);
					// remove duplicate message
					/*if (getOtherFragments().size() > 1) {
						   int val;
						   if (fragments.containsKey(msg.getUuid().toString())) {
							   val = fragments.get(msg.getUuid().toString());
							   val = val + 1;
						   } else {
							   val = 1;
							   remoteDispatch(msg);
						   }
						   // save
						   fragments.put(msg.getUuid().toString(), val);

						   if (val == getOtherFragments().size()) {
							   fragments.remove(msg.getUuid().toString());
						   }

					   } else {
						   // two nodes
						   remoteDispatch(msg);
					   }*/
				} else {
					nodeDown(client);
					_alive = false;
					/*msg = null;*/
				}
				//				msg = null;

			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			nodeDown(client);
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (IOException ignored) {
			}
			sem.release();
		}
	}


	private void forwardMessage (Message msg) {
		// remove duplicate message
		if (getOtherFragments().size() > 1) {
			int val;
			if (fragments.containsKey(msg.getUuid().toString())) {
				val = fragments.get(msg.getUuid().toString());
				val = val + 1;
			} else {
				val = 1;
				Object result = remoteDispatch(msg); // FIXME return result to the client
			}
			// save
			fragments.put(msg.getUuid().toString(), val);

			if (val == getOtherFragments().size()) {
				fragments.remove(msg.getUuid().toString());
			}

		} else {
			// two nodes
			Object result = remoteDispatch(msg); // FIXME return result to the client
		}
	}

}
