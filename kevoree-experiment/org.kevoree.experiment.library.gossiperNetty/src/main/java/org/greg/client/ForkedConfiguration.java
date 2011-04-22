package org.greg.client;


public class ForkedConfiguration {

    public String server = "localhost";
    public int port = 5676;
    public int calibrationPort = 5677;
    public int flushPeriodMs = 1000;
    public String clientId = "unknown";
    public int maxBufferedRecords = 1000000;
    public boolean useCompression = true;
    public int calibrationPeriodSec = 10;

}
