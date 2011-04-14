package com.espertech.esper.pattern;

import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.spec.FilterSpecRaw;
import com.espertech.esper.epl.spec.PatternGuardSpec;
import com.espertech.esper.epl.spec.PatternObserverSpec;

import java.util.List;

public interface PatternNodeFactory {

    public EvalAndNode makeAndNode();
    public EvalEveryDistinctNode makeEveryDistinctNode(List<ExprNode> expressions);
    public EvalEveryNode makeEveryNode();
    public EvalFilterNode makeFilterNode(FilterSpecRaw filterSpecification,String eventAsName);
    public EvalFollowedByNode makeFollowedByNode(List<ExprNode> maxExpressions);
    public EvalGuardNode makeGuardNode(PatternGuardSpec patternGuardSpec);
    public EvalMatchUntilNode makeMatchUntilNode(ExprNode lowerBounds, ExprNode upperBounds);
    public EvalNotNode makeNotNode();
    public EvalObserverNode makeObserverNode(PatternObserverSpec patternObserverSpec);
    public EvalOrNode makeOrNode();
    public EvalRootNode makeRootNode();
}
