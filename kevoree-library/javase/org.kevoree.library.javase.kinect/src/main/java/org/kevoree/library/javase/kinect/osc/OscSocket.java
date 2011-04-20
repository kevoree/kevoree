package org.kevoree.library.javase.kinect.osc;

import java.net.*;
import java.io.IOException;

/*
 * OscSocket is for sending OSC packets
 *
 */


public class OscSocket extends DatagramSocket {

    public OscSocket() throws SocketException {
	super();
    }

    /**
     * The only override, to send an OscPacket
     *
     * @param oscPacket OscPacket
     */
    public void send(OscPacket oscPacket) throws IOException {

        byte[] byteArray = oscPacket.getByteArray();

	// DEBUG
	//	System.out.println("OscSocket about to send this packet:");
	//	OscPacket.printBytes(byteArray);

        DatagramPacket packet =
	    new DatagramPacket( byteArray, byteArray.length,
				oscPacket.getAddress(), oscPacket.getPort() );
        send(packet);
    }

}
