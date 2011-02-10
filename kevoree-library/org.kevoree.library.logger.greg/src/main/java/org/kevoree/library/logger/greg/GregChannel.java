package org.kevoree.library.logger.greg;


import org.greg.server.*;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Library(name = "Greg")
@ChannelTypeFragment
@DictionaryType(
        @DictionaryAttribute(name = "port")
)
public class GregChannel extends AbstractChannelFragment implements Runnable {

    Thread reception = null;

    @Start
    public void startChannel() {

        reception = new Thread(this);
        reception.start();


        int maxConcurrentClients = 16;
        final Semaphore sem = new Semaphore(maxConcurrentClients);

        Executor pool = Executors.newFixedThreadPool(16);

        while (true) {
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
                public void run() {
                    try {
                        processRecordsBatch(stream, client.getRemoteSocketAddress());
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


    @Stop
    public void stopChannel() {

    }


    @Override
    public Object dispatch(Message msg) {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        while (true) {
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
                public void run() {
                    try {
                        processRecordsBatch(stream, client.getRemoteSocketAddress());
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


    /* UNSERIALIZE RECORDS */
    private static List<Record> readRecords(InputStream stream) throws IOException {
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
        return records;
    }




}
