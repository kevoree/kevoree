package org.greg.server;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistributionImpl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class GregServer {


    private void acceptMessages() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(conf.messagePort);
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






    private void acceptCalibration() {
        ServerSocket calibrationServer = null;
        try {
            calibrationServer = new ServerSocket(conf.calibrationPort);
        } catch (IOException e) {
            Trace.writeLine("Failed to create calibration listener", e);
        }

        Executor executor = Executors.newFixedThreadPool(16);

        while (true) {
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
                        processCalibrationExchange(client, ep);
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

    private void processCalibrationExchange(Socket client, SocketAddress ep) throws IOException {
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        // http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#On-line_algorithm

        DataOutput w = new LittleEndianDataOutputStream(out);
        DataInput r = new LittleEndianDataInputStream(in);

        UUID uuid = new UUID(r.readLong(), r.readLong());

        // We exchange *ticks* (0.1us intervals)

        // Do some iterations to warm up the TCP connection
        for (int i = 0; i < conf.preCalibrationIters; ++i) {
            w.writeLong(PreciseClock.INSTANCE.now().toUtcNanos());
            r.readLong();
        }

        long mean = 0;
        long m2 = 0;

        try {
            for (int i = 0; i < conf.maxCalibrationIters; ++i) {
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

                if (n == 1) continue;

                double s = Math.sqrt(1.0 * m2 / (n - 1));

                double td = new TDistributionImpl(n - 1).cumulativeProbability((1 + conf.desiredConfidenceLevel) / 2);
                double confidenceRange = 2 * s / Math.sqrt(n) * td;
//                Trace.writeLine("[" + ep + "] confidence range = " + confidenceRange / 1000000 + " ms");
                if (i >= conf.minCalibrationIters && confidenceRange < conf.desiredConfidenceRangeMs * 1000000) {
//                    Trace.writeLine("[" + ep + "] Achieved desired confidence range");
                    break;
                }
            }
        } catch (MathException e) {
            Trace.writeLine("Math exception while calibrating with " + ep, e);
        }

        Trace.writeLine("Clock lateness with client " + ep + " (" + uuid + ") is " + new TimeSpan(mean));
        clientLateness.put(uuid, new TimeSpan(mean));
    }





}
