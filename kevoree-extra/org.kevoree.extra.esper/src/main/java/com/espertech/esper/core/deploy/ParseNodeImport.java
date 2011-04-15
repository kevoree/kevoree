package com.espertech.esper.core.deploy;

public class ParseNodeImport extends ParseNode
{
    private String imported;

    public ParseNodeImport(EPLModuleParseItem item, String imported)
    {
        super(item);
        this.imported = imported;
    }

    public String getImported()
    {
        return imported;
    }
}
