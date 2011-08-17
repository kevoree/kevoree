//package org.kevoree.library.arduinoNodeType.utils;
//
///**
// * User: ffouquet
// * Date: 03/06/11
// * Time: 22:02
// */
//
//import java.io.InputStream;
//
///**
// *
// * @author ffouquet
// */
//public class SerialPrinter implements Runnable {
//
//    public boolean running = true;
//    private InputStream in;
//
//    public SerialPrinter(InputStream _in){
//        in = _in;
//    }
//
//    @Override
//    public void run() {
//        while(running){
//            try {
//                if(in.available() > 0){
//                    System.out.print((char)in.read());
//                    Thread.sleep(10);
//                } else {
//                    Thread.sleep(200);
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//}
