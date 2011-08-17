//package org.kevoree.library.arduinoNodeType.utils;
//
//import gnu.io.CommPort;
//import gnu.io.CommPortIdentifier;
//import gnu.io.SerialPort;
//import gnu.io.SerialPortEvent;
//import gnu.io.SerialPortEventListener;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * User: ffouquet
// * Date: 03/06/11
// * Time: 22:03
// */
//public class SerialTwoWayPort {
//
//    public SerialTwoWayPort(String portName) {
//        try {
//            connect(portName);
//
//        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
//
//
//
//    public void closeAll() {
//        sender.running = false;
//        printer.running = false;
//        try {
//            serialPort.getInputStream().close();
//            serialPort.getOutputStream().close();
//            serialPort.close();
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
//
//    SerialPort serialPort = null;
//    SerialPrinter printer = null;
//    SerialSender sender = null;
//
//    void connect(String portName) throws Exception {
//        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
//        if (portIdentifier.isCurrentlyOwned()) {
//            System.out.println("Error: Port is currently in use");
//        } else {
//            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
//            if (commPort instanceof SerialPort) {
//                serialPort = (SerialPort) commPort;
//                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
//                serialPort.disableReceiveTimeout();
//                serialPort.enableReceiveThreshold(1);
//                serialPort.addEventListener(new SerialPortEventListener() {
//                    @Override
//                    public void serialEvent(SerialPortEvent spe) {
//                        //System.out.println(spe);
//                    }
//                });
//                serialPort.notifyOnDataAvailable(true);
//                InputStream in = serialPort.getInputStream();
//                OutputStream out = serialPort.getOutputStream();
//                printer = new SerialPrinter(in);
//                sender = new SerialSender(out);
//                new Thread(printer).start();
//                new Thread(sender).start();
//            } else {
//                System.out.println("Error: Only serial ports are handled by this example.");
//            }
//        }
//    }
//
//}
//
