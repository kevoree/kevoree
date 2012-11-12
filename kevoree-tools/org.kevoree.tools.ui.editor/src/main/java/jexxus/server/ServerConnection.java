/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jexxus.server;

import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Represents a server's connection to a client.
 * 
 * @author Jason
 * 
 */
public class ServerConnection extends Connection {

	private final Server controller;
	private final Socket socket;
	private final OutputStream tcpOutput;
	private final InputStream tcpInput;
	private boolean connected = true;
	private int udpPort = -1;

    ServerConnection(Server controller, ConnectionListener listener, Socket socket) throws IOException {
        super(listener, socket.getInetAddress().getHostAddress());

		this.controller = controller;
		this.socket = socket;
		tcpOutput = new BufferedOutputStream(socket.getOutputStream());
		tcpInput = new BufferedInputStream(socket.getInputStream());

		startTCPListener();
	}

	private void startTCPListener() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					byte[] ret;
					try {
						ret = readTCP();
					} catch (SocketException e) {
						if (connected) {
							connected = false;
							controller.connectionDied(ServerConnection.this, false);
						} else {
							controller.connectionDied(ServerConnection.this, true);
						}
						break;
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					if (ret == null) {
						// the stream has ended
						if (connected) {
							connected = false;
							controller.connectionDied(ServerConnection.this, false);
						} else {
							controller.connectionDied(ServerConnection.this, true);
						}
						break;
					}
					try {
						listener.receive(ret, ServerConnection.this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.setName("Jexxus-TCPSocketListener");
		t.start();
	}

	@Override
	public synchronized void send(byte[] data, Delivery deliveryType) {
		if (connected == false) {
			throw new RuntimeException("Cannot send message when not connected!");
		}
		if (deliveryType == Delivery.RELIABLE) {
			// send with TCP
			try {
				sendTCP(data);
			} catch (IOException e) {
				System.err.println("Error writing TCP data.");
				System.err.println(e.toString());
			}
		} else if (deliveryType == Delivery.UNRELIABLE) {
			controller.sendUDP(data, this);
		}
	}

	/**
	 * Closes this connection to the client.
	 */
	public void exit() {
		connected = false;
		try {
			tcpInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			tcpOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	InetAddress getAddress() {
		return socket.getInetAddress();
	}

	int getUDPPort() {
		return udpPort;
	}

	void setUDPPort(int port) {
		this.udpPort = port;
	}

	@Override
	public void close() {
		if (!connected) {
			throw new RuntimeException("Cannot close the connection when it is not connected.");
		} else {
			try {
				socket.close();
				tcpInput.close();
				tcpOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connected = false;
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	protected InputStream getTCPInputStream() {
		return tcpInput;
	}

	@Override
	protected OutputStream getTCPOutputStream() {
		return tcpOutput;
	}
}
