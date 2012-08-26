package org.kevoree.library.scimark;

import jnt.scimark2.*;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/08/12
 * Time: 21:32
 */

@Requires({
        @RequiredPort(name = "result", type = PortType.MESSAGE, optional = true)
})
@Provides({
        @ProvidedPort(name = "trigger", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "large", defaultValue = "false", optional = true, vals = {"false", "true"})
})
@ComponentType
@Library(name = "JavaSE")
public class SciMarkBench extends AbstractComponentType implements Runnable {

    @Port(name = "trigger")
    public void trigger(Object msg) {
        pool.submit(this);
    }

    ExecutorService pool = null;

    int FFT_size = jnt.scimark2.Constants.FFT_SIZE;
    int SOR_size = jnt.scimark2.Constants.SOR_SIZE;
    int Sparse_size_M = jnt.scimark2.Constants.SPARSE_SIZE_M;
    int Sparse_size_nz = jnt.scimark2.Constants.SPARSE_SIZE_nz;
    int LU_size = jnt.scimark2.Constants.LU_SIZE;

    public void updateParams() {
        String ltest = getDictionary().get("large").toString();
        if (ltest.equals("large")) {
            FFT_size = jnt.scimark2.Constants.LG_FFT_SIZE;
            SOR_size = jnt.scimark2.Constants.LG_SOR_SIZE;
            Sparse_size_M = jnt.scimark2.Constants.LG_SPARSE_SIZE_M;
            Sparse_size_nz = jnt.scimark2.Constants.LG_SPARSE_SIZE_nz;
            LU_size = jnt.scimark2.Constants.LG_LU_SIZE;
        } else {
            FFT_size = jnt.scimark2.Constants.FFT_SIZE;
            SOR_size = jnt.scimark2.Constants.SOR_SIZE;
            Sparse_size_M = jnt.scimark2.Constants.SPARSE_SIZE_M;
            Sparse_size_nz = jnt.scimark2.Constants.SPARSE_SIZE_nz;
            LU_size = jnt.scimark2.Constants.LU_SIZE;
        }
    }

    @Start
    public void start() {
        updateParams();
        pool = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable,"SciMark" + getName());
                t.setPriority(Thread.MAX_PRIORITY);
                return t;
            }
        });
    }

    @Stop
    public void stop() {
        pool.shutdownNow();
        pool = null;
    }

    @Update
    public void update() {
        stop();
        start();
    }

    @Override
    public void run() {

        getPortByName("result", MessagePort.class).process("Bench started");
        StringBuffer result = new StringBuffer();

        // default to the (small) cache-contained version
        double min_time = jnt.scimark2.Constants.RESOLUTION_DEFAULT;
        // run the benchmark
        double res[] = new double[6];
        Random R = new Random(jnt.scimark2.Constants.RANDOM_SEED);

        res[1] = kernel.measureFFT(FFT_size, min_time, R);
        res[2] = kernel.measureSOR(SOR_size, min_time, R);
        res[3] = kernel.measureMonteCarlo(min_time, R);
        res[4] = kernel.measureSparseMatmult(Sparse_size_M,
                Sparse_size_nz, min_time, R);
        res[5] = kernel.measureLU(LU_size, min_time, R);


        res[0] = (res[1] + res[2] + res[3] + res[4] + res[5]) / 5;


        // print out results

        result.append("SciMark 2.0a");
        result.append("\n");

        result.append("Composite Score: " + res[0]);
        result.append("\n");
        result.append("FFT (" + FFT_size + "): ");
        if (res[1] == 0.0) {
            result.append(" ERROR, INVALID NUMERICAL RESULT!");
            result.append("\n");
        } else {
            result.append(res[1]);
            result.append("\n");
        }
        result.append("SOR (" + SOR_size + "x" + SOR_size + "): "
                + "  " + res[2]);
        result.append("\n");
        result.append("Monte Carlo : " + res[3]);
        result.append("\n");
        result.append("Sparse matmult (N=" + Sparse_size_M +
                ", nz=" + Sparse_size_nz + "): " + res[4]);
        result.append("\n");
        result.append("LU (" + LU_size + "x" + LU_size + "): ");
        if (res[5] == 0.0) {
            result.append(" ERROR, INVALID NUMERICAL RESULT!");
            result.append("\n");
        } else {
            result.append(res[5]);
            result.append("\n");
        }
        // print out System info
        result.append("\n");
        result.append("java.vendor: " +
                System.getProperty("java.vendor"));
        result.append("\n");
        result.append("java.version: " +
                System.getProperty("java.version"));
        result.append("\n");
        result.append("os.arch: " +
                System.getProperty("os.arch"));
        result.append("\n");
        result.append("os.name: " +
                System.getProperty("os.name"));
        result.append("\n");
        result.append("os.version: " +
                System.getProperty("os.version"));
        result.append("\n");

        getPortByName("result", MessagePort.class).process(result.toString());

    }
}
