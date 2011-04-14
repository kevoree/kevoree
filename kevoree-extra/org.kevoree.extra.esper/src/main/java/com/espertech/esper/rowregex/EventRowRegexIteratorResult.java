package com.espertech.esper.rowregex;

import java.util.List;

/**
 * Iteration result for row regex.
 */
public class EventRowRegexIteratorResult
{
    private List<RegexNFAStateEntry> endStates;
    private int eventSequenceNum;

    /**
     * Ctor.
     * @param endStates end states
     * @param eventSequenceNum seq num of event
     */
    public EventRowRegexIteratorResult(List<RegexNFAStateEntry> endStates, int eventSequenceNum)
    {
        this.endStates = endStates;
        this.eventSequenceNum = eventSequenceNum;
    }

    /**
     * Returns the end states
     * @return end states
     */
    public List<RegexNFAStateEntry> getEndStates()
    {
        return endStates;
    }

    /**
     * Returns the event seq num.
     * @return seq num
     */
    public int getEventSequenceNum()
    {
        return eventSequenceNum;
    }
}
