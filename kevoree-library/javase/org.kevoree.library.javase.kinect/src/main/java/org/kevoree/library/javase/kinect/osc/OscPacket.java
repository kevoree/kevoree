package org.kevoree.library.javase.kinect.osc;

import java.util.*;
import java.io.*;
import java.net.InetAddress;


/**
 *
 * OscMessage
 * <BR><BR>
 * OpenSoundControl packet class.
 *
 * @author  Ben Chun        ben@benchun.net
 * @version 1.0
 */

public class OscPacket {
    private long time;
    private Vector messages;
    public InetAddress address;
    public int port;


    /**
     * Constructor for all incoming packets and those outgoing packets
     * whose time, address, and port will be set later.
     *
    */
    public OscPacket() {
	time = 0;
	messages = new Vector();
    }


    /**
     * Constructor for outgoing packets.
     *
     * @param  time    OSC time tag
     * @param  address destination host
     * @param  port    destination port
    */
    public OscPacket(long time, InetAddress address, int port) {
	this.time = time;
	messages = new Vector();
	this.address = address;
	this.port = port;
    }


    /**
     * Sets the time.
     *
     * @param   time    the new time
     */
    public void setTime(long time) {
	this.time = time;
    }


    /**
     * Sets the destination address.
     *
     * @param   address  the new address
     */
    public void setAddress(InetAddress address) {
	this.address = address;
    }

    /**
     * Sets the destination port.
     *
     * @param   port    the new port
     */
    public void setPort(int port) {
	this.port = port;
    }


    /**
     * Adds a message to this packet.
     *
     * @param   message   the message to add
    */
    public void addMessage(OscMessage message) {
	messages.addElement(message);
    }

    public InetAddress getAddress() {
	return address;
    }

    public int getPort() {
	return port;
    }


  /**
   * Returns an XML representation of this packet, suitable for
   * sending to Flash.  The return value should validate against
   * flosc.dtd
   *
   */
    public String getXml() {

	String xml = "";
	xml += "<OSCPACKET ADDRESS=\"" + address.getHostAddress() +
	    "\" PORT=\"" + port +
	    "\" TIME=\""+ time + "\">";

	Enumeration m = messages.elements();
	while (m.hasMoreElements()) {
	    OscMessage mess = (OscMessage)m.nextElement();
	    xml += mess.getXml();
	}

	xml += "</OSCPACKET>";
	return xml;
    }

    /**
     * Returns a byte array representation of this packet, suitable for
     * sending to OSC client applications.
     *
     */
    public byte[] getByteArray() throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DataOutputStream stream = new DataOutputStream(baos);

	// bundle
      	if (messages.size() > 1) {
	    baos.write( ("#bundle").getBytes() );
	    baos.write(0);
	    // bundles have a time tag
	    stream.writeLong(time);
	}

	// messages
	Enumeration m = messages.elements();
	while (m.hasMoreElements()) {
	    OscMessage mess = (OscMessage)m.nextElement();
	    byte[] byteArray = mess.getByteArray();
	    // bundles have message size tags
	    if (messages.size() > 1) {
		stream.writeInt(byteArray.length);
	    }
	    baos.write(byteArray);
	}
	return baos.toByteArray();
    }

    /**
     * Make the stream end on a 4-byte boundary by padding it with
     * null characters.
     *
     * @param stream The stream to align.
     */
    private void alignStream(ByteArrayOutputStream stream) throws IOException {
        int pad = 4 - ( stream.size() % 4 );
        for (int i = 0; i < pad; i++)
            stream.write(0);
    }

    /**
     * Prints out a byte array in 4-byte lines, useful for debugging.
     *
     * @param byteArray The byte array
     */
    public static void printBytes(byte[] byteArray) {
	for (int i=0; i<byteArray.length; i++) {
	    System.out.print(byteArray[i] + " (" + (char)byteArray[i] + ")  ");
	    if ((i+1)%4 == 0)
		System.out.print("\n");
	}
    }


}

