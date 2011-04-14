package com.espertech.esper.epl.spec;

/**
 * Skip-enum for match_recognize.
 */
public enum MatchRecognizeSkipEnum {
    /**
     * Skip to current row.
     */
    TO_CURRENT_ROW,

    /**
     * Skip to next row.
     */
    TO_NEXT_ROW,
    
    /**
     * Skip past last row.
     */
    PAST_LAST_ROW
}