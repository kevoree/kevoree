package org.daum.library.p2pSock;

import org.kevoree.framework.AbstractChannelFragment;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 09/08/12
 * Time: 09:32
 * To change this template use File | Settings | File Templates.
 */
public class ObjectInputStreamImpl extends ObjectInputStream {
    private Logger logger = LoggerFactory.getLogger(ObjectInputStreamImpl.class);
    private ChannelClassResolver resolver;

    public ObjectInputStreamImpl (InputStream in, AbstractChannelFragment channelFragment) throws IOException {
        super(in);
        resolver = new ChannelClassResolver(channelFragment);
    }

    @Override
    protected Class<?> resolveClass (ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
        Class c = null;
        try {
            c = resolver.resolve(objectStreamClass.getName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (c == null) {
                c = super.resolveClass(objectStreamClass);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (c == null) {
                c = Class.forName(objectStreamClass.getName());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return c;
    }
}