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
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.VariableValueException;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.spec.OnTriggerSetAssignment;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventBeanCopyMethod;
import com.espertech.esper.event.EventPropertyWriter;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * A convenience class for dealing with reading and updating multiple variable values.
 */
public class VariableReadWritePackage
{
    private static final Log log = LogFactory.getLog(VariableReadWritePackage.class);

    private final VariableTriggerSetDesc[] assignments;
    private final VariableReader[] readers;
    private final boolean[] mustCoerce;
    private final WriteDesc writers[];
    private final Map<EventTypeSPI, EventBeanCopyMethod> copyMethods;

    private final EventAdapterService eventAdapterService;
    private final Map<String, Object> variableTypes;

    /**
     * Ctor.
     * @param assignments the list of variable assignments
     * @param variableService variable service
     * @param eventAdapterService event adapters
     * @throws ExprValidationException when variables cannot be found
     */
    public VariableReadWritePackage(List<OnTriggerSetAssignment> assignments, VariableService variableService, EventAdapterService eventAdapterService)
            throws ExprValidationException
    {
        this.assignments = toAssignments(assignments);

        this.readers = new VariableReader[assignments.size()];
        this.mustCoerce = new boolean[assignments.size()];
        this.writers = new WriteDesc[assignments.size()];

        this.variableTypes = new HashMap<String, Object>();
        this.eventAdapterService = eventAdapterService;

        Map<EventTypeSPI, CopyMethodDesc> eventTypeWrittenProps = new HashMap<EventTypeSPI, CopyMethodDesc>();
        int count = 0;

        for (OnTriggerSetAssignment assignment : assignments)
        {
            String fullVariableName = assignment.getVariableName();
            String variableName = assignment.getVariableName();
            String subPropertyName = null;

            int indexOfDot = variableName.indexOf('.');
            if (indexOfDot != -1) {
                subPropertyName = variableName.substring(indexOfDot + 1, variableName.length());
                variableName = variableName.substring(0, indexOfDot);
            }

            VariableReader variableReader = variableService.getReader(variableName);
            readers[count] = variableReader;
            if (variableReader == null)
            {
                throw new ExprValidationException("Variable by name '" + variableName + "' has not been created or configured");
            }

            if (subPropertyName != null) {
                if (variableReader.getEventType() == null) {
                    throw new ExprValidationException("Variable by name '" + variableName + "' does not have a property named '" + subPropertyName + "'");
                }
                EventType type = variableReader.getEventType();
                if (!(type instanceof EventTypeSPI)) {
                    throw new ExprValidationException("Variable by name '" + variableName + "' event type '" + type.getName() + "' not writable");
                }
                EventTypeSPI spi = (EventTypeSPI) type;
                EventPropertyWriter writer = spi.getWriter(subPropertyName);
                EventPropertyGetter getter = spi.getGetter(subPropertyName);
                if (writer == null) {
                    throw new ExprValidationException("Variable by name '" + variableName + "' the property '" + subPropertyName + "' is not writable");
                }

                variableTypes.put(fullVariableName, spi.getPropertyType(subPropertyName));
                CopyMethodDesc writtenProps = eventTypeWrittenProps.get(spi);
                if (writtenProps == null) {
                    writtenProps = new CopyMethodDesc(variableName, new ArrayList<String>());
                    eventTypeWrittenProps.put(spi, writtenProps);
                }
                writtenProps.getPropertiesCopied().add(subPropertyName);

                writers[count] = new WriteDesc(spi, variableName, writer, getter);
            }
            else {

                // determine types
                Class expressionType = assignment.getExpression().getExprEvaluator().getType();

                if (variableReader.getEventType() != null) {
                    if ((expressionType != null) && (!JavaClassHelper.isSubclassOrImplementsInterface(expressionType, variableReader.getEventType().getUnderlyingType()))) {
                        throw new VariableValueException("Variable '" + variableName
                            + "' of declared event type '" + variableReader.getEventType().getName() + "' underlying type '" + variableReader.getEventType().getUnderlyingType().getName() +
                                "' cannot be assigned a value of type '" + expressionType.getName() + "'");
                    }
                    variableTypes.put(variableName, variableReader.getEventType().getUnderlyingType());
                }
                else {

                    Class variableType = variableReader.getType();
                    variableTypes.put(variableName, variableType);

                    // determine if the expression type can be assigned
                    if (variableType != java.lang.Object.class) {
                        if ((JavaClassHelper.getBoxedType(expressionType) != variableType) &&
                            (expressionType != null))
                        {
                            if ((!JavaClassHelper.isNumeric(variableType)) ||
                                (!JavaClassHelper.isNumeric(expressionType)))
                            {
                                throw new ExprValidationException("Variable '" + variableName
                                    + "' of declared type '" + variableType.getName() +
                                        "' cannot be assigned a value of type '" + expressionType.getName() + "'");
                            }

                            if (!(JavaClassHelper.canCoerce(expressionType, variableType)))
                            {
                                throw new ExprValidationException("Variable '" + variableName
                                    + "' of declared type '" + variableType.getName() +
                                        "' cannot be assigned a value of type '" + expressionType.getName() + "'");
                            }

                            mustCoerce[count] = true;
                        }
                    }
                }
            }

            count++;
        }

        if (eventTypeWrittenProps.isEmpty()) {
            copyMethods = Collections.EMPTY_MAP;
            return;
        }

        copyMethods = new HashMap<EventTypeSPI, EventBeanCopyMethod>();
        for (Map.Entry<EventTypeSPI, CopyMethodDesc> entry : eventTypeWrittenProps.entrySet()) {
            List<String> propsWritten = entry.getValue().getPropertiesCopied();
            String[] props = propsWritten.toArray(new String[propsWritten.size()]);
            EventBeanCopyMethod copyMethod = entry.getKey().getCopyMethod(props);
            if (copyMethod == null){
                throw new ExprValidationException("Variable '" + entry.getValue().getVariableName()
                    + "' of declared type '" + entry.getKey().getName() +
                        "' cannot be assigned to");
            }
            copyMethods.put(entry.getKey(), copyMethod);
        }
    }

    /**
     * Write new variable values and commit, evaluating assignment expressions using the given
     * events per stream.
     * <p>
     * Populates an optional map of new values if a non-null map is passed.
     * @param variableService variable service
     * @param eventsPerStream events per stream
     * @param valuesWritten null or an empty map to populate with written values
     * @param exprEvaluatorContext expression evaluation context
     */
    public void writeVariables(VariableService variableService,
                                 EventBean[] eventsPerStream,
                                 Map<String, Object> valuesWritten,
                                 ExprEvaluatorContext exprEvaluatorContext)
    {
        Set<String> variablesBeansCopied = null;
        if (!copyMethods.isEmpty()) {
            variablesBeansCopied = new HashSet<String>();
        }

        // We obtain a write lock global to the variable space
        // Since expressions can contain variables themselves, these need to be unchangeable for the duration
        // as there could be multiple statements that do "var1 = var1 + 1".
        variableService.getReadWriteLock().writeLock().lock();
        try
        {
            variableService.setLocalVersion();

            int count = 0;
            for (VariableTriggerSetDesc assignment : assignments)
            {
                VariableReader reader = readers[count];
                Object value = assignment.evaluator.evaluate(eventsPerStream, true, exprEvaluatorContext);

                if (writers[count] != null) {
                    EventBean current = (EventBean) reader.getValue();
                    if (current == null) {
                        value = null;
                    }
                    else {
                        WriteDesc writeDesc = writers[count];
                        boolean copy = variablesBeansCopied.add(writeDesc.getVariableName());
                        if (copy) {
                            EventBean copied = copyMethods.get(writeDesc.getType()).copy(current);
                            current = copied;
                        }
                        variableService.write(reader.getVariableNumber(), current);
                        writeDesc.getWriter().write(value, current);
                    }
                }
                else if (reader.getEventType() != null) {
                    EventBean eventBean = eventAdapterService.adapterForType(value, reader.getEventType());
                    variableService.write(reader.getVariableNumber(), eventBean);
                }
                else {
                    if ((value != null) && (mustCoerce[count]))
                    {
                        value = JavaClassHelper.coerceBoxed((Number) value, reader.getType());
                    }
                    variableService.write(reader.getVariableNumber(), value);
                }
                
                count++;

                if (valuesWritten != null)
                {
                    valuesWritten.put(assignment.variableName, value);
                }
            }

            variableService.commit();
        }
        catch (RuntimeException ex)
        {
            log.error("Error evaluating on-set variable expressions: " + ex.getMessage(), ex);
            variableService.rollback();
        }
        finally
        {
            variableService.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Returns the readers to all variables.
     * @return readers
     */
    public VariableReader[] getReaders()
    {
        return readers;
    }

    /**
     * Returns a map of variable names and type of variable.
     * @return variables
     */
    public Map<String, Object> getVariableTypes()
    {
        return variableTypes;
    }

    /**
     * Iterate returning all values.
     * @return map of values
     */
    public Map<String, Object> iterate()
    {
        Map<String, Object> values = new HashMap<String, Object>();

        int count = 0;
        for (VariableTriggerSetDesc assignment : assignments)
        {
            VariableReader reader = readers[count];
            Object value = reader.getValue();

            if (value == null) {
                values.put(assignment.variableName, null);
            }
            else if (writers[count] != null) {
                EventBean current = (EventBean) reader.getValue();
                values.put(assignment.variableName, writers[count].getGetter().get(current));
            }
            else if (value instanceof EventBean) {
                values.put(assignment.variableName, ((EventBean) value).getUnderlying());
            }
            else {
                values.put(assignment.variableName, value);
            }
            count++;
        }
        return values;
    }

    private VariableTriggerSetDesc[] toAssignments(List<OnTriggerSetAssignment> assignments)
    {
        VariableTriggerSetDesc sets[] = new VariableTriggerSetDesc[assignments.size()];
        for (int i = 0; i < assignments.size(); i++) {
            sets[i] = new VariableTriggerSetDesc(assignments.get(i).getVariableName(), assignments.get(i).getExpression().getExprEvaluator());
        }
        return sets;
    }

    private static class CopyMethodDesc {
        private final String variableName;
        private final List<String> propertiesCopied;

        public CopyMethodDesc(String variableName, List<String> propertiesCopied)
        {
            this.variableName = variableName;
            this.propertiesCopied = propertiesCopied;
        }

        public String getVariableName()
        {
            return variableName;
        }

        public List<String> getPropertiesCopied()
        {
            return propertiesCopied;
        }
    }

    private static class WriteDesc {

        private final EventTypeSPI type;
        private final String variableName;
        private final EventPropertyWriter writer;
        private final EventPropertyGetter getter;

        public WriteDesc(EventTypeSPI type, String variableName, EventPropertyWriter writer, EventPropertyGetter getter)
        {
            this.type = type;
            this.variableName = variableName;
            this.writer = writer;
            this.getter = getter;
        }

        public String getVariableName()
        {
            return variableName;
        }

        public EventPropertyWriter getWriter()
        {
            return writer;
        }

        public EventTypeSPI getType()
        {
            return type;
        }

        public EventPropertyGetter getGetter()
        {
            return getter;
        }
    }

    private static class VariableTriggerSetDesc {
        private String variableName;
        private ExprEvaluator evaluator;

        public VariableTriggerSetDesc(String variableName, ExprEvaluator evaluator)
        {
            this.variableName = variableName;
            this.evaluator = evaluator;
        }
    }
}
