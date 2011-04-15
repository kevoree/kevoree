package com.espertech.esper.client.annotation;

/**
 * Annotation for providing a statement execution hint.
 * <p>
 * Hints are providing instructions that can change latency, throughput or memory requirements of a statement. 
 */
public @interface Hint {

    /**
     * Hint keyword(s), comma-separated.
     * @return keywords
     */
    String value();    
}
