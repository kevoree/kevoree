package org.kevoree.library.socketChannel;

import org.kevoree.framework.AbstractChannelFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 11/05/12
 * Time: 17:44
 *
 * @author Erwan Daubert
 * @version 1.0
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
