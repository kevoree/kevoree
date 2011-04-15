package com.espertech.esper.epl.agg;

/**
 * Wrapper for an aggregation spec consisting of a stream number.
 */
public class AggregationSpec
{
    private int streamNum;

    /**
     * Ctor.
     * @param streamNum stream number
     */
    public AggregationSpec(int streamNum)
    {
        this.streamNum = streamNum;
    }

    /**
     * Returns stream number.
     * @return stream number
     */
    public int getStreamNum()
    {
        return streamNum;
    }

    /**
     * Sets the stream number
     * @param streamNum to set
     */
    public void setStreamNum(int streamNum)
    {
        this.streamNum = streamNum;
    }
}
