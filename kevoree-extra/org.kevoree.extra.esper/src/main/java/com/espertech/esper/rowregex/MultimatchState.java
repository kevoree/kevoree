package com.espertech.esper.rowregex;

import com.espertech.esper.client.EventBean;

/**
 * State holder for matches, backed by an array, for fast copying and writing.
 */
public class MultimatchState {
    private int count;
    private EventBean[] events;

    /**
     * Ctor.
     * @param event first event to hold
     */
    public MultimatchState(EventBean event) {
        events = new EventBean[3];
        add(event);
    }

    /**
     * Ctor.
     * @param state to copy
     */
    public MultimatchState(MultimatchState state)
    {
        EventBean[] copyArray = new EventBean[state.getBuffer().length];
        System.arraycopy(state.getBuffer(), 0, copyArray, 0, state.getCount());

        count = state.getCount();
        events = copyArray;
    }

    /**
     * Add an event.
     * @param event to add
     */
    public void add(EventBean event)
    {
        if (count == events.length)
        {
            EventBean[] buf = new EventBean[events.length * 2];
            System.arraycopy(events, 0, buf, 0, events.length);
            events = buf;
        }
        events[count++] = event;
    }

    /**
     * Returns the count of events.
     * @return count
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the raw buffer.
     * @return buffer
     */
    public EventBean[] getBuffer() {
        return events;
    }

    /**
     * Determines if an event is in the collection.
     * @param event to check
     * @return indicator
     */
    public boolean containsEvent(EventBean event)
    {
        for (int i = 0; i < count; i++)
        {
            if (events[i] == event)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the buffer sized to only the contained events.
     * @return events
     */
    public EventBean[] getEventArray()
    {
        if (count == events.length)
        {
            return events;
        }
        EventBean[] array = new EventBean[count];
        System.arraycopy(events, 0, array, 0, count);
        return array;
    }
}
