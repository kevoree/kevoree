package org.kevoree.library.logger.greg;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPInputStream;
import org.greg.client.LittleEndianDataInputStream;
import org.greg.server.ByteSlice;
import org.greg.server.Trace;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePlatformHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

@Library(name = "Greg")
@ChannelTypeFragment
@DictionaryType(
@DictionaryAttribute(name = "port"))
public class GregRecordChannel extends AbstractChannelFragment implements Runnable {

    Thread reception = null;
    Boolean listen = true;
    private KevoreeModelHandlerService modelHandlerService = null;
    private Bundle bundle = null;
    private ServiceReference sr = null;
    private String hostname="";

    @Start
    public void startChannel() {
        reception = new Thread(this);
        reception.start();
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new AssertionError("Can't get localhost?");
        }

    }

    @Stop
    public void stopChannel() {
        listen = false;
    }

    @Override
    public Object dispatch(Message msg) {
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            //MANDATORY STOP CONDITION TO AVOID LOOP WITH REOTE PEER !
            if (!msg.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {

            @Override
            public Object sendMessageToRemote(Message message) {
                Socket client = null;
                try {
                    message.getPassedNodes().add(modelHandlerService.getNodeName());
                    client = new Socket(getIP(), getPORT());

                    Byte[] tab = (Byte[]) message.getContent();





                } catch (Exception e) {
                    System.err.println("Fail to send to remote channel ");
                    System.err.println("Reply not implemented => message lost !!!");
                } finally {
                    try {
                        client.close();
                    } catch (IOException ex) {
                        Logger.getLogger(GregRecordChannel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return null;
            }

            public String getIP() {
                String ip = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
                if (ip == null) {
                    ip = "127.0.0.1";
                }
                return ip;
            }

            public Integer getPORT() {
                String port = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
                if (port == null) {
                    port = "8000";
                }
                return Integer.parseInt(port);
            }
        };
    }

    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(Integer.parseInt(this.getDictionary().get("port").toString()));
        } catch (IOException e) {
            Trace.writeLine("Cannot create messages acceptor", e);
        }

        int maxConcurrentClients = 16;
        final Semaphore sem = new Semaphore(maxConcurrentClients);
        Executor pool = Executors.newFixedThreadPool(16);
        while (listen) {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                continue;
            }

            final Socket client;
            final InputStream stream;
            try {
                client = server.accept();
                stream = client.getInputStream();
            } catch (Exception e) {
                Trace.writeLine("Failed to accept client or get its input stream", e);
                continue;
            }

            pool.execute(new Runnable() {

                @Override
                public void run() {
                    try {

                        GregRecordsMessage records = processRecordsBatch(stream);
                        records.setAdress(client.getRemoteSocketAddress());
                        Message msg = new Message();
                        msg.setContent(records);
                        Object result = remoteDispatch(msg);

                    } finally {
                        sem.release();
                        try {
                            client.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
            });
        }

    }

    private GregRecordsMessage processRecordsBatch(InputStream rawStream) {
        try {
            InputStream stream = new BufferedInputStream(rawStream, 65536);
            DataInput r = new LittleEndianDataInputStream(stream);
            UUID uuid = new UUID(r.readLong(), r.readLong());
            boolean useCompression = r.readBoolean();

            GregRecordsMessage msg = readRecords(useCompression ? new GZIPInputStream(stream) : stream);
            msg.setUuid(uuid);

            return msg;

        } catch (Exception e) {// Socket or IO or whatever
            Trace.writeLine("Failed to receive records batch, ignoring", e);
            // Ignore
        }
        return null;
    }


    /* UNSERIALIZE RECORDS */
    private static GregRecordsMessage readRecords(InputStream stream) throws IOException {

        DataInput r = new LittleEndianDataInputStream(new BufferedInputStream(stream, 65536));

        int cidLenBytes = r.readInt();
        byte[] cidBytes = new byte[cidLenBytes];
        r.readFully(cidBytes);

        ByteSlice clientId = new ByteSlice(cidBytes, 0, cidBytes.length);

        List<Record> records = new ArrayList<Record>();

        while (0 != r.readInt()) {
            PreciseDateTime timestamp = new PreciseDateTime(r.readLong());

            int machineLenBytes = r.readInt();
            byte[] machineBytes = new byte[machineLenBytes];
            r.readFully(machineBytes);
            int msgLenBytes = r.readInt();
            byte[] msgBytes = new byte[msgLenBytes];
            r.readFully(msgBytes);

            Record rec = new Record();
            rec.machine = new ByteSlice(machineBytes, 0, machineLenBytes);
            rec.timestamp = timestamp;
            rec.message = new ByteSlice(msgBytes, 0, msgBytes.length);
            rec.serverTimestamp = PreciseClock.INSTANCE.now();
            rec.clientId = clientId;
            //sink.consume(rec);
            records.add(rec);
        }
        GregRecordsMessage result = new GregRecordsMessage();
        result.setRecords(records);
        return result;
    }
    String clientId = "default";

    //SERIALIZE
    private void writeRecords(OutputStream stream, GregRecordsMessage gmsg) throws IOException {
        int maxBatchSize = 10000;
        DataOutput w = new LittleEndianDataOutputStream(stream);
        byte[] cidBytes = clientId.getBytes("utf-8");
        w.writeInt(cidBytes.length);
        w.write(cidBytes);
        int recordsWritten = 0;
        byte[] machineBytes = hostname.getBytes("utf-8");
        CharsetEncoder enc = Charset.forName("utf-8").newEncoder();
        ByteBuffer maxMsg = ByteBuffer.allocate(1);
        for (Record rec : gmsg.getRecords()) {
            w.writeInt(1);
            w.writeLong(rec.timestamp.toUtcNanos());
            w.writeInt(machineBytes.length);
            w.write(machineBytes);

            int maxLen = rec.message.length() * 2;
            if (maxLen > maxMsg.limit()) {
                maxMsg = ByteBuffer.allocate(maxLen);
            }
            enc.reset();
            enc.encode(CharBuffer.wrap(rec.message), maxMsg, true);
            maxMsg.position(0);

            w.writeInt(maxMsg.limit());
            w.write(maxMsg.array(), maxMsg.arrayOffset(), maxMsg.limit());

            if (++recordsWritten == maxBatchSize) {
                break;
            }
        }
        w.writeInt(0);

        // Only remove records once we're sure that they have been written to server (no exception happened to this point)
        stream.flush();
        for (int i = 0; i < recordsWritten; ++i) {
            records.remove();
        }
        numRecords.addAndGet(-recordsWritten);

        Trace.writeLine("Written batch of " + recordsWritten + " records to greg");

        return recordsWritten < maxBatchSize;
    }
}
