package com.espertech.esper.view.std;

import com.espertech.esper.view.View;

import java.util.List;

public class GroupByViewAgedEntry
{
    private final List<View> subviews;
    private long lastUpdateTime;

    public GroupByViewAgedEntry(List<View> subviews, long lastUpdateTime)
    {
        this.subviews = subviews;
        this.lastUpdateTime = lastUpdateTime;
    }

    public List<View> getSubviews()
    {
        return subviews;
    }

    public long getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime)
    {
        this.lastUpdateTime = lastUpdateTime;
    }
}
