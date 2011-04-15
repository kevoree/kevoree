package com.espertech.esper.epl.property;

import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.FragmentEventType;
import com.espertech.esper.epl.core.*;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.spec.*;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.util.UuidGenerator;

import java.util.*;

/**
 * Factory for property evaluators.
 */
public class PropertyEvaluatorFactory
{
    /**
     * Makes the property evaluator.
     * @param spec is the property specification
     * @param sourceEventType the event type
     * @param optionalSourceStreamName the source stream name
     * @param eventAdapterService for event instances
     * @param methodResolutionService for resolving UDF
     * @param timeProvider provides time
     * @param variableService for resolving variables
     * @param engineURI engine URI
     * @return propert evaluator
     * @throws ExprValidationException if any expressions could not be verified
     */
    public static PropertyEvaluator makeEvaluator(PropertyEvalSpec spec,
                                                  EventType sourceEventType,
                                                  String optionalSourceStreamName,
                                                  EventAdapterService eventAdapterService,
                                                  MethodResolutionService methodResolutionService,
                                                  final TimeProvider timeProvider,
                                                  VariableService variableService,
                                                  String engineURI,
                                                  String statementId)
            throws ExprValidationException
    {
        int length = spec.getAtoms().size();
        EventPropertyGetter[] getters = new EventPropertyGetter[length];
        FragmentEventType types[] = new FragmentEventType[length];
        EventType currentEventType = sourceEventType;
        ExprEvaluator whereClauses[] = new ExprEvaluator[length];

        List<EventType> streamEventTypes = new ArrayList<EventType>();
        List<String> streamNames = new ArrayList<String>();
        Map<String, Integer> streamNameAndNumber = new HashMap<String,Integer>();
        List<String> propertyNames = new ArrayList<String>();
        ExprEvaluatorContext validateContext = new ExprEvaluatorContext()
        {
            public TimeProvider getTimeProvider()
            {
                return timeProvider;
            }
        };

        streamEventTypes.add(sourceEventType);
        streamNames.add(optionalSourceStreamName);
        streamNameAndNumber.put(optionalSourceStreamName, 0);
        propertyNames.add(sourceEventType.getName());

        List<SelectClauseElementCompiled> cumulativeSelectClause = new ArrayList<SelectClauseElementCompiled>();
        for (int i = 0; i < length; i++)
        {
            PropertyEvalAtom atom = spec.getAtoms().get(i);

            // obtain property info
            String propertyName = atom.getPropertyName();
            FragmentEventType fragmentEventType = currentEventType.getFragmentType(propertyName);
            if (fragmentEventType == null)
            {
                throw new ExprValidationException("Property expression '" + propertyName + "' against type '" + currentEventType.getName() + "' does not return a fragmentable property value");
            }
            EventPropertyGetter getter = currentEventType.getGetter(propertyName);
            if (getter == null)
            {
                throw new ExprValidationException("Property expression '" + propertyName + "' against type '" + currentEventType.getName() + "' does not return a fragmentable property value");
            }

            // validate where clause, if any
            streamEventTypes.add(fragmentEventType.getFragmentType());
            streamNames.add(atom.getOptionalAsName());
            streamNameAndNumber.put(atom.getOptionalAsName(), i + 1);
            propertyNames.add(atom.getPropertyName());

            if (atom.getOptionalWhereClause() != null)
            {
                EventType[] whereTypes = streamEventTypes.toArray(new EventType[streamEventTypes.size()]);
                String[] whereStreamNames = streamNames.toArray(new String[streamNames.size()]);
                boolean[] isIStreamOnly = new boolean[streamNames.size()];
                Arrays.fill(isIStreamOnly, true);
                StreamTypeService streamTypeService = new StreamTypeServiceImpl(whereTypes, whereStreamNames, isIStreamOnly, engineURI, false);
                whereClauses[i] = atom.getOptionalWhereClause().getValidatedSubtree(streamTypeService, methodResolutionService, null, timeProvider, variableService, validateContext).getExprEvaluator();
            }

            // validate select clause
            if (atom.getOptionalSelectClause() != null)
            {
                EventType[] whereTypes = streamEventTypes.toArray(new EventType[streamEventTypes.size()]);
                String[] whereStreamNames = streamNames.toArray(new String[streamNames.size()]);
                boolean[] isIStreamOnly = new boolean[streamNames.size()];
                Arrays.fill(isIStreamOnly, true);
                StreamTypeService streamTypeService = new StreamTypeServiceImpl(whereTypes, whereStreamNames, isIStreamOnly, engineURI, false);
                for (SelectClauseElementRaw raw : atom.getOptionalSelectClause().getSelectExprList())
                {
                    if (raw instanceof SelectClauseStreamRawSpec)
                    {
                        SelectClauseStreamRawSpec rawStreamSpec = (SelectClauseStreamRawSpec) raw;
                        if (!streamNames.contains(rawStreamSpec.getStreamName()))
                        {
                            throw new ExprValidationException("Property rename '" + rawStreamSpec.getStreamName() + "' not found in path");
                        }
                        SelectClauseStreamCompiledSpec streamSpec = new SelectClauseStreamCompiledSpec(rawStreamSpec.getStreamName(), rawStreamSpec.getOptionalAsName());
                        int streamNumber = streamNameAndNumber.get(rawStreamSpec.getStreamName());
                        streamSpec.setStreamNumber(streamNumber);
                        cumulativeSelectClause.add(streamSpec);
                    }
                    else if (raw instanceof SelectClauseExprRawSpec)
                    {
                        SelectClauseExprRawSpec exprSpec = (SelectClauseExprRawSpec) raw;
                        ExprNode exprCompiled = exprSpec.getSelectExpression().getValidatedSubtree(streamTypeService, methodResolutionService, null, timeProvider, variableService, validateContext);
                        String resultName = exprSpec.getOptionalAsName();
                        if (resultName == null)
                        {
                            resultName = exprCompiled.toExpressionString();
                        }
                        cumulativeSelectClause.add(new SelectClauseExprCompiledSpec(exprCompiled, resultName));

                        String isMinimal = ExprNodeUtility.isMinimalExpression(exprCompiled);
                        if (isMinimal != null)
                        {
                            throw new ExprValidationException("Expression in a property-selection may not utilize " + isMinimal);
                        }
                    }
                    else if (raw instanceof SelectClauseElementWildcard)
                    {
                        // wildcards are stream selects: we assign a stream name (any) and add a stream wildcard select
                        String streamNameAtom = atom.getOptionalAsName();
                        if (streamNameAtom == null)
                        {
                            streamNameAtom = UuidGenerator.generate();
                        }

                        SelectClauseStreamCompiledSpec streamSpec = new SelectClauseStreamCompiledSpec(streamNameAtom, atom.getOptionalAsName());
                        int streamNumber = i + 1;
                        streamSpec.setStreamNumber(streamNumber);
                        cumulativeSelectClause.add(streamSpec);
                    }
                    else
                    {
                        throw new IllegalStateException("Unknown select clause item:" + raw);
                    }
                }
            }            

            currentEventType = fragmentEventType.getFragmentType();
            types[i] = fragmentEventType;
            getters[i] = getter;
        }

        if (cumulativeSelectClause.isEmpty())
        {
            if (length == 1)
            {
                return new PropertyEvaluatorSimple(getters[0], types[0], whereClauses[0], propertyNames.get(0));
            }
            else
            {
                return new PropertyEvaluatorNested(getters, types, whereClauses, propertyNames);
            }
        }
        else
        {
            PropertyEvaluatorAccumulative accumulative = new PropertyEvaluatorAccumulative(getters, types, whereClauses, propertyNames);

            EventType[] whereTypes = streamEventTypes.toArray(new EventType[streamEventTypes.size()]);
            String[] whereStreamNames = streamNames.toArray(new String[streamNames.size()]);
            boolean[] isIStreamOnly = new boolean[streamNames.size()];
            Arrays.fill(isIStreamOnly, true);
            StreamTypeService streamTypeService = new StreamTypeServiceImpl(whereTypes, whereStreamNames, isIStreamOnly, engineURI, false);

            SelectExprProcessor selectExpr = SelectExprProcessorFactory.getProcessor(cumulativeSelectClause, false, null, null, streamTypeService, eventAdapterService, null, null, null, methodResolutionService, validateContext, variableService, timeProvider,engineURI,statementId);
            return new PropertyEvaluatorSelect(selectExpr, accumulative);
        }
    }
}