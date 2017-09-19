package org.kevoree.core;

import org.kevoree.*;
import org.kevoree.adaptation.AdaptationCommand;
import org.kevoree.adaptation.AdaptationExecutor;
import org.kevoree.adaptation.AdaptationType;
import org.kevoree.adaptation.KevoreeAdaptationException;
import org.kevoree.annotation.KevoreeInject;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.NodeType;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.api.handler.UpdateContext;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.ModelCloner;
import org.kevoree.modeling.api.util.ActionType;
import org.kevoree.reflect.ReflectUtils;
import org.kevoree.service.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

/**
 *
 * Created by duke on 9/26/14.
 */
public class KevoreeCoreImpl implements KevoreeCore {

    private boolean bootDone = false;
    private String nodeName;
    private ContainerRoot proposedModel;
    private ListenerInvoker modelListeners;
    private CallbackHandler onStopHandler;
    private NodeType nodeInstance;
    private KevoreeFactory factory;
    private ContainerRoot currentModel;
    private ExecutorService scheduler;
    private ModelCloner modelCloner;

    @KevoreeInject
    private KevScriptService kevscript;

    @KevoreeInject
    private RuntimeService runtime;

    public KevoreeCoreImpl() {
        this.factory = new DefaultKevoreeFactory();
        this.modelListeners = new ListenerInvoker();
        this.modelCloner = factory.createModelCloner();
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public KevoreeFactory getFactory() {
        return factory;
    }

    @Override
    public ContainerRoot getProposedModel() {
        return proposedModel;
    }

    @Override
    public void registerModelListener(ModelListener listener, String callerPath) {
        if (modelListeners != null) {
            modelListeners.addListener(listener);
        }
    }

    @Override
    public void unregisterModelListener(ModelListener listener, String callerPath) {
        if (modelListeners != null) {
            modelListeners.removeListener(listener);
        }
    }

    @Override
    public ContainerRoot getCurrentModel() {
        return currentModel;
    }

    @Override
    public void update(ContainerRoot model, UUID uuid, UpdateCallback callback, String callerPath) {
        if (scheduler != null) {
            scheduler.submit(new UpdateModelRunnable(model, callback, uuid, callerPath));
        } else {
            Log.debug("Ignoring update because the core is stopping");
        }
    }

    @Override
    public void submitScript(String script, UUID uuid, UpdateCallback callback, String callerPath) {
        if (scheduler != null) {
            scheduler.submit(new UpdateScriptRunnable(script, callback, uuid, callerPath));
        } else {
            Log.debug("Ignoring script submission because the core is stopping");
        }
    }

    private void internalUpdateModel(ContainerRoot proposedModel, UUID uuid, String callerPath)
            throws KevoreeDeployException {
        monkeyPatchKMF(proposedModel);

        if (proposedModel.findNodesByID(getNodeName()) == null) {
            throw new KevoreeDeployException("Unable to find node \""+getNodeName()+"\" in given model");
        }

        try {
            this.proposedModel = proposedModel;
            ContainerRoot currentModel = this.currentModel;
            final UpdateContext updateContext = new UpdateContextImpl(currentModel, proposedModel, uuid, callerPath);
            Log.trace("KevoreeCoreImpl#modelListeners preUpdate");
            boolean preUpdateAccepted = modelListeners.preUpdate(updateContext)
                    .get(5, TimeUnit.SECONDS);
            Log.trace("KevoreeCoreImpl#modelListeners preUpdate = {}", preUpdateAccepted);

            if (preUpdateAccepted) {
                // Checks and bootstrap the node
                checkBootstrapNode(proposedModel);
                Log.debug("Begin model update");
                if (nodeInstance != null) {
                    // Compare the two models and plan the adaptation
                    Log.trace("Comparing models and planning adaptations");
                    List<AdaptationCommand> cmds = nodeInstance.plan(currentModel, proposedModel);

                    // Looking for Hara-Kiri case
                    boolean harakiri = false;
                    Optional<AdaptationCommand> stopNodeCmd = cmds
                            .stream()
                            .filter(cmd -> cmd.getType().equals(AdaptationType.STOP_INSTANCE)
                                    && cmd.getElement().path().equals("/nodes[" + nodeName + "]"))
                            .findFirst();
                    if (stopNodeCmd.isPresent()) {
                        // adaptations contains a StopCommand for current node instance => Hara-Kiri
                        cmds.remove(stopNodeCmd.get());
                        harakiri = true;
                    }

                    // Execution of the adaptations
                    Log.trace("Launching adaptation of the system...");
                    AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);
                    if (result.getError() == null) {
                        // everything went fine
                        if (!proposedModel.isReadOnly()) {
                            proposedModel.setRecursiveReadOnly();
                        }
                        // Changes the current model with the proposed model
                        this.bootDone = true;
                        this.currentModel = proposedModel;

                        // Fires the update to listeners
                        Log.trace("KevoreeCoreImpl#modelListeners updateSuccess");
                        modelListeners.updateSuccess(updateContext);
                        Log.trace("KevoreeCoreImpl#modelListeners updateSuccess done");

                        // Handle HaraKiri if needed
                        if (harakiri) {
                            this.stop();
                        }
                    } else {
                        Log.error("Something went wrong during update", result.getError());
                        Log.warn("Starting rollback phase...");

                        Log.trace("KevoreeCoreImpl#modelListeners updateError");
                        modelListeners.updateError(updateContext, result.getError());
                        Log.trace("KevoreeCoreImpl#modelListeners updateError done");

                        // something went wrong while deploying proposed model: rollback
                        if (bootDone) {
                            Log.trace("KevoreeCoreImpl#modelListeners preRollback");
                            boolean preRollbackAccepted = modelListeners.preRollback(updateContext)
                                    .get(5, TimeUnit.SECONDS);
                            Log.trace("KevoreeCoreImpl#modelListeners preRollback = {}", preRollbackAccepted);

                            if (preRollbackAccepted) {
                                try {
                                    // rollback already executed commands
                                    AdaptationExecutor.undo(result.getExecutedCmds());

                                    Log.trace("KevoreeCoreImpl#modelListeners rollbackSuccess");
                                    modelListeners.rollbackSuccess(updateContext);
                                    Log.trace("KevoreeCoreImpl#modelListeners rollbackSuccess done");
                                } catch (KevoreeAdaptationException e) {
                                    Log.error("Something went wrong during rollback phase", e);
                                    Log.trace("KevoreeCoreImpl#modelListeners rollbackError");
                                    modelListeners.rollbackError(updateContext, e);
                                    Log.trace("KevoreeCoreImpl#modelListeners rollbackError done");
                                    stop();
                                }
                            } else {
                                // TODO what does it mean to refuse a rollback? Do we leave Kevoree in the current state
                                // no matter what happened? Do we stop?
                                Log.trace("KevoreeCoreImpl#modelListeners preRollbackRefused");
                                modelListeners.preRollbackRefused(updateContext);
                                Log.trace("KevoreeCoreImpl#modelListeners preRollbackRefused done");
                                throw new KevoreeDeployException("preRollback() refused: aborting rollback");
                            }
                        } else {
                            // this is still the boot phase and something went wrong...
                            // there is nothing we can do here => just stop the whole runtime
                            throw result.getError();
                        }
                    }
                } else {
                    throw new KevoreeDeployException("Unable to initialize node \"" + getNodeName() + "\"");
                }
            } else {
                Log.trace("KevoreeCoreImpl#modelListeners preUpdateRefused");
                modelListeners.preUpdateRefused(updateContext);
                Log.trace("KevoreeCoreImpl#modelListeners preUpdateRefused done");
                throw new KevoreeDeployException("preUpdate() refused: aborting deployment");
            }
        } catch (KevoreeAdaptationException e) {
            throw new KevoreeDeployException("Unable to deploy proposed model", e);
        } catch (KevoreeDeployException e) {
            throw e;
        } catch (InterruptedException e) {
            throw new KevoreeDeployException("Interrupted while waiting for preUpdate()", e);
        } catch (TimeoutException e) {
            throw new KevoreeDeployException("preUpdate() timed out", e);
        } catch (Exception e) {
            throw new KevoreeDeployException("Internal update failed", e);
        }
    }

    private NodeType bootstrapNodeType(ContainerRoot model, String nodeName) throws KevoreeDeployException {
        ContainerNode node = model.findNodesByID(nodeName);
        if (node != null) {
            try {
                ClassLoader classLoader = runtime.installTypeDefinition(node);
                NodeType nodeObject = (NodeType) runtime.createInstance(node, classLoader);
                if (node.getTypeDefinition().getDictionaryType() != null) {
                    for (DictionaryAttribute attr : node.getTypeDefinition().getDictionaryType().getAttributes()) {
                        if (!attr.getFragmentDependant()) {
                            ContainerNode currentNode = currentModel.findNodesByID(nodeName);
                            if (currentNode != null) {
                                Value proposedVal = node.getDictionary().findValuesByID(attr.getName());
                                if (proposedVal == null) {
                                    Value param = factory.createValue();
                                    param.setName(attr.getName());
                                    param.setValue(attr.getDefaultValue());
                                    node.getDictionary().addValues(param);
                                    Log.debug("Set default node param: {} = '{}'", param.getName(), param.getValue());
                                }
                            }
                        }
                    }
                }
                return nodeObject;
            } catch (ClassCastException e) {
                throw new KevoreeDeployException("You are trying to start a NodeType that does not target this runtime version", e);
            } catch (KevoreeCoreException e) {
                throw new KevoreeDeployException("Unable to create instance " + node.getName() + ": " + node.getTypeDefinition().getName() + "/" + node.getTypeDefinition().getVersion() + "/" + node.getTypeDefinition().getDeployUnits().get(0).getVersion(), e);
            }
        } else {
            throw new KevoreeDeployException("Unable to find a node named \"" + nodeName + "\" in model");
        }
    }

    private void checkBootstrapNode(ContainerRoot currentModel) throws KevoreeDeployException {
        try {
            if (nodeInstance == null) {
                Log.debug("Bootstrapping node \"{}\"", getNodeName());
                nodeInstance = bootstrapNodeType(currentModel, getNodeName());
                if (nodeInstance != null) {
                    Method met = ReflectUtils.findMethodWithAnnotation(nodeInstance.getClass(), Start.class);
                    if (met != null) {
                        met.invoke(nodeInstance);
                    }
                    this.currentModel = factory.createContainerRoot();
                } else {
                    Log.error("Unable to bootstrap node \"" + getNodeName() + "\"");
                    this.stop();
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new KevoreeDeployException("Error while invoking node instance \"" + getNodeName() + "\" @Start method", e);
        }
    }

    private String hash(String str) {
        int val = 0;
        if (str.isEmpty()) {
            return String.valueOf(val);
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            val = ((val<<5) - val) + c;
        }

        return String.valueOf(val & 0xfffffff);
    }

    private void monkeyPatchKMF(ContainerRoot proposedModel) {
        for (MBinding possibleBinding : proposedModel.getmBindings()) {
            String chanPath = (possibleBinding.getHub() != null ? possibleBinding.getHub().path() : "UNDEFINED");
            String portPath = (possibleBinding.getPort() != null ? possibleBinding.getPort().path() : "UNDEFINED");
            String hash = hash(chanPath + "_" + portPath);
            possibleBinding.reflexiveMutator(ActionType.SET, "generated_KMF_ID", hash, true, false);
        }
    }

    public void start() {
        if (getNodeName() == null || getNodeName().equals("")) {
            setNodeName("node0");
        }
        final ThreadFactory threadFactory = new KevoreeThreadFactory(getNodeName());
        modelListeners.start(threadFactory);
        scheduler = Executors.newSingleThreadExecutor(threadFactory);
        currentModel = factory.createContainerRoot();
        factory.root(currentModel);
        Log.debug("Kevoree core started");
    }

    public boolean isStarted() {
        return scheduler != null && !scheduler.isShutdown();
    }

    public void stop() {
        Log.info("Stopping Kevoree...");
        modelListeners.stop();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        if (nodeInstance != null) {
            if (currentModel.findNodesByID(getNodeName()) != null) {
                ContainerRoot stopModel = modelCloner.clone(currentModel);
                ContainerNode nodeToStop = stopModel.findNodesByID(getNodeName());
                nodeToStop.setStarted(false);
                for (ContainerNode childNode : nodeToStop.getHosts()) {
                    childNode.setStarted(false);
                }
                for (Group group : stopModel.getGroups()) {
                    group.setStarted(false);
                }
                for (Channel hub : stopModel.getHubs()) {
                    hub.setStarted(false);
                }
                for (ComponentInstance childComponent : nodeToStop.getComponents()) {
                    childComponent.setStarted(false);
                }
                try {
                    List<AdaptationCommand> cmds = nodeInstance.plan(currentModel, stopModel);
                    AdaptationExecutor.forceExecute(cmds);
                    // TODO whether print errors to stderr or in a file?
                } catch (KevoreeAdaptationException e) {
                    Log.error("Node instance "+getNodeName()+" did not manage to HaraKiri adaptations", e);
                }
            }

            try {
                Method met = ReflectUtils.findMethodWithAnnotation(nodeInstance.getClass(), Stop.class);
                if (met != null) {
                    met.invoke(nodeInstance);
                }
            } catch (Exception e) {
                Log.error("Something went wrong while trying to invoke @Stop on node", e);
            }
        }

        if (this.onStopHandler != null) {
            this.onStopHandler.execute();
        }
    }

    @Override
    public void onStop(CallbackHandler handler) {
        this.onStopHandler = handler;
    }

    private class UpdateScriptRunnable implements Runnable {

        private String script;
        private UpdateCallback callback;
        private UUID uuid;
        private String callerPath;

        private UpdateScriptRunnable(String script, UpdateCallback callback, UUID uuid, String callerPath) {
            this.script = script;
            this.callback = callback;
            this.uuid = uuid;
            this.callerPath = callerPath;
        }

        @Override
        public void run() {
            KevoreeDeployException error = null;
            long startTime = System.currentTimeMillis();
            try {
                ContainerRoot clonedModel = modelCloner.clone(currentModel, false);
                kevscript.execute(script, clonedModel);
                Log.info("Script executed successfully ({}ms)", System.currentTimeMillis() - startTime);
                startTime = System.currentTimeMillis();
                internalUpdateModel(clonedModel, uuid, callerPath);
                Log.info("Model deployed successfully ({}ms)", System.currentTimeMillis() - startTime);
            } catch (KevoreeDeployException e) {
                error = e;
            } catch (KevScriptException e) {
                error = new KevoreeDeployException(e);
            }

            if (error != null) {
                Log.error("Something went wrong during update ({}ms)", error, System.currentTimeMillis() - startTime);
            }
            callback.run(error);
        }
    }

    private class UpdateModelRunnable implements Runnable {

        private ContainerRoot targetModel;
        private UpdateCallback callback;
        private UUID uuid;
        private String callerPath;

        UpdateModelRunnable(ContainerRoot targetModel, UpdateCallback callback, UUID uuid, String callerPath) {
            this.targetModel = targetModel;
            this.callback = callback;
            this.uuid = uuid;
            this.callerPath = callerPath;
        }

        @Override
        public void run() {
            KevoreeDeployException error = null;
            long startTime = System.currentTimeMillis();
            try {
                internalUpdateModel(targetModel, uuid, callerPath);
                Log.info("Model deployed successfully ({}ms)", System.currentTimeMillis() - startTime);
            } catch (KevoreeDeployException e) {
                error = e;
            } catch (Exception e) {
                error = new KevoreeDeployException(e);
            }

            if (error != null) {
                Log.error("Something went wrong during update ({}ms)", error, System.currentTimeMillis() - startTime);
            }
            callback.run(error);
        }
    }

    public interface OnStopHandler {
        void execute();
    }
}
