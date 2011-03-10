
package org.kevoree.library.gossiper.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;

/**
 * Representation based upon a protocol buffer message. 
 * 
 * @see <a href="http://code.google.com/apis/protocolbuffers/">Protocol Buffers</a>
 * 
 * @author David Bordoley
 * @param <T>
 *            The Message class to serialize, see
 *            {@link com.google.protobuf.Message}
 */
public class MessageRepresentation<T extends MessageLite> extends
        OutputRepresentation {

    private static final MediaType mediaType = MediaType.register(
            "application/x-protobuf", "Google Protocol Buffer");

    private T message;

    /**
     * Constructor for reading the message from a serialized representation. This
     * representation must have the proper media type: "application/x-protobuf".
     * 
     * @param serializedRepresentation
     *            The serialized representation.
     * @param prototype
     *            A prototype message used by the constructor to obtain a
     *            message builder instance. Typically obtained by invoking the
     *            getDefaultInstance() static method of the message class.
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public MessageRepresentation(Representation serializedRepresentation,
            T prototype) throws IOException {
        super(mediaType);

        if (!serializedRepresentation.getMediaType().equals(mediaType)) {
            throw new IllegalArgumentException(
                    "The serialized representation must have this media type: "
                            + mediaType.toString());
        }

        InputStream is = serializedRepresentation.getStream();
        setMessage((T) prototype.newBuilderForType().mergeFrom(is).build());
        is.close();
    }

    /**
     * Constructor from a protocol buffer message.
     * 
     * @param message
     *            The protocol buffer message
     *            
     * @throws IllegalArgumentException
     */
    public MessageRepresentation(T message) {
        super(mediaType);
        this.message = message;
    }

    /**
     * Returns the represented message.
     * 
     * @return The represented message.
     */
    public T getMessage() {
        return message;
    }

    public long getSize() {
        return message.getSerializedSize();
    }

    public String getText() {
        return message.toString();
    }

    /**
     * Releases the represented message.
     */
    @Override
    public void release() {
        message = null;
        super.release();
    }

    /**
     * Sets the represented message.
     * 
     * @param message
     *            The represented message.
     * @throws IllegalArgumentException
     */
    public void setMessage(T message) {
        if (null == message) {
            throw new IllegalArgumentException(
                    "Message argument may not be null.");
        }

        this.message = message;
    }

    public void write(OutputStream os) throws IOException {
        message.writeTo(os);
    }
}