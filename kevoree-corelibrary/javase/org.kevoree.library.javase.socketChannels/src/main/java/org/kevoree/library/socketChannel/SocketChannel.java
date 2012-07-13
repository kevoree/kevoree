package org.kevoree.library.socketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 07/10/11
 * Time: 09:30
 */

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

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
		@DictionaryAttribute(name = "replay", defaultValue = "false", optional = false, vals = {"true", "false"})
}
)
public class SocketChannel extends AbstractChannelFragment implements Runnable {

	//<<<<<<< HEAD
	private ServerSocket server = null;
	private List<Socket> localServerSockets = new ArrayList<Socket>();

	/* thread in charge for receiving messages   PRODUCTEUR  */
	private Thread reception_messages = null;
	private DeadMessageQueueThread sending_messages_node_dead;
	private boolean alive = false;
	int port;


	private HashMap<String, Integer> fragments = null;
	final Semaphore sem = new java.util.concurrent.Semaphore(1);

	private Logger logger = LoggerFactory.getLogger(SocketChannel.class);

	public Map<String, Socket> getClientSockets () {
		return clientSockets;
	}

	private Map<String, Socket> clientSockets = new HashMap<String, Socket>();


	@Override
	public Object dispatch (Message message) {

		Object result = null;
		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
			if (result == null) {
				result = forward(p, message);
			}
		}
		for (KevoreeChannelFragment cf : getOtherFragments()) {
			if (!message.getPassedNodes().contains(cf.getNodeName())) {
				if (result == null) {
					result = forward(cf, message);
				}
			}
		}
		return result;
	}

	public String getAddress (String remoteNodeName) {
		Option<String> ipOption = KevoreePropertyHelper
				.getStringNetworkProperty(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		String ip = "127.0.0.1";
		if (ipOption.isDefined()) {
			ip = ipOption.get();
		}
		return ip;
	}

	public int parsePortNumber (String nodeName) throws IOException {
		try {
			//logger.debug("look for port on " + nodeName);
			Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForChannel(this.getModelService().getLastModel(), this.getName(), "port", true, nodeName);
			int port = 9000;
			if (portOption.isDefined()) {
				port = portOption.get();
			}
			return port;
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Start
	public void startChannel () throws IOException {
		logger.debug("Socket channel is starting");
		Integer maximum_size_messaging = Integer.parseInt(getDictionary().get("maximum_size_messaging").toString());
		Integer timer = Integer.parseInt(getDictionary().get("timer").toString());
		sending_messages_node_dead = new DeadMessageQueueThread(this, timer, maximum_size_messaging);
		port = parsePortNumber(getNodeName());
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
		logger.debug("Socket channel is closing");
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
		logger.debug("Socket channel is closed");
	}

	@Update
	public void updateChannel () throws IOException {
		if (port != parsePortNumber(getNodeName())) {
					stopChannel();
					startChannel(); // TODO CHECK MSG IN QUEUE
				}
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
					logger.debug("Sending message to {}", remoteNodeName);
					msg.setDestNodeName(remoteNodeName);
					host = getAddress(msg.getDestNodeName());
					port = parsePortNumber(msg.getDestNodeName());

					// adding the current node  to passedNodes
					if (!msg.getPassedNodes().contains(getNodeName())) {
						msg.getPassedNodes().add(getNodeName());
					}

					// create the link if not exist
					client_consumer = getOrCreateSocket(host, port);
					/*os = client_consumer.getOutputStream();
					oos = new ObjectOutputStream(os);
					oos.writeObject(msg);
					oos.flush();*/
					writeData(client_consumer, msg);

					if (msg.inOut()) {
						is = client_consumer.getInputStream();
						ois = new ObjectInputStreamImpl(is, SocketChannel.this);
						return ois.readObject();
					} else {
						return null;
					}
				} catch (Exception e) {
					logger.warn("Unable to send message to {}", msg.getDestNodeName(), e);

					delete_link(host, port);
					if (getDictionary().get("replay").toString().equals("true")) {
						sending_messages_node_dead.addToDeadQueue(msg);
					} else {
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
					}
				} finally {

//					msg = null;
					sem.release();
				}

				return null;
			}
		};
	}

	private void writeData (Socket socket, Object msg) throws IOException {
		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(msg);
		oos.flush();
	}


	public void nodeDown (Socket client) {
		logger.warn("Node is down");
		localServerSockets.remove(client);
		try {
			client.close();
		} catch (IOException ignored) {

		}
	}


	@Override
	public void run () {
		try {
			logger.debug("Running Socket server <{}> port <{}>", getNodeName(), port);
			server = new ServerSocket(port);
//			server.setSoTimeout(2000);
			server.setReuseAddress(true);
			manageClient();
		} catch (IOException e) {
			logger.error("Unable to create ServerSocket", e);
		} finally {
			if (server != null) {
				logger.debug("The ServerSocket pool is closing");
				try {
					server.close();
					logger.debug("ServerSocket is closed");
				} catch (IOException ignored) {

				}
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
//			client_consumer.setSoTimeout(2000);
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
			Socket client;
			try {
				sem.acquire();
				client = server.accept();
				logger.debug("Message received");
				localServerSockets.add(client);
				final Socket tmpClient = client;

				pool.execute(new Runnable() {
					@Override
					public void run () {
						readIncomingMessage(/*tmpStream, */tmpClient);
					}
				});
			} catch (Exception ignored) {
			} /*finally {
				try {
					if (client != null) {
						client.close();
					}
				} catch (Exception ignored) {
				}
			}*/
		}
	}

	private void readIncomingMessage (/*InputStream stream, */Socket client) {
		boolean _alive = true;
		Message msg;
		ObjectInputStream ois = null;
		try {
			InputStream stream = client.getInputStream();
			while (_alive) {
				if (stream != null) {
					ois = new ObjectInputStreamImpl(stream, SocketChannel.this);
					msg = (Message) ois.readObject();
				} else {
					// the remote node close the channel (update, down )
					logger.debug("Stream is null so we can't read data");
					nodeDown(client);
					_alive = false;
					msg = null;
				}

				if (msg != null) {
					logger.debug("message is read from {}", msg.getDestNodeName());

					if (!msg.getPassedNodes().contains(getNodeName())) {
						msg.getPassedNodes().add(getNodeName());
					}


					Object result = forwardMessage(msg);
					logger.debug("message forwarded");
					if (msg.inOut()) {
						logger.debug("waiting for response ...");
						writeData(client, result);
					}
				} else {
					logger.debug("MSG is null so we can't use it");
					nodeDown(client);
					_alive = false;
					/*msg = null;*/
				}
				//				msg = null;

			}
		} catch (ClassNotFoundException e) {
			logger.warn("Unable to read message", e);
			nodeDown(client);
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (IOException ignored) {
			}
		} catch (IOException e) {
			logger.warn("Unable to read message", e);
			nodeDown(client);
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (IOException ignored) {
			}
		} finally {
			sem.release();
		}
	}


	private Object forwardMessage (Message msg) {
		logger.debug("Forward message to corresponding nodes");
		// remove duplicate message
		if (getOtherFragments().size() > 1) {
			int val;
			if (fragments.containsKey(msg.getUuid().toString())) {
				val = fragments.get(msg.getUuid().toString());
				val = val + 1;
				msg.getPassedNodes().add(getNodeName());
			} else {
				val = 1;
//				Object result = remoteDispatch(msg); // FIXME return result to the client
			}
			// save
			fragments.put(msg.getUuid().toString(), val);


			if (val == getOtherFragments().size()) {
				fragments.remove(msg.getUuid().toString());
			}

		}// else {
		// two nodes
		return dispatch(msg); // FIXME return result to the client
//		}
	}
}