package com.espertech.esper.pattern;

import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.spec.FilterSpecRaw;
import com.espertech.esper.epl.spec.PatternGuardSpec;
import com.espertech.esper.epl.spec.PatternObserverSpec;

import java.util.List;

public class PatternNodeFactoryImpl implements PatternNodeFactory {

    public EvalAndNode makeAndNode() {
        return new EvalAndNode();
    }

    public EvalEveryDistinctNode makeEveryDistinctNode(List<ExprNode> expressions) {
        return new EvalEveryDistinctNode(expressions);
    }

    public EvalEveryNode makeEveryNode() {
        return new EvalEveryNode();
    }

    public EvalFilterNode makeFilterNode(FilterSpecRaw filterSpecification, String eventAsName) {
        return new EvalFilterNode(filterSpecification, eventAsName);
    }

    public EvalFollowedByNode makeFollowedByNode(List<ExprNode> maxExpressions) {
        return new EvalFollowedByNode(maxExpressions);
    }

    public EvalGuardNode makeGuardNode(PatternGuardSpec patternGuardSpec) {
        return new EvalGuardNode(patternGuardSpec);
    }

    public EvalMatchUntilNode makeMatchUntilNode(ExprNode lowerBounds, ExprNode upperBounds) {
        return new EvalMatchUntilNode(lowerBounds, upperBounds);
    }

    public EvalNotNode makeNotNode() {
        return new EvalNotNode();
    }

    public EvalObserverNode makeObserverNode(PatternObserverSpec patternObserverSpec) {
        return new EvalObserverNode(patternObserverSpec);
    }

    public EvalOrNode makeOrNode() {
        return new EvalOrNode();
    }

    public EvalRootNode makeRootNode() {
        return new EvalRootNode();
    }
}
