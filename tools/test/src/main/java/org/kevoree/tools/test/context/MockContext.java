package org.kevoree.tools.test.context;

import org.kevoree.api.Context;

/**
 *
 * Created by leiko on 1/16/17.
 */
public class MockContext implements Context {

    private ContextType type;
    private String nodeName;
    private String instanceName;

    private MockContext() {}

    @Override
    public String getPath() {
        switch (this.type) {
            case NODE:
                return "/nodes[" + instanceName + "]";

            case GROUP:
                return "/groups[" + instanceName + "]";

            case CHANNEL:
                return "/hubs[" + instanceName + "]";

            case COMPONENT:
                return "/nodes[" + nodeName + "]/components[" + instanceName + "]";

            default:
                return null;
        }
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public String getInstanceName() {
        return instanceName;
    }

    public static class Builder {
        private MockContext context = new MockContext();

        public Builder nodeName(String nodeName) {
            context.nodeName = nodeName;
            return this;
        }

        public Builder instanceName(String instanceName) {
            context.instanceName = instanceName;
            return this;
        }

        public Builder node() {
            assert context.type == null;
            context.type = ContextType.NODE;
            return this;
        }

        public Builder group() {
            assert context.type == null;
            context.type = ContextType.GROUP;
            return this;
        }

        public Builder channel() {
            assert context.type == null;
            context.type = ContextType.CHANNEL;
            return this;
        }

        public Builder component() {
            assert context.type == null;
            context.type = ContextType.COMPONENT;
            return this;
        }

        public MockContext build() {
            assert context.type != null : "Context type must be specified (group, channel, node, component)";
            assert context.nodeName != null : "Context nodeName must be specified";
            assert context.instanceName != null : "Context instanceName must be specified";
            return context;
        }
    }
}
