package org.kevoree.library.grapher.android;


/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 02/11/11
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DynamicXYDatasource  {


    private int HISTORY_SIZE = 30;
    private  int SIZE = 30;
    private LinkedList<Number> values;
    private MyObservable notifier;

    class MyObservable extends Observable {
        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }


    public  DynamicXYDatasource(int size,int HISTORY_SIZE) {
        this.SIZE  = size;
        this.HISTORY_SIZE =   HISTORY_SIZE;
        notifier = new MyObservable();
        values = new LinkedList<Number>();
    }

    public void addItem(Number value){
        values.addLast(value);
        if (values.size() > HISTORY_SIZE) {
            values.removeFirst();
        }
        notifier.notifyObservers();
    }

    public int getItemCount() {
        return values.size();
    }

    public Number getX(int index) {
        if (index >= SIZE) {
            throw new IllegalArgumentException();
        }
        return index;
    }

    public Number getY(int index) {
        if (index >= SIZE) {
            throw new IllegalArgumentException();
        }
        return values.get(index);
    }

    public void addObserver(Observer observer) {
        notifier.addObserver(observer);
    }

    public void removeObserver(Observer observer) {
        notifier.deleteObserver(observer);
    }

}