package com.espertech.esper.event;

/**
 * Marker interface for event types that need not transpose their property.
 * <p>
 * Transpose is the process of taking a fragment event property and adding the fragment to the
 * resulting type rather then the underlying property object.
 */
public interface NativeEventType
{
}
