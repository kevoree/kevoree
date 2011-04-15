package com.espertech.esper.core.thread;

/**
 * Ctor
 */
public class ThreadingOption
{
    /**
     * Public access.
     */
    public static boolean isThreadingEnabled = false;

    /**
     * Sets the thread option on.
     * @param threadingEnabled option on
     */
    public static void setThreadingEnabled(Boolean threadingEnabled)
    {
        isThreadingEnabled = threadingEnabled;
    }

    /**
     * Returns true when threading is enabled
     * @return indicator
     */
    public static boolean isThreadingEnabled()
    {
        return isThreadingEnabled;
    }
}
