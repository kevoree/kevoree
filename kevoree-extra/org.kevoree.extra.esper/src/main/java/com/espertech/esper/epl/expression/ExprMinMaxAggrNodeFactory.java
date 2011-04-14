package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.agg.*;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.type.MinMaxTypeEnum;

public class ExprMinMaxAggrNodeFactory implements AggregationMethodFactory
{
    private final MinMaxTypeEnum minMaxTypeEnum;
    private final Class type;
    private final boolean hasDataWindows;
    private final boolean distinct;

    public ExprMinMaxAggrNodeFactory(MinMaxTypeEnum minMaxTypeEnum, Class type, boolean hasDataWindows, boolean distinct)
    {
        this.minMaxTypeEnum = minMaxTypeEnum;
        this.type = type;
        this.hasDataWindows = hasDataWindows;
        this.distinct = distinct;
    }

    public AggregationAccessor getAccessor()
    {
        return null;
    }

    public AggregationMethod getPrototypeAggregator(MethodResolutionService methodResolutionService)
    {
        AggregationMethod method = methodResolutionService.makeMinMaxAggregator(minMaxTypeEnum, type, hasDataWindows);
        if (!distinct) {
            return method;
        }
        return methodResolutionService.makeDistinctAggregator(method, type);
    }

    public AggregationSpec getSpec(boolean isMatchRecognize)
    {
        return null;  // defaults apply
    }

    public Class getResultType()
    {
        return type;
    }
}
