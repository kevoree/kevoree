/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.variable;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.VariableValueException;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.core.StatementExtensionSvcContext;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Variables service for reading and writing variables, and for setting a version number for the current thread to
 * consider variables for.
 * <p>
 * Consider a statement as follows: select * from MyEvent as A where A.val > var1 and A.val2 > var1 and A.val3 > var2
 * <p>
 * Upon statement execution we need to guarantee that the same atomic value for all variables is applied for all
 * variable reads (by expressions typically) within the statement.
 * <p>
 * Designed to support:
 * <ol>
 * <li>lock-less read of the current and prior version, locked reads for older versions
 * <li>atomicity by keeping multiple versions for each variable and a threadlocal that receives the current version each call
 * <li>one write lock for all variables (required to coordinate with single global version number),
 *   however writes are very fast (entry to collection plus increment an int) and therefore blocking should not be an issue
 * </ol>
 * <p>
 * As an alternative to a version-based design, a read-lock for the variable space could also be used, with the following
 * disadvantages: The write lock may just not be granted unless fair locks are used which are more expensive; And
 * a read-lock is more expensive to acquire for multiple CPUs; A thread-local is still need to deal with
 * "set var1=3, var2=var1+1" assignments where the new uncommitted value must be visible in the local evaluation.
 * <p>
 * Every new write to a variable creates a new version. Thus when reading variables, readers can ignore newer versions
 * and a read lock is not required in most circumstances.
 * <p>
 * This algorithm works as follows:
 * <p>
 * A thread processing an event into the engine via sendEvent() calls the "setLocalVersion" method once
 * before processing a statement that has variables.
 * This places into a threadlocal variable the current version number, say version 570.
 * <p>
 * A statement that reads a variable has an {@link com.espertech.esper.epl.expression.ExprVariableNode} that has a {@link com.espertech.esper.epl.variable.VariableReader} handle
 * obtained during validation (example).
 * <p>
 * The {@link com.espertech.esper.epl.variable.VariableReader} takes the version from the threadlocal (570) and compares the version number with the
 * version numbers held for the variable.
 * If the current version is same or lower (520, as old or older) then the threadlocal version,
 * then use the current value.
 * If the current version is higher (571, newer) then the threadlocal version, then go to the prior value.
 * Use the prior value until a version is found that as old or older then the threadlocal version.
 * <p>
 * If no version can be found that is old enough, output a warning and return the newest version.
 * This should not happen, unless a thread is executing for very long within a single statement such that
 * lifetime-old-version time speriod passed before the thread asks for variable values.
 * <p>
 * As version numbers are counted up they may reach a boundary. Any write transaction after the boundary
 * is reached performs a roll-over. In a roll-over, all variables version lists are
 * newly created and any existing threads that read versions go against a (old) high-collection,
 * while new threads reading the reset version go against a new low-collection.
 * <p>
 * The class also allows an optional state handler to be plugged in to handle persistence for variable state.
 * The state handler gets invoked when a variable changes value, and when a variable gets created
 * to obtain the current value from persistence, if any.
 */
public class VariableServiceImpl implements VariableService
{
    private static Log log = LogFactory.getLog(VariableServiceImpl.class);

    /**
     * Sets the boundary above which a reader considers the high-version list of variable values.
     * For use in roll-over when the current version number overflows the ROLLOVER_WRITER_BOUNDARY.
     */
    protected final static int ROLLOVER_READER_BOUNDARY = Integer.MAX_VALUE - 100000;

    /**
     * Sets the boundary above which a write transaction rolls over all variable's
     * version lists.
     */
    protected final static int ROLLOVER_WRITER_BOUNDARY = ROLLOVER_READER_BOUNDARY + 10000;

    /**
     * Applicable for each variable if more then the number of versions accumulated, check
     * timestamps to determine if a version can be expired.
     */
    protected final static int HIGH_WATERMARK_VERSIONS = 50;

    // Keep the variable list
    private final Map<String, VariableReader> variables;

    // Each variable has an index number, a current version and a list of values
    private final ArrayList<VersionedValueList<Object>> variableVersions;

    // Each variable may have a single callback to invoke when the variable changes
    private final ArrayList<Set<VariableChangeCallback>> changeCallbacks;

    // Write lock taken on write of any variable; and on read of older versions
    private final ReadWriteLock readWriteLock;

    // Thread-local for the visible version per thread
    private final VariableVersionThreadLocal versionThreadLocal = new VariableVersionThreadLocal();

    // Number of milliseconds that old versions of a variable are allowed to live
    private final long millisecondLifetimeOldVersions;
    private final TimeProvider timeProvider;
    private final EventAdapterService eventAdapterService;
    private final VariableStateHandler optionalStateHandler;

    private volatile int currentVersionNumber;
    private int currentVariableNumber;

    /**
     * Ctor.
     * @param millisecondLifetimeOldVersions number of milliseconds a version may hang around before expiry
     * @param timeProvider provides the current time
     * @param optionalStateHandler a optional plug-in that may store variable state and retrieve state upon creation
     * @param eventAdapterService event adapters
     */
    public VariableServiceImpl(long millisecondLifetimeOldVersions, TimeProvider timeProvider, EventAdapterService eventAdapterService, VariableStateHandler optionalStateHandler)
    {
        this(0, millisecondLifetimeOldVersions, timeProvider, eventAdapterService, optionalStateHandler);
    }

    /**
     * Ctor.
     * @param startVersion the first version number to start from
     * @param millisecondLifetimeOldVersions number of milliseconds a version may hang around before expiry
     * @param timeProvider provides the current time
     * @param optionalStateHandler a optional plug-in that may store variable state and retrieve state upon creation
     * @param eventAdapterService for finding event types
     */
    protected VariableServiceImpl(int startVersion, long millisecondLifetimeOldVersions, TimeProvider timeProvider, EventAdapterService eventAdapterService, VariableStateHandler optionalStateHandler)
    {
        this.millisecondLifetimeOldVersions = millisecondLifetimeOldVersions;
        this.timeProvider = timeProvider;
        this.eventAdapterService = eventAdapterService;
        this.optionalStateHandler = optionalStateHandler;
        this.variables = new HashMap<String, VariableReader>();
        this.variableVersions = new ArrayList<VersionedValueList<Object>>();
        this.readWriteLock = new ReentrantReadWriteLock();
        this.changeCallbacks = new ArrayList<Set<VariableChangeCallback>>();
        currentVersionNumber = startVersion;
    }

    public synchronized void removeVariable(String name) {
        VariableReader reader = variables.get(name);
        if (reader == null)
        {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Removing variable '" + name + "'");
        }
        variables.remove(name);

        int number = reader.getVariableNumber();
        variableVersions.set(number, null);
        changeCallbacks.set(number, null);
    }

    public void setLocalVersion()
    {
        versionThreadLocal.getCurrentThread().setVersion(currentVersionNumber);
    }

    public void registerCallback(int variableNumber, VariableChangeCallback variableChangeCallback)
    {
        Set<VariableChangeCallback> callbacks = changeCallbacks.get(variableNumber);
        if (callbacks == null)
        {
            callbacks = new CopyOnWriteArraySet<VariableChangeCallback>();
            changeCallbacks.set(variableNumber, callbacks);
        }
        callbacks.add(variableChangeCallback);
    }

    public void unregisterCallback(int variableNumber, VariableChangeCallback variableChangeCallback)
    {
        Set<VariableChangeCallback> callbacks = changeCallbacks.get(variableNumber);
        if (callbacks != null)
        {
            callbacks.remove(variableChangeCallback);
        }
    }

    public void createNewVariable(String variableName, String variableType, Object value, StatementExtensionSvcContext extensionServicesContext) throws VariableExistsException, VariableTypeException
    {
        // Determime the variable type
        Class type = JavaClassHelper.getClassForSimpleName(variableType);
        EventType eventType = null;
        if (type == null) {
            if (variableType.toLowerCase().equals("object")) {
                type = Object.class;
            }
            if (type == null) {
                eventType = eventAdapterService.getExistsTypeByName(variableType);
                if (eventType != null) {
                    type = eventType.getUnderlyingType();
                }
            }
            if (type == null) {
                throw new VariableTypeException("Cannot create variable '" + variableName + "', type '" +
                    variableType + "' is not a recognized type");
            }
        }

        if ((eventType == null) && (!JavaClassHelper.isJavaBuiltinDataType(type)) && (type != Object.class)) {
            eventType = eventAdapterService.addBeanType(type.getName(), type, false, false, false);
        }

        createNewVariable(variableName, type, eventType, value, extensionServicesContext);
    }

    private synchronized void createNewVariable(String variableName, Class type, EventType eventType, Object value, StatementExtensionSvcContext extensionServicesContext)
            throws VariableExistsException, VariableTypeException
    {
        // check coercion
        Object coercedValue = value;

        // check type
        Class variableType = JavaClassHelper.getBoxedType(type);

        if (eventType != null) {
            if ((value != null) && (!JavaClassHelper.isSubclassOrImplementsInterface(value.getClass(), eventType.getUnderlyingType()))) {
                throw new VariableTypeException("Variable '" + variableName
                    + "' of declared event type '" + eventType.getName() + "' underlying type '" + eventType.getUnderlyingType().getName() +
                        "' cannot be assigned a value of type '" + value.getClass().getName() + "'");
            }
            coercedValue = eventAdapterService.adapterForType(value, eventType);
        }
        else if (type == java.lang.Object.class) {
            // no validation
        }
        else {

            // allow string assignments to non-string variables
            if ((coercedValue != null) && (coercedValue instanceof String))
            {
                try
                {
                    coercedValue = JavaClassHelper.parse(type, (String) coercedValue);
                }
                catch (Exception ex)
                {
                    throw new VariableTypeException("Variable '" + variableName
                        + "' of declared type '" + variableType.getName() +
                            "' cannot be initialized by value '" + coercedValue + "': " + ex.toString());
                }
            }

            if ((coercedValue != null) &&
                (variableType != coercedValue.getClass()))
            {
                // if the declared type is not numeric or the init value is not numeric, fail
                if ((!JavaClassHelper.isNumeric(variableType)) ||
                    (!(coercedValue instanceof Number)))
                {
                    throw new VariableTypeException("Variable '" + variableName
                        + "' of declared type '" + variableType.getName() +
                            "' cannot be initialized by a value of type '" + coercedValue.getClass().getName() + "'");
                }

                if (!(JavaClassHelper.canCoerce(coercedValue.getClass(), variableType)))
                {
                    throw new VariableTypeException("Variable '" + variableName
                        + "' of declared type '" + variableType.getName() +
                            "' cannot be initialized by a value of type '" + coercedValue.getClass().getName() + "'");
                }

                // coerce
                coercedValue = JavaClassHelper.coerceBoxed((Number)coercedValue, variableType);
            }
        }

        // check if it exists
        VariableReader reader = variables.get(variableName);
        if (reader != null)
        {
            throw new VariableExistsException("Variable by name '" + variableName + "' has already been created");
        }

        long timestamp = timeProvider.getTime();

        // Check current state - see if the variable exists in the state handler
        if (optionalStateHandler != null)
        {
            Pair<Boolean, Object> priorValue = optionalStateHandler.getHasState(variableName, currentVariableNumber, variableType, eventType, extensionServicesContext);
            if (priorValue.getFirst())
            {
                coercedValue = priorValue.getSecond();
            }
        }

        // create new holder for versions
        VersionedValueList<Object> valuePerVersion = new VersionedValueList<Object>(variableName, currentVersionNumber, coercedValue, timestamp, millisecondLifetimeOldVersions, readWriteLock.readLock(), HIGH_WATERMARK_VERSIONS, false);

        // find empty spot
        int emptySpot = -1;
        int count = 0;
        for (VersionedValueList<Object> entry : variableVersions) {
            if (entry == null) {
                emptySpot = count;
                break;
            }
            count++;
        }

        int variableNumber;
        if (emptySpot != -1) {
            variableVersions.set(emptySpot, valuePerVersion);
            changeCallbacks.set(emptySpot, null);
            variableNumber = emptySpot;
        }
        else {
            // add entries matching in index the variable number
            variableVersions.add(valuePerVersion);
            changeCallbacks.add(null);
            variableNumber = currentVariableNumber;
            currentVariableNumber++;
        }

        // create reader
        reader = new VariableReader(versionThreadLocal, variableType, eventType, variableName, variableNumber, valuePerVersion);
        variables.put(variableName, reader);
    }

    public VariableReader getReader(String variableName)
    {
        return variables.get(variableName);
    }

    public void write(int variableNumber, Object newValue)
    {
        VariableVersionThreadEntry entry = versionThreadLocal.getCurrentThread();
        if (entry.getUncommitted() == null)
        {
            entry.setUncommitted(new HashMap<Integer, Object>());
        }
        entry.getUncommitted().put(variableNumber, newValue);
    }

    public ReadWriteLock getReadWriteLock()
    {
        return readWriteLock;
    }

    public void commit()
    {
        VariableVersionThreadEntry entry = versionThreadLocal.getCurrentThread();
        if (entry.getUncommitted() == null)
        {
            return;
        }

        // get new version for adding the new values (1 or many new values)
        int newVersion = currentVersionNumber + 1;

        if (currentVersionNumber == ROLLOVER_READER_BOUNDARY)
        {
            // Roll over to new collections;
            // This honors existing threads that will now use the "high" collection in the reader for high version requests
            // and low collection (new and updated) for low version requests
            rollOver();
            newVersion = 2;
        }
        long timestamp = timeProvider.getTime();

        // apply all uncommitted changes
        for (Map.Entry<Integer, Object> uncommittedEntry : entry.getUncommitted().entrySet())
        {
            VersionedValueList<Object> versions = variableVersions.get(uncommittedEntry.getKey());

            // add new value as a new version
            Object oldValue = versions.addValue(newVersion, uncommittedEntry.getValue(), timestamp);

            // make a callback that the value changed
            Set<VariableChangeCallback> callbacks = changeCallbacks.get(uncommittedEntry.getKey());
            if (callbacks != null)
            {
                for (VariableChangeCallback callback : callbacks)
                {
                    callback.update(uncommittedEntry.getValue(), oldValue);
                }
            }

            // Check current state - see if the variable exists in the state handler
            if (optionalStateHandler != null)
            {
                String name = versions.getName();
                optionalStateHandler.setState(name, uncommittedEntry.getKey(), uncommittedEntry.getValue());
            }
        }

        // this makes the new values visible to other threads (not this thread unless set-version called again)
        currentVersionNumber = newVersion;
        entry.setUncommitted(null);    // clean out uncommitted variables
    }

    public void rollback()
    {
        VariableVersionThreadEntry entry = versionThreadLocal.getCurrentThread();
        entry.setUncommitted(null);
    }

    /**
     * Rollover includes creating a new
     */
    private void rollOver()
    {
        for (Map.Entry<String, VariableReader> entry : variables.entrySet())
        {
            int variableNum = entry.getValue().getVariableNumber();
            String name = entry.getKey();
            long timestamp = timeProvider.getTime();

            // Construct a new collection, forgetting the history
            VersionedValueList<Object> versionsOld = variableVersions.get(variableNum);
            Object currentValue = versionsOld.getCurrentAndPriorValue().getCurrentVersion().getValue();
            VersionedValueList<Object> versionsNew = new VersionedValueList<Object>(name, 1, currentValue, timestamp, millisecondLifetimeOldVersions, readWriteLock.readLock(), HIGH_WATERMARK_VERSIONS, false);

            // Tell the reader to use the high collection for old requests
            entry.getValue().setVersionsHigh(versionsOld);
            entry.getValue().setVersionsLow(versionsNew);

            // Save new collection instead
            variableVersions.set(variableNum, versionsNew);
        }
    }

    public void checkAndWrite(int variableNumber, Object newValue) throws VariableValueException
    {
        if (newValue == null)
        {
            write(variableNumber, null);
            return;
        }

        Class valueType = newValue.getClass();
        String variableName = variableVersions.get(variableNumber).getName();
        VariableReader variableReader = variables.get(variableName);

        if (variableReader.getEventType() != null) {
            if ((newValue != null) && (!JavaClassHelper.isSubclassOrImplementsInterface(newValue.getClass(), variableReader.getEventType().getUnderlyingType()))) {
                throw new VariableValueException("Variable '" + variableName
                    + "' of declared event type '" + variableReader.getEventType().getName() + "' underlying type '" + variableReader.getEventType().getUnderlyingType().getName() +
                        "' cannot be assigned a value of type '" + valueType.getName() + "'");
            }
            EventBean eventBean = eventAdapterService.adapterForType(newValue, variableReader.getEventType());
            write(variableNumber, eventBean);
            return;
        }

        Class variableType = variableReader.getType();
        if ((valueType.equals(variableType)) || (variableType == Object.class))
        {
            write(variableNumber, newValue);
            return;
        }
        
        if ((!JavaClassHelper.isNumeric(variableType)) ||
            (!JavaClassHelper.isNumeric(valueType)))
        {
            throw new VariableValueException("Variable '" + variableName
                + "' of declared type '" + variableType.getName() +
                    "' cannot be assigned a value of type '" + valueType.getName() + "'");
        }

        // determine if the expression type can be assigned
        if (!(JavaClassHelper.canCoerce(valueType, variableType)))
        {
            throw new VariableValueException("Variable '" + variableName
                + "' of declared type '" + variableType.getName() +
                    "' cannot be assigned a value of type '" + valueType.getName() + "'");
        }

        Object valueCoerced = JavaClassHelper.coerceBoxed((Number) newValue, variableType);
        write(variableNumber, valueCoerced);
    }

    public String toString()
    {
        StringWriter writer = new StringWriter();
        for (Map.Entry<String, VariableReader> entry : variables.entrySet())
        {
            int variableNum = entry.getValue().getVariableNumber();
            VersionedValueList<Object> list = variableVersions.get(variableNum);
            writer.write("Variable '" + entry.getKey() + "' : " + list.toString() + "\n");
        }
        return writer.toString();
    }

    public Map<String, VariableReader> getVariables()
    {
        Map<String, VariableReader> variables = new HashMap<String, VariableReader>();
        variables.putAll(this.variables);
        return variables;
    }
}
