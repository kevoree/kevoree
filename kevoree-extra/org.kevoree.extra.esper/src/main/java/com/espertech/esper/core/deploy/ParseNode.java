package com.espertech.esper.core.deploy;

public abstract class ParseNode
{
    private EPLModuleParseItem item;

    protected ParseNode(EPLModuleParseItem item)
    {
        this.item = item;
    }

    public EPLModuleParseItem getItem()
    {
        return item;
    }
}
