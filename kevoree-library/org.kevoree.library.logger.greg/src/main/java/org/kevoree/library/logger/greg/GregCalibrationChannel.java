/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.logger.greg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.greg.server.LittleEndianDataInputStream;
import org.greg.server.LittleEndianDataOutputStream;
import org.greg.server.PreciseClock;
import org.greg.server.PreciseDateTime;
import org.greg.server.TimeSpan;
import org.greg.server.Trace;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;

/**
 *
 * @author ffouquet
 */
@Library(name = "Greg")
@ChannelTypeFragment
@DictionaryType(
@DictionaryAttribute(name = "port"))
public class GregCalibrationChannel extends AbstractChannelFragment implements Runnable {

    private int maxCalibrationIters = 100;
    private int minCalibrationIters = 10;
    private int preCalibrationIters = 10;
    private double desiredConfidenceLevel = 0.95;
    private int desiredConfidenceRangeMs = 1;
    Thread reception = null;
    Boolean listen = true;

    @Start
    public void startChannel() {
        reception = new Thread(this);
        reception.start();
    }

    @Stop
    public void stopChannel() {
        listen = false;
    }

    @Override
    public void run() {
        ServerSocket calibrationServer = null;
        try {
            calibrationServer = new ServerSocket(Integer.parseInt(this.getDictionary().get("port").toString()));
        } catch (IOException e) {
            Trace.writeLine("Failed to create calibration listener", e);
        }

        Executor executor = Executors.newFixedThreadPool(16);

        while (listen) {
            final Socket client;
            try {
                client = calibrationServer.accept();
                client.setTcpNoDelay(true);
            } catch (Exception e) {
                Trace.writeLine("Failed to accept client for calibration", e);
                continue;
            }
            final SocketAddress ep = client.getRemoteSocketAddress();

            executor.execute(new Runnable() {

                public void run() {
                    try {
                        GregCalibrationMessage cmsg = processCalibrationExchange(client, ep);
                        //records.setAdress(client.getRemoteSocketAddress());
                        Message msg = new Message();
                        msg.setContent(cmsg);
                        Object result = remoteDispatch(msg);

                    } catch (Exception e) {
                        Trace.writeLine("Failed to process calibration exchange with " + ep, e);
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            });
        }
    }

    @Override
    public Object dispatch(Message msg) {
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private GregCalibrationMessage processCalibrationExchange(Socket client, SocketAddress ep) throws IOException {
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        // http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#On-line_algorithm

        DataOutput w = new LittleEndianDataOutputStream(out);
        DataInput r = new LittleEndianDataInputStream(in);

        UUID uuid = new UUID(r.readLong(), r.readLong());

        // We exchange *ticks* (0.1us intervals)

        // Do some iterations to warm up the TCP connection
        for (int i = 0; i < preCalibrationIters; ++i) {
            w.writeLong(PreciseClock.INSTANCE.now().toUtcNanos());
            r.readLong();
        }

        long mean = 0;
        long m2 = 0;

        try {
            for (int i = 0; i < maxCalibrationIters; ++i) {
                PreciseDateTime beforeSend = PreciseClock.INSTANCE.now();
                w.writeLong(beforeSend.toUtcNanos());
                PreciseDateTime clientTime = new PreciseDateTime(r.readLong());
                PreciseDateTime afterReceive = PreciseClock.INSTANCE.now();

                long latencyNanos = (afterReceive.toUtcNanos() - beforeSend.toUtcNanos()) / 2;
                long clockLatenessNanos = clientTime.toUtcNanos() - beforeSend.toUtcNanos() - latencyNanos;
                // clientTime   == beforeSend + clockLateness + latency
                // afterReceive == clientTime - clockLateness + latency
//                Trace.writeLine(
//                    "[" + ep + "] Iteration " + i + ": clock late by " +
//                    new TimeSpan(clockLateness) +
//                    " (beforeSend: " + beforeSend.toString() +
//                    ", clientTime: " + clientTime.toString() +
//                    ", afterReceive: " + afterReceive.toString()+ ")");

                int n = i + 1;

                long delta = clockLatenessNanos - mean;
                mean += delta / n;
                m2 += delta * (clockLatenessNanos - mean);

                if (n == 1) {
                    continue;
                }

                double s = Math.sqrt(1.0 * m2 / (n - 1));

                double td = new TDistributionImpl(n - 1).cumulativeProbability((1 + desiredConfidenceLevel) / 2);
                double confidenceRange = 2 * s / Math.sqrt(n) * td;
//                Trace.writeLine("[" + ep + "] confidence range = " + confidenceRange / 1000000 + " ms");
                if (i >= minCalibrationIters && confidenceRange < desiredConfidenceRangeMs * 1000000) {
//                    Trace.writeLine("[" + ep + "] Achieved desired confidence range");
                    break;
                }
            }
        } catch (MathException e) {
            Trace.writeLine("Math exception while calibrating with " + ep, e);
        }

        Trace.writeLine("Clock lateness with client " + ep + " (" + uuid + ") is " + new TimeSpan(mean));

        GregCalibrationMessage result = new GregCalibrationMessage();
        result.setUuid(uuid);
        result.setTimeSpan(new TimeSpan(mean));


        return result;
        // clientLateness.put(uuid, new TimeSpan(mean));
    }
}
