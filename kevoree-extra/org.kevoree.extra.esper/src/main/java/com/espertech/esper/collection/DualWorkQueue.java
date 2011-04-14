package com.espertech.esper.collection;

import java.util.ArrayDeque;

public class DualWorkQueue<Object> {

    private ArrayDeque<Object> frontQueue;
    private ArrayDeque<Object> backQueue;

    public DualWorkQueue() {
        frontQueue = new ArrayDeque<Object>();
        backQueue = new ArrayDeque<Object>();
    }

    public ArrayDeque<Object> getFrontQueue() {
        return frontQueue;
    }

    public ArrayDeque<Object> getBackQueue() {
        return backQueue;
    }
}
