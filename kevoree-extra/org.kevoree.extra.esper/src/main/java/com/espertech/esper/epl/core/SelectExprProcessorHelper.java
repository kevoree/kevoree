/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.FragmentEventType;
import com.espertech.esper.epl.core.eval.*;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.spec.InsertIntoDesc;
import com.espertech.esper.epl.spec.SelectClauseExprCompiledSpec;
import com.espertech.esper.epl.spec.SelectClauseStreamCompiledSpec;
import com.espertech.esper.event.EventAdapterException;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.NativeEventType;
import com.espertech.esper.event.WrapperEventType;
import com.espertech.esper.event.map.MapEventType;
import com.espertech.esper.event.vaevent.ValueAddEventProcessor;
import com.espertech.esper.event.vaevent.ValueAddEventService;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Processor for select-clause expressions that handles a list of selection items represented by
 * expression nodes. Computes results based on matching events.
 */
public class SelectExprProcessorHelper
{
	private static final Log log = LogFactory.getLog(SelectExprProcessorHelper.class);

    private final List<SelectClauseExprCompiledSpec> selectionList;
    private final List<SelectClauseStreamCompiledSpec> selectedStreams;
    private final InsertIntoDesc insertIntoDesc;
    private final boolean isUsingWildcard;
    private final StreamTypeService typeService;
    private final EventAdapterService eventAdapterService;
    private final ValueAddEventService valueAddEventService;
    private final SelectExprEventTypeRegistry selectExprEventTypeRegistry;
    private final MethodResolutionService methodResolutionService;
    private final ExprEvaluatorContext exprEvaluatorContext;
    private final String statementId;

    /**
     * Ctor.
     * @param selectionList - list of select-clause items
     * @param insertIntoDesc - descriptor for insert-into clause contains column names overriding select clause names
     * @param isUsingWildcard - true if the wildcard (*) appears in the select clause
     * @param typeService -service for information about streams
     * @param eventAdapterService - service for generating events and handling event types
     * @param valueAddEventService - service that handles update events
     * @param selectExprEventTypeRegistry - service for statement to type registry
     * @param methodResolutionService - for resolving methods
     * @param exprEvaluatorContext context for expression evalauation
     * @throws com.espertech.esper.epl.expression.ExprValidationException thrown if any of the expressions don't validate
     */
    public SelectExprProcessorHelper(List<SelectClauseExprCompiledSpec> selectionList,
                                   List<SelectClauseStreamCompiledSpec> selectedStreams,
                                   InsertIntoDesc insertIntoDesc,
                                   boolean isUsingWildcard,
                                   StreamTypeService typeService,
                                   EventAdapterService eventAdapterService,
                                   ValueAddEventService valueAddEventService,
                                   SelectExprEventTypeRegistry selectExprEventTypeRegistry,
                                   MethodResolutionService methodResolutionService,
                                   ExprEvaluatorContext exprEvaluatorContext,
                                   String statementId) throws ExprValidationException
    {
        this.selectionList = selectionList;
        this.selectedStreams = selectedStreams;
        this.insertIntoDesc = insertIntoDesc;
        this.eventAdapterService = eventAdapterService;
        this.isUsingWildcard = isUsingWildcard;
        this.typeService = typeService;
        this.exprEvaluatorContext = exprEvaluatorContext;
        this.valueAddEventService = valueAddEventService;
        this.selectExprEventTypeRegistry = selectExprEventTypeRegistry;
        this.methodResolutionService = methodResolutionService;
        this.statementId = statementId;
    }

    public SelectExprProcessor getEvaluator() throws ExprValidationException {

        // Get the named and un-named stream selectors (i.e. select s0.* from S0 as s0), if any
        List<SelectClauseStreamCompiledSpec> namedStreams = new ArrayList<SelectClauseStreamCompiledSpec>();
        List<SelectClauseStreamCompiledSpec> unnamedStreams = new ArrayList<SelectClauseStreamCompiledSpec>();
        for (SelectClauseStreamCompiledSpec spec : selectedStreams)
        {
            if (spec.getOptionalName() == null)
            {
                unnamedStreams.add(spec);
            }
            else
            {
                namedStreams.add(spec);
                if (spec.isProperty())
                {
                    throw new ExprValidationException("The property wildcard syntax must be used without column name");
                }
            }
        }
        // Error if there are more then one un-named streams (i.e. select s0.*, s1.* from S0 as s0, S1 as s1)
        // Thus there is only 1 unnamed stream selector maximum.
        if (unnamedStreams.size() > 1)
        {
            throw new ExprValidationException("A column name must be supplied for all but one stream if multiple streams are selected via the stream.* notation");
        }        

        if (selectedStreams.isEmpty() && selectionList.isEmpty() && !isUsingWildcard)
        {
            throw new IllegalArgumentException("Empty selection list not supported");
        }

        for (SelectClauseExprCompiledSpec entry : selectionList)
        {
            if (entry.getAssignedName() == null)
            {
                throw new IllegalArgumentException("Expected name for each expression has not been supplied");
            }
        }

        // Verify insert into clause
        if (insertIntoDesc != null)
        {
            verifyInsertInto(insertIntoDesc, selectionList);
        }

        // Build a subordinate wildcard processor for joins
        SelectExprJoinWildcardProcessor joinWildcardProcessor = null;
        if(typeService.getStreamNames().length > 1 && isUsingWildcard)
        {
        	joinWildcardProcessor = new SelectExprJoinWildcardProcessor(typeService.getStreamNames(), typeService.getEventTypes(), eventAdapterService, null, selectExprEventTypeRegistry, methodResolutionService, exprEvaluatorContext);
        }

        // Resolve underlying event type in the case of wildcard select
        EventType eventType = null;
        boolean singleStreamWrapper = false;
        if(isUsingWildcard)
        {
        	if(joinWildcardProcessor != null)
        	{
        		eventType = joinWildcardProcessor.getResultEventType();
        	}
        	else
        	{
        		eventType = typeService.getEventTypes()[0];
        		if(eventType instanceof WrapperEventType)
        		{
        			singleStreamWrapper = true;
        		}
        	}
        }

        // Get expression nodes
        ExprEvaluator[] expressionNodes = new ExprEvaluator[selectionList.size()];
        Object[] expressionReturnTypes = new Object[selectionList.size()];
        for (int i = 0; i < selectionList.size(); i++)
        {
            ExprNode expr = selectionList.get(i).getSelectExpression();
            expressionNodes[i] = expr.getExprEvaluator();
            Map<String, Object> eventTypeExpr = expressionNodes[i].getEventType();
            if (eventTypeExpr == null) {
                expressionReturnTypes[i] = expressionNodes[i].getType();
            }
            else {
                final ExprEvaluator innerExprEvaluator = expr.getExprEvaluator();
                final EventType mapType = eventAdapterService.createAnonymousMapType(eventTypeExpr);
                ExprEvaluator evaluatorFragment = new ExprEvaluator() {
                    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                    {
                        Map<String, Object> values = (Map<String, Object>) innerExprEvaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                        if (values == null) {
                            values = Collections.emptyMap();
                        }
                        return eventAdapterService.adaptorForTypedMap(values, mapType);
                    }
                    public Class getType()
                    {
                        return Map.class;
                    }
                    public Map<String, Object> getEventType() {
                        return null;
                    }
                };

                expressionReturnTypes[i] = mapType;
                expressionNodes[i] = evaluatorFragment;
            }
        }

        // Get column names
        String[] columnNames;
        if ((insertIntoDesc != null) && (!insertIntoDesc.getColumnNames().isEmpty()))
        {
            columnNames = insertIntoDesc.getColumnNames().toArray(new String[insertIntoDesc.getColumnNames().size()]);
        }
        else  if (!selectedStreams.isEmpty()) { // handle stream selection column names
            int numStreamColumnsJoin = 0;
            if (isUsingWildcard && typeService.getEventTypes().length > 1)
            {
                numStreamColumnsJoin = typeService.getEventTypes().length;
            }
            columnNames = new String[selectionList.size() + namedStreams.size() + numStreamColumnsJoin];
            int count = 0;
            for (SelectClauseExprCompiledSpec aSelectionList : selectionList)
            {
                columnNames[count] = aSelectionList.getAssignedName();
                count++;
            }
            for (SelectClauseStreamCompiledSpec aSelectionList : namedStreams)
            {
                columnNames[count] = aSelectionList.getOptionalName();
                count++;
            }
            // for wildcard joins, add the streams themselves
            if (isUsingWildcard && typeService.getEventTypes().length > 1)
            {
                for (String streamName : typeService.getStreamNames())
                {
                    columnNames[count] = streamName;
                    count++;
                }
            }
        }
        else    // handle regular column names
        {
            columnNames = new String[selectionList.size()];
            for (int i = 0; i < selectionList.size(); i++)
            {
                columnNames[i] = selectionList.get(i).getAssignedName();
            }
        }

        // Find if there is any fragments selected
        EventType targetType= null;
        if (insertIntoDesc != null)
        {
            targetType = eventAdapterService.getExistsTypeByName(insertIntoDesc.getEventTypeName());
        }

        // Find if there is any fragment event types:
        // This is a special case for fragments: select a, b from pattern [a=A -> b=B]
        // We'd like to maintain 'A' and 'B' EventType in the Map type, and 'a' and 'b' EventBeans in the event bean
        for (int i = 0; i < selectionList.size(); i++)
        {
            if (!(expressionNodes[i] instanceof ExprIdentNode))
            {
                continue;
            }

            ExprIdentNode identNode = (ExprIdentNode) expressionNodes[i];
            String propertyName = identNode.getResolvedPropertyName();
            final int streamNum = identNode.getStreamId();

            EventType eventTypeStream = typeService.getEventTypes()[streamNum];
            if (eventTypeStream instanceof NativeEventType)
            {
                continue;   // we do not transpose the native type for performance reasons
            }
            
            FragmentEventType fragmentType = eventTypeStream.getFragmentType(propertyName);
            if ((fragmentType == null) || (fragmentType.isNative()))
            {
                continue;   // we also ignore native Java classes as fragments for performance reasons
            }

            // may need to unwrap the fragment if the target type has this underlying type
            FragmentEventType targetFragment = null;
            if (targetType != null)
            {
                targetFragment = targetType.getFragmentType(columnNames[i]);
            }
            if ((targetType != null) &&
                (fragmentType.getFragmentType().getUnderlyingType() == expressionReturnTypes[i]) &&
                ((targetFragment == null) || (targetFragment != null && targetFragment.isNative())) )
            {
                ExprEvaluator evaluatorFragment;

                // A match was found, we replace the expression
                final EventPropertyGetter getter = eventTypeStream.getGetter(propertyName);
                final Class returnType = eventTypeStream.getPropertyType(propertyName);
                evaluatorFragment = new ExprEvaluator() {
                    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                    {
                        EventBean streamEvent = eventsPerStream[streamNum];
                        if (streamEvent == null)
                        {
                            return null;
                        }
                        return getter.get(streamEvent);
                    }
                    public Class getType()
                    {
                        return returnType;
                    }
                    @Override
                    public Map<String, Object> getEventType() {
                        return null;
                    }
                };
                expressionNodes[i] = evaluatorFragment;
            }
            else
            {
                ExprEvaluator evaluatorFragment;
                final EventPropertyGetter getter =  eventTypeStream.getGetter(propertyName);
                final Class returnType = eventTypeStream.getFragmentType(propertyName).getFragmentType().getUnderlyingType();

                // A match was found, we replace the expression
                evaluatorFragment = new ExprEvaluator() {

                    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                    {
                        EventBean streamEvent = eventsPerStream[streamNum];
                        if (streamEvent == null)
                        {
                            return null;
                        }
                        return getter.getFragment(streamEvent);
                    }

                    public Class getType()
                    {
                        return returnType;
                    }

                    public Map<String, Object> getEventType() {
                        return null;
                    }
                };

                expressionNodes[i] = evaluatorFragment;
                if (!fragmentType.isIndexed())
                {
                    expressionReturnTypes[i] = fragmentType.getFragmentType();
                }
                else
                {
                    expressionReturnTypes[i] = new EventType[] {fragmentType.getFragmentType()};
                }
            }
        }

        // Find if there is any stream expression (ExprStreamNode) :
        // This is a special case for stream selection: select a, b from A as a, B as b
        // We'd like to maintain 'A' and 'B' EventType in the Map type, and 'a' and 'b' EventBeans in the event bean
        for (int i = 0; i < selectionList.size(); i++)
        {
            if (!(expressionNodes[i] instanceof ExprStreamUnderlyingNode))
            {
                continue;
            }

            ExprStreamUnderlyingNode undNode = (ExprStreamUnderlyingNode) expressionNodes[i];
            final int streamNum = undNode.getStreamId();
            final Class returnType = undNode.getType();
            EventType eventTypeStream = typeService.getEventTypes()[streamNum];

            // A match was found, we replace the expression
            ExprEvaluator evaluator = new ExprEvaluator() {

                public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
                {
                    return eventsPerStream[streamNum];
                }

                public Class getType()
                {
                    return returnType;
                }

                public Map<String, Object> getEventType() {
                    return null;
                }
            };

            expressionNodes[i] = evaluator;
            expressionReturnTypes[i] = eventTypeStream;
        }

        // Build event type that reflects all selected properties
        Map<String, Object> selPropertyTypes = new LinkedHashMap<String, Object>();
        int count = 0;
        for (ExprEvaluator expressionNode : expressionNodes)
        {
            Object expressionReturnType = expressionReturnTypes[count];
            selPropertyTypes.put(columnNames[count], expressionReturnType);
            count++;
        }
        if (!selectedStreams.isEmpty()) {
            for (SelectClauseStreamCompiledSpec element : namedStreams)
            {
                EventType eventTypeStream = typeService.getEventTypes()[element.getStreamNumber()];
                selPropertyTypes.put(columnNames[count], eventTypeStream);
                count++;
            }
            if (isUsingWildcard && typeService.getEventTypes().length > 1)
            {
                for (int i = 0; i < typeService.getEventTypes().length; i++)
                {
                    EventType eventTypeStream = typeService.getEventTypes()[i];
                    selPropertyTypes.put(columnNames[count], eventTypeStream);
                    count++;
                }
            }            
        }

        // Handle stream selection
        EventType underlyingEventType = null;
        int underlyingStreamNumber = 0;
        boolean underlyingIsFragmentEvent = false;
        EventPropertyGetter underlyingPropertyEventGetter = null;

        if (!selectedStreams.isEmpty()) {
            // Resolve underlying event type in the case of wildcard or non-named stream select.
            // Determine if the we are considering a tagged event or a stream name.
            if((isUsingWildcard) || (!unnamedStreams.isEmpty()))
            {
                if (!unnamedStreams.isEmpty())
                {
                    // the tag.* syntax for :  select tag.* from pattern [tag = A]
                    underlyingStreamNumber = unnamedStreams.get(0).getStreamNumber();
                    if (unnamedStreams.get(0).isFragmentEvent())
                    {
                        EventType compositeMap = typeService.getEventTypes()[underlyingStreamNumber];
                        FragmentEventType fragment = compositeMap.getFragmentType(unnamedStreams.get(0).getStreamName());
                        underlyingEventType = fragment.getFragmentType();
                        underlyingIsFragmentEvent = true;
                    }
                    // the property.* syntax for :  select property.* from A
                    else if (unnamedStreams.get(0).isProperty())
                    {
                        String propertyName = unnamedStreams.get(0).getStreamName();
                        Class propertyType = unnamedStreams.get(0).getPropertyType();
                        int streamNumber = unnamedStreams.get(0).getStreamNumber();

                        if (JavaClassHelper.isJavaBuiltinDataType(unnamedStreams.get(0).getPropertyType()))
                        {
                            throw new ExprValidationException("The property wildcard syntax cannot be used on built-in types as returned by property '" + propertyName + "'");
                        }

                        // create or get an underlying type for that Class
                        underlyingEventType = eventAdapterService.addBeanType(propertyType.getName(), propertyType, false, false, false);
                        selectExprEventTypeRegistry.add(underlyingEventType);
                        underlyingPropertyEventGetter = typeService.getEventTypes()[streamNumber].getGetter(propertyName);
                        if (underlyingPropertyEventGetter == null)
                        {
                            throw new ExprValidationException("Unexpected error resolving property getter for property " + propertyName);
                        }
                    }
                    // the stream.* syntax for:  select a.* from A as a
                    else
                    {
                        underlyingEventType = typeService.getEventTypes()[underlyingStreamNumber];
                    }
                }
                else
                {
                    // no un-named stream selectors, but a wildcard was specified
                    if (typeService.getEventTypes().length == 1)
                    {
                        // not a join, we are using the selected event
                        underlyingEventType = typeService.getEventTypes()[0];
                        if(underlyingEventType instanceof WrapperEventType)
                        {
                            singleStreamWrapper = true;
                        }
                    }
                    else
                    {
                        // For joins, all results are placed in a map with properties for each stream
                        underlyingEventType = null;
                    }
                }
            }
        }

        SelectExprContext selectExprContext = new SelectExprContext(expressionNodes, columnNames, exprEvaluatorContext, eventAdapterService);

        if (insertIntoDesc == null)
        {
            if (!selectedStreams.isEmpty()) {
                EventType resultEventType;
                if (underlyingEventType != null)
                {
                    resultEventType = eventAdapterService.createAnonymousWrapperType(underlyingEventType, selPropertyTypes);
                    return new EvalSelectStreamWUnderlying(selectExprContext, resultEventType, namedStreams, isUsingWildcard,
                            unnamedStreams, underlyingEventType, singleStreamWrapper, underlyingIsFragmentEvent, underlyingStreamNumber, underlyingPropertyEventGetter);
                }
                else
                {
                    resultEventType = eventAdapterService.createAnonymousMapType(selPropertyTypes);
                    return new EvalSelectStreamNoUnderlying(selectExprContext, resultEventType, namedStreams, isUsingWildcard);
                }
            }

            if (isUsingWildcard)
            {
        	    EventType resultEventType = eventAdapterService.createAnonymousWrapperType(eventType, selPropertyTypes);
                if (singleStreamWrapper) {
                    return new EvalSelectWildcardSSWrapper(selectExprContext, resultEventType);
                }
                if (joinWildcardProcessor == null) {
                    return new EvalSelectWildcard(selectExprContext, resultEventType);
                }
                return new EvalSelectWildcardJoin(selectExprContext, resultEventType, joinWildcardProcessor);
            }

            EventType resultEventType = eventAdapterService.createAnonymousMapType(selPropertyTypes);
            return new EvalSelectNoWildcard(selectExprContext, resultEventType);
        }

        EventType vaeInnerEventType = null;
        boolean singleColumnCoercion = false;
        boolean isRevisionEvent = false;

        try
        {
            if (!selectedStreams.isEmpty()) {
                EventType resultEventType;
                if (underlyingEventType != null)
                {
                    resultEventType = eventAdapterService.addWrapperType(insertIntoDesc.getEventTypeName(), underlyingEventType, selPropertyTypes, false, true);
                    return new EvalSelectStreamWUnderlying(selectExprContext, resultEventType, namedStreams, isUsingWildcard,
                            unnamedStreams, underlyingEventType, singleStreamWrapper, underlyingIsFragmentEvent, underlyingStreamNumber, underlyingPropertyEventGetter);
                }
                else
                {
                    resultEventType = eventAdapterService.addNestableMapType(insertIntoDesc.getEventTypeName(), selPropertyTypes, null, false, false, false, false, true);
                    return new EvalSelectStreamNoUnderlying(selectExprContext, resultEventType, namedStreams, isUsingWildcard);
                }
            }

            ValueAddEventProcessor vaeProcessor = valueAddEventService.getValueAddProcessor(insertIntoDesc.getEventTypeName());
            EventType resultEventType;
            if (isUsingWildcard)
            {
                if (vaeProcessor != null)
                {
                    resultEventType = vaeProcessor.getValueAddEventType();
                    isRevisionEvent = true;
                    vaeProcessor.validateEventType(eventType);
                }
                else
                {
                    EventType existingType = eventAdapterService.getExistsTypeByName(insertIntoDesc.getEventTypeName());
                    SelectExprInsertEventBean selectExprInsertEventBean = null;
                    if (existingType != null)
                    {
                        selectExprInsertEventBean = SelectExprInsertEventBean.getInsertUnderlying(eventAdapterService, existingType);
                    }
                    if ((existingType != null) && (selectExprInsertEventBean != null))
                    {
                        selectExprInsertEventBean.initialize(isUsingWildcard, typeService, expressionNodes, columnNames, expressionReturnTypes, methodResolutionService, eventAdapterService);
                        resultEventType = existingType;
                        return new EvalInsertNative(resultEventType, selectExprInsertEventBean, exprEvaluatorContext);
                    }
                    else if (existingType != null && selPropertyTypes.isEmpty() && existingType instanceof MapEventType) {
                        resultEventType = existingType;
                        return new EvalInsertMapTypeCoercion(resultEventType, eventAdapterService);
                    }
                    else
                    {
                        resultEventType = eventAdapterService.addWrapperType(insertIntoDesc.getEventTypeName(), eventType, selPropertyTypes, false, true);
                    }
                }

                if (singleStreamWrapper) {
                    if (!isRevisionEvent) {
                        return new EvalInsertWildcardSSWrapper(selectExprContext, resultEventType);
                    }
                    else {
                        return new EvalInsertWildcardSSWrapperRevision(selectExprContext, resultEventType, vaeProcessor);
                    }
                }
                if (joinWildcardProcessor == null) {
                    if (!isRevisionEvent) {
                        return new EvalInsertWildcard(selectExprContext, resultEventType);
                    }
                    else {
                        return new EvalInsertWildcardRevision(selectExprContext, resultEventType, vaeProcessor);
                    }
                }
                else {
                    if (!isRevisionEvent) {
                        return new EvalInsertWildcardJoin(selectExprContext, resultEventType, joinWildcardProcessor);
                    }
                    else {
                        return new EvalInsertWildcardJoinRevision(selectExprContext, resultEventType, joinWildcardProcessor, vaeProcessor);
                    }
                }
            }

            // not using wildcard
            resultEventType = null;
            if ((columnNames.length == 1) && (insertIntoDesc.getColumnNames().size() == 0))
            {
                EventType existingType = eventAdapterService.getExistsTypeByName(insertIntoDesc.getEventTypeName());
                if (existingType != null)
                {
                    // check if the existing type and new type are compatible
                    Object columnOneType = expressionReturnTypes[0];
                    if (existingType instanceof WrapperEventType)
                    {
                        WrapperEventType wrapperType = (WrapperEventType) existingType;
                        // Map and Object both supported
                        if (wrapperType.getUnderlyingEventType().getUnderlyingType() == columnOneType)
                        {
                            singleColumnCoercion = true;
                            resultEventType = existingType;
                        }
                    }
                }
            }
            if (resultEventType == null)
            {
                if (vaeProcessor != null)
                {
                    // Use an anonymous type if the target is not a variant stream
                    if (valueAddEventService.getValueAddProcessor(insertIntoDesc.getEventTypeName()) == null) {
                        resultEventType = eventAdapterService.createAnonymousMapType(selPropertyTypes);
                    }
                    else {
                        String statementName = "stmt_" + statementId + "_insert";
                        resultEventType = eventAdapterService.addNestableMapType(statementName, selPropertyTypes, null, false, false, false, false, true);
                    }
                }
                else
                {
                    EventType existingType = eventAdapterService.getExistsTypeByName(insertIntoDesc.getEventTypeName());

                    if (existingType == null) {
                        // The type may however be an auto-import or fully-qualified class name
                        Class clazz = null;
                        try {
                            clazz = this.methodResolutionService.resolveClass(insertIntoDesc.getEventTypeName());
                        }
                        catch (EngineImportException e) {
                            log.debug("Target stream name '" + insertIntoDesc.getEventTypeName() + "' is not resolved as a class name");
                        }
                        if (clazz != null) {
                            existingType = eventAdapterService.addBeanType(clazz.getName(), clazz, false, false, false);
                        }
                    }

                    SelectExprInsertEventBean selectExprInsertEventBean = null;
                    if (existingType != null)
                    {
                        selectExprInsertEventBean = SelectExprInsertEventBean.getInsertUnderlying(eventAdapterService, existingType);
                    }
                    if ((existingType != null) && (selectExprInsertEventBean != null))
                    {
                        selectExprInsertEventBean.initialize(isUsingWildcard, typeService, expressionNodes, columnNames, expressionReturnTypes, methodResolutionService, eventAdapterService);
                        resultEventType = existingType;
                        return new EvalInsertNative(resultEventType, selectExprInsertEventBean, exprEvaluatorContext);
                    }
                    else
                    {
                        resultEventType = eventAdapterService.addNestableMapType(insertIntoDesc.getEventTypeName(), selPropertyTypes, null, false, false, false, false, true);
                    }
                }
            }
            if (vaeProcessor != null)
            {
                vaeProcessor.validateEventType(resultEventType);
                vaeInnerEventType = resultEventType;
                resultEventType = vaeProcessor.getValueAddEventType();
                isRevisionEvent = true;
            }

            if (singleColumnCoercion) {
                if (!isRevisionEvent) {
                    if (resultEventType instanceof MapEventType) {
                        return new EvalInsertNoWildcardSingleColCoercionMap(selectExprContext, resultEventType);
                    }
                    else {
                        return new EvalInsertNoWildcardSingleColCoercionBean(selectExprContext, resultEventType);
                    }
                }
                else {
                    if (resultEventType instanceof MapEventType) {
                        return new EvalInsertNoWildcardSingleColCoercionRevisionMap(selectExprContext, resultEventType, vaeProcessor, vaeInnerEventType);
                    }
                    else {
                        return new EvalInsertNoWildcardSingleColCoercionRevisionBean(selectExprContext, resultEventType, vaeProcessor, vaeInnerEventType);
                    }
                }
            }
            if (!isRevisionEvent) {
                return new EvalInsertNoWildcard(selectExprContext, resultEventType);
            }
            else {
                return new EvalInsertNoWildcardRevision(selectExprContext, resultEventType, vaeProcessor, vaeInnerEventType);
            }
        }
        catch (EventAdapterException ex)
        {
            log.debug("Exception provided by event adapter: " + ex.getMessage(), ex);
            throw new ExprValidationException(ex.getMessage());
        }
    }

    private static void verifyInsertInto(InsertIntoDesc insertIntoDesc,
                                         List<SelectClauseExprCompiledSpec> selectionList)
        throws ExprValidationException
    {
        // Verify all column names are unique
        Set<String> names = new HashSet<String>();
        for (String element : insertIntoDesc.getColumnNames())
        {
            if (names.contains(element))
            {
                throw new ExprValidationException("Property name '" + element + "' appears more then once in insert-into clause");
            }
            names.add(element);
        }

        // Verify number of columns matches the select clause
        if ( (!insertIntoDesc.getColumnNames().isEmpty()) &&
             (insertIntoDesc.getColumnNames().size() != selectionList.size()) )
        {
            throw new ExprValidationException("Number of supplied values in the select clause does not match insert-into clause");
        }
    }
}
