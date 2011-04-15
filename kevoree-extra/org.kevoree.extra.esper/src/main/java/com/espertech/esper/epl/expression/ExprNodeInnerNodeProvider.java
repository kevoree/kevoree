package com.espertech.esper.epl.expression;

import java.util.List;

public interface ExprNodeInnerNodeProvider {
    public List<ExprNode> getAdditionalNodes();
}
