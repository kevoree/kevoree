package org.kevoree.library.esper;

import java.util.Date;

class Tick {
    String symbol;
    Double price;
    Date timeStamp;

    public Tick(String s, double p, long t) {
        symbol = s;
        price = p;
        timeStamp = new Date(t);
    }
    public double getPrice() {return price;}
    public String getSymbol() {return symbol;}
    public Date getTimeStamp() {return timeStamp;}

    @Override
    public String toString() {
        return "Price: " + price.toString() + " time: " + timeStamp.toString();
    }
}