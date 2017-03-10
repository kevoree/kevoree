package org.kevoree.core;

import org.kevoree.*;
import org.kevoree.adaptation.AdaptationCommand;
import org.kevoree.adaptation.AdaptationExecutor;
import org.kevoree.adaptation.KevoreeAdaptationException;
import org.kevoree.api.KevScriptService;
import org.kevoree.api.NodeType;
import org.kevoree.api.RuntimeService;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.api.handler.UpdateContext;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.ModelCloner;
import org.kevoree.modeling.api.util.ActionType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 * Created by duke on 9/26/14.
 */
public class KevoreeCore implements ContextAwareModelService {

    private boolean bootDone = false;
    private String nodeName;
    private ContainerRoot pending;
    private ListenerInvoker modelListeners;
    private KevScriptService kevscript;
    private OnStopHandler onStopHandler;
    private RuntimeService runtimeService;
    private NodeType nodeInstance;
    private MethodAnnotationResolver resolver;
    private KevoreeFactory factory;
    private ContainerRoot currentModel;
    private ExecutorService scheduler;
    private ModelCloner modelCloner;

    public KevoreeCore(KevScriptService kevscript) {
        this.kevscript = kevscript;
        this.modelListeners = new ListenerInvoker();
        this.factory = new DefaultKevoreeFactory();
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
    public ContainerRoot getPendingModel() {
        return pending;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    private ContainerRoot cloneCurrentModel(ContainerRoot model) {
        return modelCloner.clone(model, true);
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
    public void update(ContainerRoot model, UpdateCallback callback, String callerPath) {
        if (callback == null) {
            callback = (ignore) -> {};
        }
        monkeyPatchKMF(model);
        scheduler.submit(new UpdateModelRunnable(cloneCurrentModel(model), callback, callerPath));
    }

    @Override
    public void submitScript(String script, UpdateCallback callback, String callerPath) {
        if (callback == null) {
            callback = (ignore) -> {};
        }
        scheduler.submit(new UpdateScriptRunnable(script, callback, callerPath));
    }

    private void internalUpdateModel(ContainerRoot proposedNewModel, String callerPath) throws KevoreeDeployException {
        if (proposedNewModel.findNodesByID(getNodeName()) == null) {
            throw new KevoreeDeployException("Unable to find node \""+getNodeName()+"\" in given model");
        }

        try {
            pending = proposedNewModel;
            ContainerRoot currentModel = this.currentModel;
            UpdateContext updateContext = new UpdateContext(currentModel, proposedNewModel, callerPath);
            Log.trace("KevoreeCore#modelListeners preUpdate");
            boolean preUpdateAccepted = modelListeners.preUpdate(updateContext)
                    .get(5, TimeUnit.SECONDS);
            Log.trace("KevoreeCore#modelListeners preUpdate = {}", preUpdateAccepted);

            if (preUpdateAccepted) {
                // Checks and bootstrap the node
                checkBootstrapNode(proposedNewModel);
//                long startTime = System.currentTimeMillis();
                Log.debug("Begin model update");
                if (nodeInstance != null) {
                    // Compare the two models and plan the adaptation
                    Log.trace("Comparing models and planning adaptations");
                    List<AdaptationCommand> cmds = nodeInstance.plan(currentModel, proposedNewModel);
                    updateContext = new UpdateContext(currentModel, proposedNewModel, callerPath);

                    // Execution of the adaptations
                    Log.trace("Launching adaptation of the system...");
                    AdaptationExecutor.Result result = AdaptationExecutor.execute(cmds);
                    if (result.getError() == null) {
                        // everything went fine
                        if (!proposedNewModel.isReadOnly()) {
                            proposedNewModel.setRecursiveReadOnly();
                        }
                        // Changes the current model with the proposed model
                        this.bootDone = true;
                        this.currentModel = proposedNewModel;
//                        long endTime = System.currentTimeMillis() - startTime;
//                        Log.info("New model deployed successfully in {}ms", endTime);

                        // Fires the update to listeners
                        Log.trace("KevoreeCore#modelListeners updateSuccess");
                        modelListeners.updateSuccess(updateContext);
                        Log.trace("KevoreeCore#modelListeners updateSuccess done");
                    } else {
                        Log.error("Something went wrong during update", result.getError());
                        Log.warn("Starting rollback phase...");

                        Log.trace("KevoreeCore#modelListeners updateError");
                        modelListeners.updateError(updateContext, result.getError());
                        Log.trace("KevoreeCore#modelListeners updateError done");

                        // something went wrong while deploying proposed model: rollback
                        if (bootDone) {
                            Log.trace("KevoreeCore#modelListeners preRollback");
                            boolean preRollbackAccepted = modelListeners.preRollback(updateContext)
                                    .get(5, TimeUnit.SECONDS);
                            Log.trace("KevoreeCore#modelListeners preRollback = {}", preRollbackAccepted);

                            if (preRollbackAccepted) {
                                try {
                                    // rollback already executed commands
                                    AdaptationExecutor.undo(result.getExecutedCmds());

                                    Log.trace("KevoreeCore#modelListeners rollbackSuccess");
                                    modelListeners.rollbackSuccess(updateContext);
                                    Log.trace("KevoreeCore#modelListeners rollbackSuccess done");
                                } catch (KevoreeAdaptationException e) {
                                    Log.error("Something went wrong during rollback phase", e);
                                    Log.trace("KevoreeCore#modelListeners rollbackError");
                                    modelListeners.rollbackError(updateContext, e);
                                    Log.trace("KevoreeCore#modelListeners rollbackError done");
                                    stop();
                                }
                            } else {
                                // TODO what does it mean to refuse a rollback? Do we leave Kevoree in the current state
                                // no matter what happened? Do we stop?
                                Log.trace("KevoreeCore#modelListeners preRollbackRefused");
                                modelListeners.preRollbackRefused(updateContext);
                                Log.trace("KevoreeCore#modelListeners preRollbackRefused done");
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
                Log.trace("KevoreeCore#modelListeners preUpdateRefused");
                modelListeners.preUpdateRefused(updateContext);
                Log.trace("KevoreeCore#modelListeners preUpdateRefused done");
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
            throw new KevoreeDeployException(e);
        }
    }

    private NodeType bootstrapNodeType(ContainerRoot model, String nodeName) throws KevoreeDeployException {
        ContainerNode node = model.findNodesByID(nodeName);
        if (node != null) {
            try {
                FlexyClassLoader kcl = runtimeService.installTypeDefinition(node);
                NodeType nodeObject = (NodeType) runtimeService.createInstance(node, kcl);
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
                    resolver = new MethodAnnotationResolver(nodeInstance.getClass());
                    Method met = resolver.resolve(org.kevoree.annotation.Start.class);
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
//            Log.error(, e.getCause());
//            nodeInstance = null;
//            resolver = null;
//            this.stop();
        }
//        } catch (Exception e) {
//            Log.error("Error while bootstrapping node instance \"" + getNodeName() + "\"", e);
//            nodeInstance = null;
//            resolver = null;
//            this.stop();
//        }
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
                    List<KevoreeAdaptationException> errors = AdaptationExecutor.forceExecute(cmds);
                    // TODO whether print errors to stderr or in a file?
                } catch (KevoreeAdaptationException e) {
                    Log.error("Node instance "+getNodeName()+" did not manage to HaraKiri adaptations", e);
                }
            }

            try {
                Method met = resolver.resolve(org.kevoree.annotation.Stop.class);
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

    public void onStop(OnStopHandler onStopHandler) {
        this.onStopHandler = onStopHandler;
    }

    private class UpdateScriptRunnable implements Runnable {

        private String script;
        private UpdateCallback callback;
        private String callerPath;

        private UpdateScriptRunnable(String script, UpdateCallback callback, String callerPath) {
            this.script = script;
            this.callback = callback;
            this.callerPath = callerPath;
        }

        @Override
        public void run() {
            KevoreeDeployException error = null;
            long startTime = System.currentTimeMillis();
            try {
                ContainerRoot clonedModel = cloneCurrentModel(currentModel);
                kevscript.execute(script, clonedModel);
                Log.info("Script executed successfully ({}ms)", System.currentTimeMillis() - startTime);
                internalUpdateModel(clonedModel, callerPath);
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
        private String callerPath;

        UpdateModelRunnable(ContainerRoot targetModel, UpdateCallback callback, String callerPath) {
            this.targetModel = targetModel;
            this.callback = callback;
            this.callerPath = callerPath;
        }

        @Override
        public void run() {
            KevoreeDeployException error = null;
            long startTime = System.currentTimeMillis();
            try {
                internalUpdateModel(targetModel, callerPath);
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
