package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;

/**
 * Interface for statement result callbacks.
 */
public interface StatementResultListener {
    /**
     * Provide statement result.
     * @param newEvents insert stream
     * @param oldEvents remove stream
     * @param statementName stmt name
     * @param statement stmt
     * @param epServiceProvider engine
     */
    public void update(EventBean[] newEvents, EventBean[] oldEvents, String statementName, EPStatementSPI statement, EPServiceProviderSPI epServiceProvider);
}
