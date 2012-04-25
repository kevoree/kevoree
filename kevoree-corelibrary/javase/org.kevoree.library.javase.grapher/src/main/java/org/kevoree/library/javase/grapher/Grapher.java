package org.kevoree.library.javase.grapher;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * User: ffouquet
 * Date: 13/06/11
 * Time: 18:00
 */

@Library(name="JavaSE")
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@ComponentType
public class Grapher extends AbstractComponentType {

    Chart2D chart = null;
    HashMap<String, ITrace2D> traces = new HashMap<String, ITrace2D>();
    JFrame frame = null;

	private static final Logger logger = LoggerFactory.getLogger(Grapher.class);

    @Start
    public void startGraph() {
        chart = new Chart2D();
        chart.setBackground(Color.DARK_GRAY);
        IAxis axisX = chart.getAxisX();
        axisX.setStartMajorTick(false);
        axisX.setMajorTickSpacing(10);
        frame = new JFrame("Kevoree Grapher " + this.getName() + "@" + this.getNodeName());
        frame.getContentPane().add(chart);
        frame.setSize(800, 600);
        frame.setVisible(true);

    }

    private ITrace2D getTraceByName(String traceName) {
        if (traces.containsKey(traceName)) {
            return traces.get(traceName);
        } else {
            ITrace2D trace = new Trace2DLtd(100);
            trace.setColor(Color.ORANGE);
            chart.addTrace(trace);
            traces.put(traceName, trace);
            return trace;
        }
    }

    @Stop
    public void stopGraph() {
        chart.removeAllTraces();
        traces.clear();
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

    @Port(name = "input")
    public void appendIncoming(Object msg) {

      //  System.out.println(msg);

        try {
            String[] values = msg.toString().split(",");
            //System.out.println(values.length);;
            for (int i = 0; i < values.length; i++) {
                String[] lvl = values[i].split("=");
                //System.out.println(values[i]);
                //System.out.println(lvl.length);
                if (lvl.length >= 2) {
                    Double value = Double.parseDouble(lvl[1]);
                    Long time = System.currentTimeMillis();

                    //System.out.println(lvl[0]);
                   // System.out.println(value);
                   // System.out.println(time);

                    getTraceByName(lvl[0]).addPoint(time.doubleValue(),value);
                }
            }
        } catch (Exception e) {
            logger.warn("Grapher bad message => " + e.getMessage());
        }

    }
}
