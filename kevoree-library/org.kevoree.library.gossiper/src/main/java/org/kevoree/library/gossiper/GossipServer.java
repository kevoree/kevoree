package org.kevoree.library.gossiper;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class GossipServer {

    private Integer _port;

    public GossipServer(Integer port) {
        _port = port;
    }

    public void start() throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Selector selector = Selector.open();

    }


}
