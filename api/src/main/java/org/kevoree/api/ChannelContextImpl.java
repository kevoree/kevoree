package org.kevoree.api;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 *
 */
public class ChannelContextImpl extends InstanceContext implements ChannelContext {

    private HashMap<String, Port> localInputs   = new HashMap<>();
    private HashMap<String, Port> localOutputs  = new HashMap<>();
    private HashMap<String, Port> remoteInputs  = new HashMap<>();
    private HashMap<String, Port> remoteOutputs = new HashMap<>();

    public ChannelContextImpl(InstanceContext context) {
        super(context.getPath(), context.getNodeName(), context.getInstanceName());
    }

    public void internalAddLocalInput(Port port) {
        this.localInputs.put(port.getPath(), port);
    }

    public void internalAddLocalOutput(Port port) {
        this.localOutputs.put(port.getPath(), port);
    }

    public void internalAddRemoteInput(Port port) {
        this.remoteInputs.put(port.getPath(), port);
    }

    public void internalAddRemoteOutput(Port port) {
        this.remoteOutputs.put(port.getPath(), port);
    }

    public void internalRemovePort(String path) {
        this.localInputs.remove(path);
        this.localOutputs.remove(path);
        this.remoteInputs.remove(path);
        this.remoteOutputs.remove(path);
    }

    @Override
    public Set<Port> getLocalInputs() {
        return this.localInputs.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<Port> getRemoteInputs() {
        return this.remoteInputs.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<Port> getLocalOutputs() {
        return this.localOutputs.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<Port> getRemoteOutputs() {
        return this.remoteOutputs.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<Port> getInputs() {
        Set<Port> inputs = getLocalInputs();
        inputs.addAll(getRemoteInputs());
        return inputs;
    }

    @Override
    public Set<Port> getOutputs() {
        Set<Port> outputs = getLocalOutputs();
        outputs.addAll(getRemoteOutputs());
        return outputs;
    }

    @Override
    public String toString() {
        return String.format("ChannelContext { path='%s', nodeName='%s', instanceName='%s', inputs=[%s], outputs=[%s] }",
                getPath(),
                getNodeName(),
                getInstanceName(),
                getInputs().stream().map(Port::getPath).reduce((p, n) -> p + ", " + n).orElse(""),
                getOutputs().stream().map(Port::getPath).reduce((p, n) -> p + ", " + n).orElse(""));
    }
}
