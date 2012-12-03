package jexxus.common;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/12/12
 * Time: 13:13
 */
public class FakeConnection extends Connection {

    public FakeConnection(String ip) {
        super(null, ip);
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void send(byte[] data, Delivery deliveryType) { }

    @Override
    protected InputStream getTCPInputStream() {return null;}

    @Override
    protected OutputStream getTCPOutputStream() {return null;}

    @Override
    public void close() {}
}
