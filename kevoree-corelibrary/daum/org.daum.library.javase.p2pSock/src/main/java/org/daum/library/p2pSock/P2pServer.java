package org.daum.library.p2pSock;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 08/08/12
 * Time: 14:42
 * To change this template use File | Settings | File Templates.
 */

import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class P2pServer  implements Runnable{

    protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private P2pSock p2pSock;
    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    public P2pServer(P2pSock p2pSock,int port){
        this.serverPort = port;
        this.p2pSock = p2pSock;
    }

    public void run()
    {
        synchronized(this)
        {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try
            {
                clientSocket = this.serverSocket.accept();

                this.threadPool.execute( new WorkerRunnable(p2pSock,clientSocket,"Thread Pooled Server"));

            } catch (IOException e) {
                if(isStopped())
                {
                    logger.debug("Server Stopped ",e);
                    return;
                }
                logger.debug("Error accepting client connection ",e);

                if(clientSocket != null)
                    try
                    {
                        clientSocket.close();
                    } catch (IOException e1) {

                    }
            }

        }
        this.threadPool.shutdown();
        logger.debug("Server Stopped ");
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop()
    {
        this.isStopped = true;
        try {
            if(serverSocket != null)
                this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket()
    {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);

        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }
}