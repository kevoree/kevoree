package com.espertech.esper.core.deploy;

public class ParseNodeModule extends ParseNode
{
    private String moduleName;

    public ParseNodeModule(EPLModuleParseItem item, String moduleName)
    {
        super(item);
        this.moduleName = moduleName;
    }

    public String getModuleName()
    {
        return moduleName;
    }
}
