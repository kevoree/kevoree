package com.espertech.esper.core.deploy;

public class ParseNodeUses extends ParseNode
{
    private String uses;

    public ParseNodeUses(EPLModuleParseItem item, String uses)
    {
        super(item);
        this.uses = uses;
    }

    public String getUses()
    {
        return uses;
    }
}
