package com.espertech.esper.epl.lookup;

import java.util.Map;

public class JoinedPropPlan {
    private final Map<String, JoinedPropDesc> joinProps;
    private final boolean mustCoerce;

    public JoinedPropPlan(Map<String, JoinedPropDesc> joinProps, boolean mustCoerce) {
        this.joinProps = joinProps;
        this.mustCoerce = mustCoerce;
    }

    public Map<String, JoinedPropDesc> getJoinProps() {
        return joinProps;
    }

    public boolean isMustCoerce() {
        return mustCoerce;
    }
}
