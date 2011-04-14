package com.espertech.esper.epl.core;

import com.espertech.esper.epl.view.OutputCallback;
import com.espertech.esper.epl.view.OutputCondition;

public class PerGroupOutputState implements OutputCallback
{
    private boolean hold;
    private OutputCondition outputCondition;

    public boolean isHold()
    {
        return hold;
    }

    public void setHold(boolean hold)
    {
        this.hold = hold;
    }

    public OutputCondition getOutputCondition()
    {
        return outputCondition;
    }

    public void setOutputCondition(OutputCondition outputCondition)
    {
        this.outputCondition = outputCondition;
    }

    public void continueOutputProcessing(boolean doOutput, boolean forceUpdate)
    {
        hold = false;
    }
}
