package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.agg.*;
import com.espertech.esper.epl.core.MethodResolutionService;

public class ExprPlugInAggFunctionNodeFactory implements AggregationMethodFactory
{
    private final AggregationSupport aggregationSupport;
    private final boolean distinct;
    private final Class aggregatedValueType;

    public ExprPlugInAggFunctionNodeFactory(AggregationSupport aggregationSupport, boolean distinct, Class aggregatedValueType)
    {
        this.aggregationSupport = aggregationSupport;
        this.distinct = distinct;
        this.aggregatedValueType = aggregatedValueType;
    }

    public Class getResultType()
    {
        return aggregationSupport.getValueType();
    }

    public AggregationSpec getSpec(boolean isMatchRecognize)
    {
        return null;  // defaults apply
    }

    public AggregationMethod getPrototypeAggregator(MethodResolutionService methodResolutionService)
    {
        AggregationMethod method = aggregationSupport;
        if (!distinct) {
            return method;
        }
        return methodResolutionService.makeDistinctAggregator(method, aggregatedValueType);
    }

    public AggregationAccessor getAccessor()
    {
        return null;  // no accessor
    }
}
