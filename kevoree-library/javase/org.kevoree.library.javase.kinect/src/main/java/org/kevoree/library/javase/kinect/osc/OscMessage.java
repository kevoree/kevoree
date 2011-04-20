package org.kevoree.library.javase.kinect.osc;

import java.util.*;
import java.io.*;

/**
 *
 * OscMessage
 * <BR><BR>
 * OpenSoundControl message class.
 *
 * @author  Ben Chun        ben@benchun.net
 * @version 1.0
 */

public class OscMessage {
    private String name;
    private Vector types;
    private Vector arguments;

    /**
     * Constructor for the OscMessage.
     *
     * @param   name    the message name
    */
    public OscMessage(String name) {
	this.name = name;
	types = new Vector();
	arguments = new Vector();
    }

    /**
     * Adds a type/argument pair to the list of arguments
     *
     * @param   type           the argument data type
     * @param   argument       the argument value
    */
    public void addArg(Character type, Object argument) {
	types.addElement(type);
	arguments.addElement(argument);
    }

    /**
     * Directly sets the type and arg Vectors
     *
     * @param   types    a list of types
     * @param   args     a list of arguments matching the types
    */
    public void setTypesAndArgs(Vector types, Vector args) {
	this.types = types;
	this.arguments = args;
    }

    /**
     * Returns an XML representation of the message
     *
     */
    public String getXml() {
	if (types == null)
	    return "ERROR: Types not set";

	String xml = "";
	xml += "<MESSAGE NAME=\"" + name + "\">";

	Enumeration t = types.elements();
	Enumeration a = arguments.elements();

	while (t.hasMoreElements()) {
	    char type = ( (Character)t.nextElement() ).charValue();

	    if (type == '[')
		xml += "<ARRAY>";
	    else if (type == ']')
		xml += "</ARRAY>";
	    else {
		xml += "<ARGUMENT TYPE=\"" + type + "\" ";
		switch(type) {
		case 'i':
		    xml += "VALUE=\"" + (Integer)a.nextElement() + "\" />";
		    break;
		case 'f':
		    xml += "VALUE=\"" + (Float)a.nextElement() + "\" />";
		    break;
		case 'h':
		    xml += "VALUE=\"" + (Long)a.nextElement() + "\" />";
		    break;
		case 'd':
		    xml += "VALUE=\"" + (Double)a.nextElement() + "\" />";
		    break;
		case 's':
		    xml += "VALUE=\"" + (String)a.nextElement() + "\" />";
		    break;
		case 'T':
		case 'F':
		case 'N':
		case 'I':
		    xml += " />";
		    break;
		}
	    }

	}
	xml += "</MESSAGE>";
	return xml;
    }

    /**
     * Returns a byte array representation of this message.
     *
     */
    public byte[] getByteArray() throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DataOutputStream stream = new DataOutputStream(baos);
	
	// address (name)
	stream.writeBytes( name );
	alignStream( baos );

	// type tags
	stream.writeByte( ',' );  // comma indicates type tags
	Enumeration t = types.elements();
	while ( t.hasMoreElements() ) {
	    char type = ( (Character)t.nextElement() ).charValue();
	    stream.writeByte( type );
	}
	alignStream( baos );

	// values
	t = types.elements();
	Enumeration a = arguments.elements();
	while ( t.hasMoreElements() ) {
	    char type = ( (Character)t.nextElement() ).charValue();
	    switch(type) {
	    case 'i':
		stream.writeInt( ((Integer)a.nextElement()).intValue() );
		break;
	    case 'f':
		stream.writeFloat( ((Float)a.nextElement()).floatValue() );
		break;
	    case 'h':
		stream.writeLong( ((Long)a.nextElement()).longValue() );
		break;
	    case 'd':
		stream.writeDouble( ((Double)a.nextElement()).doubleValue() );
		break;
	    case 's':
		stream.writeBytes( ((String)a.nextElement()) );
		alignStream( baos );
		break;
	    }    
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
}
