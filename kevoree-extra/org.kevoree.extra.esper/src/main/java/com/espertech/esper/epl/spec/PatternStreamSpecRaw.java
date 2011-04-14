/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.StreamTypeServiceImpl;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.property.PropertyEvaluator;
import com.espertech.esper.epl.property.PropertyEvaluatorFactory;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.filter.FilterSpecCompiled;
import com.espertech.esper.filter.FilterSpecCompiler;
import com.espertech.esper.pattern.*;
import com.espertech.esper.pattern.guard.GuardFactory;
import com.espertech.esper.pattern.guard.GuardParameterException;
import com.espertech.esper.pattern.observer.ObserverFactory;
import com.espertech.esper.pattern.observer.ObserverParameterException;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.UuidGenerator;
import com.espertech.esper.view.ViewParameterException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Pattern specification in unvalidated, unoptimized form.
 */
public class PatternStreamSpecRaw extends StreamSpecBase implements StreamSpecRaw
{
    private final EvalNode evalNode;
    private static final Log log = LogFactory.getLog(PatternStreamSpecRaw.class);
    private static final long serialVersionUID = 6393401926404401433L;

    /**
     * Ctor.
     * @param evalNode - pattern evaluation node representing pattern statement
     * @param viewSpecs - specifies what view to use to derive data
     * @param optionalStreamName - stream name, or null if none supplied
     * @param streamSpecOptions - additional options, such as unidirectional stream in a join
     */
    public PatternStreamSpecRaw(EvalNode evalNode, List<ViewSpec> viewSpecs, String optionalStreamName, StreamSpecOptions streamSpecOptions)
    {
        super(optionalStreamName, viewSpecs, streamSpecOptions);
        this.evalNode = evalNode;
    }

    /**
     * Returns the pattern expression evaluation node for the top pattern operator.
     * @return parent pattern expression node
     */
    public EvalNode getEvalNode()
    {
        return evalNode;
    }

    public StreamSpecCompiled compile(StatementContext context,
                                      Set<String> eventTypeReferences,
                                      boolean isInsertInto)
            throws ExprValidationException
    {
        MatchEventSpec tags = new MatchEventSpec();
        recursiveCompile(evalNode, context, eventTypeReferences, isInsertInto, tags);
        return new PatternStreamSpecCompiled(evalNode, tags.getTaggedEventTypes(), tags.getArrayEventTypes(), this.getViewSpecs(), this.getOptionalStreamName(), this.getOptions());
    }

    private static void recursiveCompile(EvalNode evalNode, StatementContext context, Set<String> eventTypeReferences, boolean isInsertInto, MatchEventSpec tags) throws ExprValidationException
    {
        for (EvalNode child : evalNode.getChildNodes())
        {
            recursiveCompile(child, context, eventTypeReferences, isInsertInto, tags);
        }

        LinkedHashMap<String, Pair<EventType, String>> newTaggedEventTypes = null;
        LinkedHashMap<String, Pair<EventType, String>> newArrayEventTypes = null;

        if (evalNode instanceof EvalFilterNode)
        {
            EvalFilterNode filterNode = (EvalFilterNode) evalNode;
            String eventName = filterNode.getRawFilterSpec().getEventTypeName();
            EventType resolvedEventType = FilterStreamSpecRaw.resolveType(context.getEngineURI(), eventName, context.getEventAdapterService(), context.getPlugInTypeResolutionURIs());
            EventType finalEventType = resolvedEventType;
            String optionalTag = filterNode.getEventAsName();
            boolean isPropertyEvaluation = false;

            // obtain property event type, if final event type is properties
            if (filterNode.getRawFilterSpec().getOptionalPropertyEvalSpec() != null)
            {
                PropertyEvaluator optionalPropertyEvaluator = PropertyEvaluatorFactory.makeEvaluator(filterNode.getRawFilterSpec().getOptionalPropertyEvalSpec(), resolvedEventType, filterNode.getEventAsName(), context.getEventAdapterService(), context.getMethodResolutionService(), context.getSchedulingService(), context.getVariableService(), context.getEngineURI(), context.getStatementId());
                finalEventType = optionalPropertyEvaluator.getFragmentEventType();
                isPropertyEvaluation = true;
            }

            if (finalEventType instanceof EventTypeSPI)
            {
                eventTypeReferences.add(((EventTypeSPI) finalEventType).getMetadata().getPrimaryName());
            }

            // If a tag was supplied for the type, the tags must stay with this type, i.e. a=BeanA -> b=BeanA -> a=BeanB is a no
            if (optionalTag != null)
            {
                Pair<EventType, String> pair = tags.getTaggedEventTypes().get(optionalTag);
                EventType existingType = null;
                if (pair != null)
                {
                    existingType = pair.getFirst();
                }
                if (existingType == null)
                {
                    pair = tags.getArrayEventTypes().get(optionalTag);
                    if (pair != null)
                    {
                        throw new ExprValidationException("Tag '" + optionalTag + "' for event '" + eventName +
                                "' used in the repeat-until operator cannot also appear in other filter expressions");
                    }
                }
                if ((existingType != null) && (existingType != finalEventType))
                {
                    throw new ExprValidationException("Tag '" + optionalTag + "' for event '" + eventName +
                            "' has already been declared for events of type " + existingType.getUnderlyingType().getName());
                }
                pair = new Pair<EventType, String>(finalEventType, eventName);

                // add tagged type
                if (isPropertyEvaluation)
                {
                    newArrayEventTypes = new LinkedHashMap<String, Pair<EventType, String>>();
                    newArrayEventTypes.put(optionalTag, pair);
                }
                else
                {
                    newTaggedEventTypes = new LinkedHashMap<String, Pair<EventType, String>>();
                    newTaggedEventTypes.put(optionalTag, pair);
                }
            }

            // For this filter, filter types are all known tags at this time,
            // and additionally stream 0 (self) is our event type.
            // Stream type service allows resolution by property name event if that name appears in other tags.
            // by defaulting to stream zero.
            // Stream zero is always the current event type, all others follow the order of the map (stream 1 to N).
            String selfStreamName = optionalTag;
            if (selfStreamName == null)
            {
                selfStreamName = "s_" + UuidGenerator.generate();
            }
            LinkedHashMap<String, Pair<EventType, String>> filterTypes = new LinkedHashMap<String, Pair<EventType, String>>();
            Pair<EventType, String> typePair = new Pair<EventType, String>(finalEventType, eventName);
            filterTypes.put(selfStreamName, typePair);
            filterTypes.putAll(tags.getTaggedEventTypes());

            // for the filter, specify all tags used
            LinkedHashMap<String, Pair<EventType, String>> filterTaggedEventTypes = new LinkedHashMap<String, Pair<EventType, String>>(tags.getTaggedEventTypes());
            filterTaggedEventTypes.remove(optionalTag);

            // handle array tags (match-until clause)
            LinkedHashMap<String, Pair<EventType, String>> arrayCompositeEventTypes = null;
            if (tags.getArrayEventTypes() != null)
            {
                arrayCompositeEventTypes = new LinkedHashMap<String, Pair<EventType, String>>();
                EventType arrayTagCompositeEventType = context.getEventAdapterService().createSemiAnonymousMapType(new HashMap(), tags.getArrayEventTypes(), isInsertInto);
                for (Map.Entry<String, Pair<EventType, String>> entry : tags.getArrayEventTypes().entrySet())
                {
                    String tag = entry.getKey();
                    if (!filterTypes.containsKey(tag))
                    {
                        Pair<EventType, String> pair = new Pair<EventType, String>(arrayTagCompositeEventType, tag);
                        filterTypes.put(tag, pair);
                        arrayCompositeEventTypes.put(tag, pair);
                    }
                }
            }

            StreamTypeService streamTypeService = new StreamTypeServiceImpl(filterTypes, context.getEngineURI(), true, false);
            List<ExprNode> exprNodes = filterNode.getRawFilterSpec().getFilterExpressions();

            FilterSpecCompiled spec = FilterSpecCompiler.makeFilterSpec(resolvedEventType, eventName, exprNodes, filterNode.getRawFilterSpec().getOptionalPropertyEvalSpec(),  filterTaggedEventTypes, arrayCompositeEventTypes, streamTypeService, context.getMethodResolutionService(), context.getSchedulingService(), context.getVariableService(), context.getEventAdapterService(), context.getEngineURI(), null, context);
            filterNode.setFilterSpec(spec);
        }
        else if (evalNode instanceof EvalObserverNode)
        {
            EvalObserverNode observerNode = (EvalObserverNode) evalNode;
            try
            {
                ObserverFactory observerFactory = context.getPatternResolutionService().create(observerNode.getPatternObserverSpec());

                StreamTypeService streamTypeService = getStreamTypeService(context.getEngineURI(), context.getEventAdapterService(), tags.taggedEventTypes, tags.arrayEventTypes);
                List<ExprNode> validated = validateExpressions(observerNode.getPatternObserverSpec().getObjectParameters(),
                        streamTypeService, context.getMethodResolutionService(), null, context.getSchedulingService(), context.getVariableService(), context);

                MatchedEventConvertor convertor = new MatchedEventConvertorImpl(tags.taggedEventTypes, tags.arrayEventTypes, context.getEventAdapterService());

                observerNode.setObserverFactory(observerFactory);
                observerFactory.setObserverParameters(validated, convertor);
            }
            catch (ObserverParameterException e)
            {
                throw new ExprValidationException("Invalid parameter for pattern observer: " + e.getMessage(), e);
            }
            catch (PatternObjectException e)
            {
                throw new ExprValidationException("Failed to resolve pattern observer: " + e.getMessage(), e);
            }
        }
        else if (evalNode instanceof EvalGuardNode)
        {
            EvalGuardNode guardNode = (EvalGuardNode) evalNode;
            try
            {
                GuardFactory guardFactory = context.getPatternResolutionService().create(guardNode.getPatternGuardSpec());

                StreamTypeService streamTypeService = getStreamTypeService(context.getEngineURI(), context.getEventAdapterService(), tags.taggedEventTypes, tags.arrayEventTypes);
                List<ExprNode> validated = validateExpressions(guardNode.getPatternGuardSpec().getObjectParameters(),
                        streamTypeService, context.getMethodResolutionService(), null, context.getSchedulingService(), context.getVariableService(), context);

                MatchedEventConvertor convertor = new MatchedEventConvertorImpl(tags.taggedEventTypes, tags.arrayEventTypes, context.getEventAdapterService());

                guardNode.setGuardFactory(guardFactory);
                guardFactory.setGuardParameters(validated, convertor);
            }
            catch (GuardParameterException e)
            {
                throw new ExprValidationException("Invalid parameter for pattern guard: " + e.getMessage(), e);
            }
            catch (PatternObjectException e)
            {
                throw new ExprValidationException("Failed to resolve pattern guard: " + e.getMessage(), e);
            }
        }
        else if (evalNode instanceof EvalEveryDistinctNode)
        {
            EvalEveryDistinctNode distinctNode = (EvalEveryDistinctNode) evalNode;
            MatchEventSpec matchEventFromChildNodes = analyzeMatchEvent(distinctNode);
            StreamTypeService streamTypeService = getStreamTypeService(context.getEngineURI(), context.getEventAdapterService(), matchEventFromChildNodes.getTaggedEventTypes(), matchEventFromChildNodes.getArrayEventTypes());
            List<ExprNode> validated;
            try
            {
                validated = validateExpressions(distinctNode.getExpressions(),
                    streamTypeService, context.getMethodResolutionService(), null, context.getSchedulingService(), context.getVariableService(), context);
            }
            catch (ExprValidationPropertyException ex)
            {
                throw new ExprValidationPropertyException(ex.getMessage() + ", every-distinct requires that all properties resolve from sub-expressions to the every-distinct", ex.getCause());
            }

            MatchedEventConvertor convertor = new MatchedEventConvertorImpl(matchEventFromChildNodes.getTaggedEventTypes(), matchEventFromChildNodes.getArrayEventTypes(), context.getEventAdapterService());

            distinctNode.setConvertor(convertor);

            // Determine whether some expressions are constants or time period
            List<ExprNode> distinctExpressions = new ArrayList<ExprNode>();
            Long msecToExpire = null;
            for (ExprNode expr : validated) {
                if (expr instanceof ExprTimePeriod) {
                    Double secondsExpire = (Double) ((ExprTimePeriod) expr).evaluate(null, true, context);
                    if ((secondsExpire != null) && (secondsExpire > 0)) {
                        msecToExpire = Math.round(1000d * secondsExpire);
                    }
                    log.debug("Setting every-distinct msec-to-expire to " + msecToExpire);
                }
                else if (expr.isConstantResult()) {
                    log.warn("Every-distinct node utilizes an expression returning a constant value, please check expression '" + expr.toExpressionString() + "', not adding expression to distinct-value expression list");
                }
                else {
                    distinctExpressions.add(expr);
                }
            }
            if (distinctExpressions.isEmpty()) {
                throw new ExprValidationException("Every-distinct node requires one or more distinct-value expressions that each return non-constant result values");
            }
            distinctNode.setExpressions(distinctExpressions, msecToExpire);
        }
        else if (evalNode instanceof EvalMatchUntilNode)
        {
            EvalMatchUntilNode matchUntilNode = (EvalMatchUntilNode) evalNode;

            // compile bounds expressions, if any
            MatchEventSpec untilMatchEventSpec = new MatchEventSpec(tags.getTaggedEventTypes(), tags.getArrayEventTypes());
            StreamTypeService streamTypeService = getStreamTypeService(context.getEngineURI(), context.getEventAdapterService(), untilMatchEventSpec.getTaggedEventTypes(), untilMatchEventSpec.getArrayEventTypes());

            String message = "Match-until bounds value expressions must return a numeric value";
            if (matchUntilNode.getLowerBounds() != null) {
                ExprNode validated = matchUntilNode.getLowerBounds().getValidatedSubtree(streamTypeService, context.getMethodResolutionService(), null, context.getSchedulingService(), context.getVariableService(), context);
                matchUntilNode.setLowerBounds(validated);
                if ((validated.getExprEvaluator().getType() == null) || (!JavaClassHelper.isNumeric(validated.getExprEvaluator().getType()))) {
                    throw new ExprValidationException(message);
                }
            }

            if (matchUntilNode.getUpperBounds() != null) {
                ExprNode validated = matchUntilNode.getUpperBounds().getValidatedSubtree(streamTypeService, context.getMethodResolutionService(), null, context.getSchedulingService(), context.getVariableService(), context);
                matchUntilNode.setUpperBounds(validated);
                if ((validated.getExprEvaluator().getType() == null) || (!JavaClassHelper.isNumeric(validated.getExprEvaluator().getType()))) {
                    throw new ExprValidationException(message);
                }
            }

            MatchedEventConvertor convertor = new MatchedEventConvertorImpl(untilMatchEventSpec.getTaggedEventTypes(), untilMatchEventSpec.getArrayEventTypes(), context.getEventAdapterService());
            matchUntilNode.setConvertor(convertor);

            // compile new tag lists
            Set<String> arrayTags = null;
            EvalNodeAnalysisResult matchUntilAnalysisResult = EvalNode.recursiveAnalyzeChildNodes(matchUntilNode.getChildNodes().get(0));
            for (EvalFilterNode filterNode : matchUntilAnalysisResult.getFilterNodes())
            {
                String optionalTag = filterNode.getEventAsName();
                if (optionalTag != null)
                {
                    if (arrayTags == null)
                    {
                        arrayTags = new HashSet<String>();
                    }
                    arrayTags.add(optionalTag);
                }
            }

            if (arrayTags != null)
            {
                for (String arrayTag : arrayTags)
                {
                    if (!tags.arrayEventTypes.containsKey(arrayTag))
                    {
                        tags.arrayEventTypes.put(arrayTag, tags.taggedEventTypes.get(arrayTag));
                        tags.taggedEventTypes.remove(arrayTag);
                    }
                }
            }
            matchUntilNode.setTagsArrayedSet(arrayTags);
        }
        else if (evalNode instanceof EvalFollowedByNode)
        {
            EvalFollowedByNode followedByNode = (EvalFollowedByNode) evalNode;
            StreamTypeService streamTypeService = new StreamTypeServiceImpl(context.getEngineURI(), false);

            if (followedByNode.getOptionalMaxExpressions() != null) {
                List<ExprNode> validated = new ArrayList<ExprNode>();
                for (ExprNode maxExpr : followedByNode.getOptionalMaxExpressions()) {
                    if (maxExpr == null) {
                        validated.add(null);
                    }
                    else {
                        ExprNodeSummaryVisitor visitor = new ExprNodeSummaryVisitor();
                        maxExpr.accept(visitor);
                        if (!visitor.isPlain())
                        {
                            String errorMessage = "Invalid maximum expression in followed-by, " + visitor.getMessage() + " are not allowed within the expression";
                            log.error(errorMessage);
                            throw new ExprValidationException(errorMessage);
                        }

                        ExprNode validatedExpr = maxExpr.getValidatedSubtree(streamTypeService, context.getMethodResolutionService(), null, context.getSchedulingService(), context.getVariableService(), context);
                        validated.add(validatedExpr);
                        if ((validatedExpr.getExprEvaluator().getType() == null) || (!JavaClassHelper.isNumeric(validatedExpr.getExprEvaluator().getType()))) {
                            String message = "Invalid maximum expression in followed-by, the expression must return an integer value";
                            throw new ExprValidationException(message);
                        }
                    }
                }
                followedByNode.setOptionalMaxExpressions(validated);
            }
        }

        if (newTaggedEventTypes != null)
        {
            tags.getTaggedEventTypes().putAll(newTaggedEventTypes);
        }
        if (newArrayEventTypes != null)
        {
            tags.getArrayEventTypes().putAll(newArrayEventTypes);
        }
    }

    private static List<ExprNode> validateExpressions(List<ExprNode> objectParameters, StreamTypeService streamTypeService,
                         MethodResolutionService methodResolutionService,
                         ViewResourceDelegate viewResourceDelegate,
                         TimeProvider timeProvider,
                         VariableService variableService,
                         ExprEvaluatorContext exprEvaluatorContext)
            throws ExprValidationException
    {
        if (objectParameters == null)
        {
            return objectParameters;
        }
        List<ExprNode> validated = new ArrayList<ExprNode>();
        for (ExprNode node : objectParameters)
        {
            validated.add(node.getValidatedSubtree(streamTypeService, methodResolutionService, viewResourceDelegate, timeProvider, variableService, exprEvaluatorContext));
        }
        return validated;
    }

    private static StreamTypeService getStreamTypeService(String engineURI, EventAdapterService eventAdapterService, Map<String, Pair<EventType, String>> taggedEventTypes, Map<String, Pair<EventType, String>> arrayEventTypes)
    {
        LinkedHashMap<String, Pair<EventType, String>> filterTypes = new LinkedHashMap<String, Pair<EventType, String>>();
        filterTypes.putAll(taggedEventTypes);

        // handle array tags (match-until clause)
        if (arrayEventTypes != null)
        {
            EventType arrayTagCompositeEventType = eventAdapterService.createSemiAnonymousMapType(new HashMap(), arrayEventTypes, false);
            for (Map.Entry<String, Pair<EventType, String>> entry : arrayEventTypes.entrySet())
            {
                String tag = entry.getKey();
                if (!filterTypes.containsKey(tag))
                {
                    Pair<EventType, String> pair = new Pair<EventType, String>(arrayTagCompositeEventType, tag);
                    filterTypes.put(tag, pair);
                }
            }
        }

        return new StreamTypeServiceImpl(filterTypes, engineURI, true, false);
    }

    private static MatchEventSpec analyzeMatchEvent(EvalNode relativeNode)
    {
        LinkedHashMap<String, Pair<EventType, String>> taggedEventTypes = new LinkedHashMap<String, Pair<EventType, String>>();
        LinkedHashMap<String, Pair<EventType, String>> arrayEventTypes = new LinkedHashMap<String, Pair<EventType, String>>();

        // Determine all the filter nodes used in the pattern
        EvalNodeAnalysisResult evalNodeAnalysisResult = EvalNode.recursiveAnalyzeChildNodes(relativeNode);

        // collect all filters underneath
        for (EvalFilterNode filterNode : evalNodeAnalysisResult.getFilterNodes())
        {
            String optionalTag = filterNode.getEventAsName();
            if (optionalTag != null)
            {
                taggedEventTypes.put(optionalTag, new Pair<EventType, String>(filterNode.getFilterSpec().getFilterForEventType(), filterNode.getFilterSpec().getFilterForEventTypeName()));
            }
        }

        // collect those filters under a repeat since they are arrays
        Set<String> arrayTags = new HashSet<String>();
        for (EvalMatchUntilNode matchUntilNode : evalNodeAnalysisResult.getRepeatNodes())
        {
            EvalNodeAnalysisResult matchUntilAnalysisResult = EvalNode.recursiveAnalyzeChildNodes(matchUntilNode.getChildNodes().get(0));
            for (EvalFilterNode filterNode : matchUntilAnalysisResult.getFilterNodes())
            {
                String optionalTag = filterNode.getEventAsName();
                if (optionalTag != null)
                {
                    arrayTags.add(optionalTag);
                }
            }
        }

        // for each array tag change collection
        for (String arrayTag : arrayTags)
        {
            if (taggedEventTypes.get(arrayTag) != null)
            {
                arrayEventTypes.put(arrayTag, taggedEventTypes.get(arrayTag));
                taggedEventTypes.remove(arrayTag);
            }
        }

        return new MatchEventSpec(taggedEventTypes, arrayEventTypes);
    }

    private static class MatchEventSpec
    {
        private final LinkedHashMap<String, Pair<EventType, String>> taggedEventTypes;
        private final LinkedHashMap<String, Pair<EventType, String>> arrayEventTypes;

        private MatchEventSpec(LinkedHashMap<String, Pair<EventType, String>> taggedEventTypes, LinkedHashMap<String, Pair<EventType, String>> arrayEventTypes)
        {
            this.taggedEventTypes = taggedEventTypes;
            this.arrayEventTypes = arrayEventTypes;
        }

        public MatchEventSpec()
        {
            this.taggedEventTypes = new LinkedHashMap<String, Pair<EventType, String>>();
            this.arrayEventTypes = new LinkedHashMap<String, Pair<EventType, String>>();
        }

        public LinkedHashMap<String, Pair<EventType, String>> getArrayEventTypes()
        {
            return arrayEventTypes;
        }

        public LinkedHashMap<String, Pair<EventType, String>> getTaggedEventTypes()
        {
            return taggedEventTypes;
        }
    }
}
