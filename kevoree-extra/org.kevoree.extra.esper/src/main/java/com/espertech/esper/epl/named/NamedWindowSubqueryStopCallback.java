package com.espertech.esper.epl.named;

import com.espertech.esper.epl.lookup.TableLookupStrategy;
import com.espertech.esper.util.StopCallback;

public class NamedWindowSubqueryStopCallback implements StopCallback {

    private final NamedWindowProcessor processor;
    private final TableLookupStrategy namedWindowSubqueryLookup;

    public NamedWindowSubqueryStopCallback(NamedWindowProcessor processor, TableLookupStrategy namedWindowSubqueryLookup) {
        this.processor = processor;
        this.namedWindowSubqueryLookup = namedWindowSubqueryLookup;
    }

    @Override
    public void stop() {
        processor.getRootView().removeSubqueryLookupStrategy(namedWindowSubqueryLookup);
    }
}
