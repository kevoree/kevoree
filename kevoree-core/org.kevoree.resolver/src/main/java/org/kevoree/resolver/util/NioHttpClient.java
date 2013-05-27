package org.kevoree.resolver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/05/13
 * Time: 23:42
 */


/*
public class NioHttpClient {

    public String readHttpContent(URL url) throws IOException {
        String host = url.getHost();
        //int port = url.getPort();
        String file = url.getPath();
        SocketAddress remote = new InetSocketAddress(host, 80);
        SocketChannel channel = SocketChannel.open(remote);
        String request = "GET " + file + " HTTP/1.1\r\n" + "User-Agent: KevoreeResolver\r\n"
                + "Accept: text/*\r\n" + "Connection: close\r\n" + "Host: " + host + "\r\n" + "\r\n";
        ByteBuffer header = ByteBuffer.wrap(request.getBytes("US-ASCII"));
        channel.write(header);
        StringBuilder builder = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        while (channel.read(buffer) != -1) {
            buffer.flip();
            builder.append(buffer.array());
            buffer.clear();
        }
        channel.close();
        return builder.toString();
    }


    public String ioVersion(URL url) throws IOException {
        URLConnection c = url.openConnection();
        //c.setConnectTimeout(500);
        InputStream in = c.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String line = reader.readLine();
        builder.append(line);
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        in.close();
        return builder.toString();
    }

    public static void main(String[] args) throws IOException {
        NioHttpClient client = new NioHttpClient();
        long begin = System.currentTimeMillis();

        String result = client.ioVersion(new URL("http://maven.kevoree.org/snapshots/org/kevoree/org.kevoree.model/maven-metadata.xml"));
        //String result = client.readHttpContent(new URL("http://maven.kevoree.org/snapshots/org/kevoree/org.kevoree.model/maven-metadata.xml"));

        System.out.println(System.currentTimeMillis() - begin);


        System.out.println(result);

    }


}  */
