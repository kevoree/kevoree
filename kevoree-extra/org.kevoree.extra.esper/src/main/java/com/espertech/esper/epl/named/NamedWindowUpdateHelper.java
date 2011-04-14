/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.spec.OnTriggerSetAssignment;
import com.espertech.esper.event.EventBeanCopyMethod;
import com.espertech.esper.event.EventPropertyWriter;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.util.TypeWidener;
import com.espertech.esper.util.TypeWidenerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NamedWindowUpdateHelper
{
    private static final Log log = LogFactory.getLog(NamedWindowUpdateHelper.class);

    private final ExprEvaluator[] expressions;
    private final String[] propertyNames;
    private final EventPropertyWriter[] writers;
    private final EventBeanCopyMethod copyMethod;
    private final boolean[] notNullableField;
    private final TypeWidener[] wideners;

    public NamedWindowUpdateHelper(ExprEvaluator[] expressions, String[] propertyNames, EventPropertyWriter[] writers, EventBeanCopyMethod copyMethod, boolean[] notNullableField, TypeWidener[] wideners) {
        this.expressions = expressions;
        this.propertyNames = propertyNames;
        this.writers = writers;
        this.copyMethod = copyMethod;
        this.notNullableField = notNullableField;
        this.wideners = wideners;
    }

    public static NamedWindowUpdateHelper make(EventTypeSPI eventTypeSPI,
                                        List<OnTriggerSetAssignment> assignments,
                                        String namedWindowAlias)
            throws ExprValidationException
    {
        // validate expression, obtain wideners
        TypeWidener wideners[] = new TypeWidener[assignments.size()];
        List<String> properties = new ArrayList<String>();
        int len = assignments.size();
        ExprEvaluator[] expressions = new ExprEvaluator[len];
        EventPropertyWriter[] writers = new EventPropertyWriter[len];
        boolean[] notNullableField = new boolean[len];

        for (int i = 0; i < assignments.size(); i++)
        {
            OnTriggerSetAssignment assignment = assignments.get(i);
            String propertyName = assignment.getVariableName(); 
            expressions[i] = assignment.getExpression().getExprEvaluator();
            EventPropertyDescriptor writableProperty = eventTypeSPI.getWritableProperty(propertyName);

            if (writableProperty == null)
            {
                int indexDot = propertyName.indexOf(".");
                if ((namedWindowAlias != null) || (indexDot != -1)) {
                    String prefix = propertyName.substring(0, indexDot);
                    String name = propertyName.substring(indexDot + 1);
                    if (prefix.equals(namedWindowAlias)) {
                        writableProperty = eventTypeSPI.getWritableProperty(name);
                        propertyName = name;
                    }
                }
                if (writableProperty == null) {
                    throw new ExprValidationException("Property '" + propertyName + "' is not available for write access");
                }
            }
            writers[i] = eventTypeSPI.getWriter(propertyName);
            notNullableField[i] = writableProperty.getPropertyType().isPrimitive();

            properties.add(propertyName);
            wideners[i] = TypeWidenerFactory.getCheckPropertyAssignType(assignment.getExpression().toExpressionString(), assignment.getExpression().getExprEvaluator().getType(),
                    writableProperty.getPropertyType(), propertyName);
        }
        String[] propertyNames = properties.toArray(new String[properties.size()]);

        // map expression index to property index
        List<String> propertiesUniqueList = new ArrayList<String>(new HashSet<String>(properties));
        String[] propertiesArray = propertiesUniqueList.toArray(new String[propertiesUniqueList.size()]);
        EventBeanCopyMethod copyMethod = eventTypeSPI.getCopyMethod(propertiesArray);
        if (copyMethod == null) {
            throw new ExprValidationException("Event type does not support event bean copy");
        }

        return new NamedWindowUpdateHelper(expressions, propertyNames, writers, copyMethod, notNullableField, wideners);
    }

    public EventBean update(EventBean matchingEvent, EventBean[] eventsPerStream, ExprEvaluatorContext exprEvaluatorContext)
    {
        EventBean copy = copyMethod.copy(matchingEvent);
        eventsPerStream[0] = copy;

        for (int i = 0; i < expressions.length; i++) {
            Object result = expressions[i].evaluate(eventsPerStream, true, exprEvaluatorContext);

            if (result == null && notNullableField[i]) {
                log.warn("Null value returned by expression for assignment to property '" + propertyNames[i] + " is ignored as the property type is not nullable for expression");
                continue;
            }

            if (wideners[i] != null) {
                result = wideners[i].widen(result);
            }
            writers[i].write(result, copy);
        }
        return copy;
    }
}