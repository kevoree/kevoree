/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.logger.greg;

import org.greg.server.*;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ffouquet
 */
@Library(name = "Greg")
@ComponentType
@Provides({
    @ProvidedPort(name = "records", type = PortType.MESSAGE),
    @ProvidedPort(name = "calibrations", type = PortType.MESSAGE)
})
public class GregServer extends AbstractComponentType implements Runnable {

    //private final Configuration conf;
    private final TimeBufferedQueue<Record> outputQueue;
    private final ConcurrentMap<UUID, Queue<Record>> clientRecords = new ConcurrentHashMap<UUID, Queue<Record>>();
    private final ConcurrentMap<UUID, TimeSpan> clientLateness = new ConcurrentHashMap<UUID, TimeSpan>();
    private final AtomicInteger numPendingUncalibratedEntries = new AtomicInteger(0);
    private int maxPendingUncalibrated = 100000;
    private int maxPendingCalibrated = 1000000;
    private int timeWindowSec = 5;

    public GregServer() {


        /*
        conf = new Configuration();
        conf.messagePort = 5676;// get(args, "port", 5676);
        conf.calibrationPort = 5677;//  get(args, "calibrationPort", 5677);
        conf.desiredConfidenceLevel = 0.95;// get(args, "confidenceLevel", 0.95);
        conf.desiredConfidenceRangeMs = 1;//get(args, "confidenceRangeMs", 1);
        conf.maxCalibrationIters = 100;// get(args, "maxCalibrationIters", 100);
        conf.minCalibrationIters = 10;// get(args, "minCalibrationIters", 10);
        conf.preCalibrationIters = 10;// get(args, "preCalibrationIters", 10);
        conf.maxPendingCalibrated = 1000000;// get(args, "maxPendingCalibrated", 1000000);
        conf.maxPendingUncalibrated = 100000;//get(args, "maxPendingUncalibrated", 100000);
        conf.timeWindowSec = 5;//get(args, "timeWindowSec", 5);
         */
        Comparator<Record> RECORD_COMPARATOR = new Comparator<Record>() {

            @Override
            public int compare(Record x, Record y) {
                return x.timestamp.compareTo(y.timestamp);
            }
        };

        this.outputQueue = new TimeBufferedQueue<Record>(new TimeSpan(timeWindowSec * 1000000000L), PreciseClock.INSTANCE, maxPendingCalibrated, RECORD_COMPARATOR);
    }

    @Start
    public void start() {

        new Thread() {

            public void run() {
                flushCalibratedMessages();
            }
        }.start();

        new Thread(this).start();

        Trace.writeLine("GregServer Started !");

    }
    private Boolean run = true;

    @Stop
    public void stop() {
        run = false;
    }

    private static Pair<PreciseDateTime, Record> absolutizeTime(Record rec, TimeSpan lateness) {
        PreciseDateTime t = new PreciseDateTime(rec.timestamp.toUtcNanos() - lateness.toNanos());
        rec.timestamp = t;
        return new Pair<PreciseDateTime, Record>(t, rec);
    }

    private void flushCalibratedMessages() {
        Thread.currentThread().setPriority(7); // above normal
        while (run) {
            List<Pair<PreciseDateTime, Record>> snapshot = new ArrayList<Pair<PreciseDateTime, Record>>(10000);
            for (Map.Entry<UUID, TimeSpan> p : clientLateness.entrySet()) {
                UUID client = p.getKey();
                TimeSpan lateness = p.getValue();

                Queue<Record> q = clientRecords.get(client);
                if (q != null) {
                    snapshot.clear();
                    for (int i = 0; i < 10000; ++i) {
                        Record r = q.poll();
                        if (r == null) {
                            break;
                        }
                        snapshot.add(absolutizeTime(r, lateness));
                    }
                    if (snapshot.size() > 0) {
                        Trace.writeLine("Dequeued snapshot: " + snapshot.size());
                    }

                    numPendingUncalibratedEntries.addAndGet(-snapshot.size());
                    outputQueue.enqueue(snapshot);
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setPriority(7); // Above normal
            OutputStream os = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 16384);
            byte[] newline = System.getProperty("line.separator").getBytes("utf-8");
            while (run) {
                List<Record> records = outputQueue.dequeue();
                if (records.isEmpty()) {
                    Thread.sleep(50);
                    continue;
                }

                for (Record rec : records) {

                    System.out.println("record "+rec.toString());

                    os.write(rec.machine.array, rec.machine.offset, rec.machine.len);
                    os.write(' ');
                    os.write(rec.clientId.array, rec.clientId.offset, rec.clientId.len);
                    os.write(' ');
                    byte[] ts = rec.timestamp.toBytes();
                    os.write(ts);
                    os.write(' ');
                    os.write(rec.message.array, rec.message.offset, rec.message.len);
                    os.write(newline);
                }
                os.flush();
            }
        } catch (Exception e) {
            Trace.writeLine("Failure while writing records", e);
        }
    }

    private interface Sink<T> {

        void consume(T t);
    }

    @Port(name = "records")
    public void processRecords(Object msg) {
        if (msg instanceof GregRecordsMessage) {
            processRecordsBatch((GregRecordsMessage) msg);
        } else {
            Trace.writeLine("Bad message format for records port");
        }
    }

    @Port(name = "calibrations")
    public void processCalibrations(Object msg) {
        if (msg instanceof GregCalibrationMessage) {
            GregCalibrationMessage gmesg = (GregCalibrationMessage) msg;
            clientLateness.put(gmesg.getUuid(), gmesg.getTimeSpan());
        }
    }

    private void processRecordsBatch(final GregRecordsMessage msg) {

        try {
            clientRecords.putIfAbsent(msg.getUuid(), new ConcurrentLinkedQueue<Record>());
            final Queue<Record> q = clientRecords.get(msg.getUuid());

            final boolean[] skipping = new boolean[]{false};
            final int[] numRead = {0};
            final int[] numSkipped = {0};

            final List<Record> uncalibrated = new ArrayList<Record>();
            final List<Pair<PreciseDateTime, Record>> calibrated = new ArrayList<Pair<PreciseDateTime, Record>>();

            final TimeSpan lateness = clientLateness.get(msg.getUuid());

            Sink<Record> sink = new Sink<Record>() {

                @Override
                public void consume(Record rec) {
                    numRead[0]++;

                    if (lateness == null) {
                        int numPending = numPendingUncalibratedEntries.incrementAndGet();
                        if (numPending < maxPendingUncalibrated) {
                            uncalibrated.add(rec);

                            if (skipping[0]) {
                                Trace.writeLine("Receiving entries from client " + msg.getAdress() + " again, after having skipped " + numSkipped[0]);
                            }
                            skipping[0] = false;
                            numSkipped[0] = 0;
                        } else {
                            numPendingUncalibratedEntries.decrementAndGet();

                            numSkipped[0]++;
                            if (!skipping[0] || numSkipped[0] % 10000 == 0) {
                                Trace.writeLine(
                                        "Uncalibrated records buffer full - skipping entry from client " + msg.getAdress()
                                        + " because there are already " + numPending + " uncalibrated entries. "
                                        + (numSkipped[0] == 1 ? "" : (numSkipped[0] + " skipped in a row...")));
                            }
                            skipping[0] = true;
                        }
                    } else {
                        calibrated.add(absolutizeTime(rec, lateness));
                    }
                }
            };

            for (Record record : msg.getRecords()) {
                sink.consume(record);
            }

            // Only publish records to main queue if we read all them successfully (had no exception to this point)
            // Otherwise we'd have duplicates if clients resubmit their records after failure.
            //System.out.println(uncalibrated.size());

            q.addAll(uncalibrated);

            //System.out.println(calibrated.size());

            outputQueue.enqueue(calibrated);

            if (skipping[0]) {
                Trace.writeLine("Skipped " + numSkipped[0] + " entries from " + msg.getAdress() + " in a row.");
            }
            Trace.writeLine("Read " + numRead[0] + " entries");
        } catch (Exception e) {// Socket or IO or whatever
            Trace.writeLine("Failed to receive records batch, ignoring", e);
            // Ignore
        }
    }
}
