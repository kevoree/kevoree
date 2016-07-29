package org.kevoree.core.impl;

import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.NodeType;
import org.kevoree.api.PlatformService;
import org.kevoree.api.adaptation.AdaptationModel;
import org.kevoree.api.handler.*;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.api.telemetry.TelemetryListener;
import org.kevoree.core.impl.deploy.PrimitiveCommandExecutionHelper;
import org.kevoree.core.impl.deploy.PrimitiveExecute;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.trace.TraceSequence;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by duke on 9/26/14.
 */
public class KevoreeCoreBean implements ContextAwareModelService, PlatformService {

    ArrayList<TelemetryListener> telemetryListeners = new ArrayList<TelemetryListener>();

    @Override
    public void addTelemetryListener(TelemetryListener l) {
        telemetryListeners.add(l);
    }

    @Override
    public void removeTelemetryListener(TelemetryListener l) {
        telemetryListeners.remove(l);
    }

    public boolean isAnyTelemetryListener() {
        return !telemetryListeners.isEmpty();
    }

    private String nodeName;

    @Override
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }


    public KevoreeFactory getKevoreeFactory() {
        return kevoreeFactory;
    }

    public KevoreeFactory getFactory() {
        return kevoreeFactory;
    }


    public void broadcastTelemetry(TelemetryEvent.Type typeMessage, String message, Throwable stack) {
        if (isAnyTelemetryListener()) {
            String txt_stack = "";
            if (stack != null) {
                try {
                    ByteArrayOutputStream boo = new ByteArrayOutputStream();
                    PrintStream writer = new PrintStream(boo);
                    stack.printStackTrace(writer);
                    boo.flush();
                    boo.close();
                    txt_stack = new String(boo.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            TelemetryEvent event = TelemetryEventImpl.build(getNodeName(), typeMessage, message, txt_stack);
            for (TelemetryListener tl : telemetryListeners) {
                tl.notify(event);
            }
        }
    }

    ContainerRoot pending = null;

    @Override
    public ContainerRoot getPendingModel() {
        return pending;
    }

    KevoreeListeners modelListeners = new KevoreeListeners(this);

    public BootstrapService getBootstrapService() {
        return bootstrapService;
    }

    public void setBootstrapService(BootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    BootstrapService bootstrapService = null;
    NodeType nodeInstance;
    MethodAnnotationResolver resolver;
    LinkedList<UUIDModel> models = new LinkedList<UUIDModel>();
    KevoreeFactory kevoreeFactory = new org.kevoree.factory.DefaultKevoreeFactory();
    AtomicReference<UUIDModel> model = new AtomicReference<UUIDModel>();
    Date lastDate = new Date(System.currentTimeMillis());
    private ExecutorService scheduler;
    private ScheduledExecutorService lockWatchDog;
    private ScheduledFuture futurWatchDog;
    private TupleLockCallBack currentLock;

    private ContainerRoot cloneCurrentModel(ContainerRoot pmodel) {
        return kevoreeFactory.createModelCloner().clone(pmodel, true);
    }

    @Override
    public void registerModelListener(ModelListener listener, String callerPath) {
        modelListeners.addListener(listener);
    }

    @Override
    public void unregisterModelListener(ModelListener listener, String callerPath) {
        modelListeners.removeListener(listener);
    }


    private class UpdateModelRunnable implements Runnable {

        ContainerRoot targetModel;
        UUID uuid;
        UpdateCallback callback;
        String callerPath;

        public UpdateModelRunnable(ContainerRoot targetModel, UUID uuid, UpdateCallback callback, String callerPath) {
            this.targetModel = targetModel;
            this.uuid = uuid;
            this.callback = callback;
            this.callerPath = callerPath;
        }

        @Override
        public void run() {
            boolean res;
            if (currentLock != null) {
                if (uuid.compareTo(currentLock.getUuid()) == 0) {
                    res = internal_update_model(targetModel, callerPath);
                } else {
                    Log.debug("Core Locked , bad UUID {}", uuid);
                    res = false; //LOCK REFUSED !
                }
            } else {
                //COMMON CHECK
                if (uuid != null) {
                    if (uuid.compareTo(model.get().getUUID()) == 0) {
                        res = internal_update_model(targetModel, callerPath);
                    } else {
                        res = false;
                    }
                } else {
                    res = internal_update_model(targetModel, callerPath);
                }
            }
            final boolean finalRes = res;
            if (callback!= null) {
                callback.run(finalRes);
            }
        }
    }

    @Override
    public UUIDModel getCurrentModel() {
        return model.get();
    }

    @Override
    public void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback, String callerPath) {
        scheduler.submit(new UpdateModelRunnable(cloneCurrentModel(model), uuid, callback, callerPath));
    }

    @Override
    public void update(ContainerRoot model, UpdateCallback callback, String callerPath) {
        scheduler.submit(new UpdateModelRunnable(cloneCurrentModel(model), null, callback, callerPath));
    }

    KevScriptEngine scriptEngine = new KevScriptEngine();

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
            // TODO throwable should be returned in UpdateCallback as an error parameter to distinguish error from "false" result
            try {
                ContainerRoot newModel = kevoreeFactory.createModelCloner().clone(model.get().getModel(), false);
                scriptEngine.execute(script, newModel);
                callback.run(internal_update_model(cloneCurrentModel(newModel), callerPath));
            } catch (Throwable e) {
                Log.error("Error while applying submitted KevScript", e);
                callback.run(false);
            }
        }
    }

    @Override
    public void submitScript(String script, UpdateCallback callback, String callerPath) {
        scheduler.submit(new UpdateScriptRunnable(script, callback, callerPath));
    }

    private class UpdateSequenceRunnable implements Runnable {

        private TraceSequence sequence;

        private UpdateSequenceRunnable(TraceSequence sequence, UpdateCallback callback, String callerPath) {
            this.sequence = sequence;
            this.callback = callback;
            this.callerPath = callerPath;
        }

        private UpdateCallback callback;
        private String callerPath;

        @Override
        public void run() {
            try {
                ContainerRoot newModel = kevoreeFactory.createModelCloner().clone(model.get().getModel(), false);
                sequence.applyOn(newModel);
                callback.run(internal_update_model(cloneCurrentModel(newModel), callerPath));
            } catch (Throwable e) {
                broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while applying trace sequence.", e);
                //Log.error("error while apply trace sequence", e)
                Log.error("Error while applying submitted traces sequence", e);
                callback.run(false);
            }
        }
    }

    public void submitSequence(TraceSequence sequence, UpdateCallback callback, String callerPath) {
        scheduler.submit(new UpdateSequenceRunnable(sequence, callback, callerPath));
    }

    private void switchToNewModel(ContainerRoot c) {
        ContainerRoot cc = c;
        if (!c.isReadOnly()) {
            broadcastTelemetry(TelemetryEvent.Type.LOG_WARNING, "It is not safe to store ReadWrite model!", null);
            //Log.error("It is not safe to store ReadWrite model")
            cc = kevoreeFactory.createModelCloner().clone(c, true);
        }
        //current model is backed-up
        UUIDModel previousModel = model.get();
        if (previousModel != null) {
            models.add(previousModel);
        }
        // TODO : MAGIC NUMBER ;-) , ONLY KEEP 10 PREVIOUS MODEL
        if (models.size() > 15) {
            models.removeFirst();
            Log.debug("Garbage old previous model");
        }
        //Changes the current model by the new model
        if (cc != null) {
            UUIDModel uuidModel = new UUIDModelImpl(UUID.randomUUID(), cc);
            model.set(uuidModel);
            lastDate = new Date(System.currentTimeMillis());
            //Fires the update to listeners
            modelListeners.notifyAllListener();
        }
    }

    public boolean internal_update_model(ContainerRoot proposedNewModel, String callerPath) {
        if (proposedNewModel.findNodesByID(getNodeName()) == null) {
            broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Asking for update with a NULL model or node name (" + getNodeName() + ") was not found in target model !", null);
            return false;
        }
        try {
            pending = proposedNewModel;
            ContainerRoot currentModel = model.get().getModel();
            Log.trace("Before listeners PreCheck !");
            UpdateContext updateContext = new UpdateContext(currentModel, proposedNewModel, callerPath);
            boolean preCheckResult = modelListeners.preUpdate(updateContext);
            Log.trace("PreCheck result = " + preCheckResult);
            Log.trace("Before listeners InitUpdate !");
            boolean initUpdateResult = modelListeners.initUpdate(updateContext);
            Log.debug("InitUpdate result = " + initUpdateResult);
            if (preCheckResult && initUpdateResult) {
                //CHECK FOR HARA KIRI
                ContainerRoot previousHaraKiriModel = null;
                //Checks and bootstrap the node
                checkBootstrapNode(proposedNewModel);
                currentModel = model.get().getModel();
                long milli = System.currentTimeMillis();
                if (Log.DEBUG) {
                    Log.debug("Begin update model {}", milli);
                }
                boolean deployResult;
                AdaptationModel adaptationModel = null;
                try {
                    if (nodeInstance != null) {
                        // Compare the two models and plan the adaptation
                        Log.info("Comparing models and planning adaptation.");
                        broadcastTelemetry(TelemetryEvent.Type.MODEL_COMPARE_AND_PLAN, "Comparing models and planning adaptation.", null);
                        adaptationModel = nodeInstance.plan(currentModel, proposedNewModel);
                        //Execution of the adaptation
                        Log.info("Launching adaptation of the system.");
                        broadcastTelemetry(TelemetryEvent.Type.PLATFORM_UPDATE_START, "Launching adaptation of the system.", null);
                        updateContext = new UpdateContext(currentModel, proposedNewModel, callerPath);

                        final UpdateContext final_updateContext = updateContext;
                        PrimitiveExecute afterUpdateTest = new PrimitiveExecute() {
                            @Override
                            public boolean exec() {
                                return modelListeners.afterUpdate(final_updateContext);
                            }
                        };
                        PrimitiveExecute postRollbackTest = new PrimitiveExecute() {
                            @Override
                            public boolean exec() {
                                modelListeners.postRollback(final_updateContext);
                                return true;
                            }
                        };
                        PreCommand preCmd = new PreCommand(updateContext, modelListeners);
                        ContainerNode rootNode = proposedNewModel.findNodesByID(getNodeName());
                        deployResult = PrimitiveCommandExecutionHelper.instance$.execute(this, rootNode, adaptationModel, nodeInstance, afterUpdateTest, preCmd.preRollbackTest, postRollbackTest);
                    } else {
                        broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Unable to initialize platform node " + getNodeName(), null);
                        Log.error("Unable to initialize platform node " + getNodeName());
                        deployResult = false;
                    }
                } catch (Exception e) {
                    broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while updating", e);
                    Log.error("Error while updating", e);
                    deployResult = false;
                }
                if (deployResult) {
                    switchToNewModel(proposedNewModel);
                    broadcastTelemetry(TelemetryEvent.Type.PLATFORM_UPDATE_SUCCESS, "Update successfully completed.", null);
                    Log.info("Update successfully completed.");
                } else {
                    //KEEP FAIL MODEL, TODO
                    //Log.warn("Update failed")
                    broadcastTelemetry(TelemetryEvent.Type.PLATFORM_UPDATE_FAIL, "Update failed!", null);
                    //IF HARAKIRI
                    if (adaptationModel != null) {

                    }
//                    if (previousHaraKiriModel != null) {
//                        internal_update_model(previousHaraKiriModel, callerPath);
//                        previousHaraKiriModel = null; //CLEAR
//                    }
                }
                long milliEnd = System.currentTimeMillis() - milli;
                Log.info("End deploy result={} ({}ms)", deployResult, milliEnd);
                pending = null;
                return deployResult;

            } else {
                Log.warn("PreCheck or InitUpdate Step was refused, update aborded !");
                return false;
            }
        } catch (Throwable e) {
            broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while updating.", e);
            //Log.error("Error while update", e)
            return false;
        }
    }

    private Object bootstrapNodeType(ContainerRoot model, String nodeName) {
        ContainerNode nodeInstance = model.findNodesByID(nodeName);
        if (nodeInstance != null) {
            FlexyClassLoader kcl = bootstrapService.installTypeDefinition(nodeInstance.getTypeDefinition());
            Object newInstance = bootstrapService.createInstance(nodeInstance, kcl);
            bootstrapService.injectDictionary(nodeInstance, newInstance, false);
            return newInstance;
        } else {
//            broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Node not found using name " + nodeName, null);
            Log.error("Unable to find a node named \"" + nodeName + "\" in model");
            return null;
        }
    }

    private void checkBootstrapNode(ContainerRoot currentModel) {
        try {
            if (nodeInstance == null) {
            	nodeInstance = (NodeType) bootstrapNodeType(currentModel, getNodeName());
                if (nodeInstance != null) {
                    resolver = new MethodAnnotationResolver(nodeInstance.getClass());
                    Method met = resolver.resolve(org.kevoree.annotation.Start.class);
                    if (met != null) {
                    	try {
                    		met.invoke(nodeInstance);
                    	} catch (Throwable ee) {
                    		Log.error("Error while invoking platform node @Start method" , ee);
                    	}
                    }
                    UUIDModelImpl uuidModel = new UUIDModelImpl(UUID.randomUUID(), kevoreeFactory.createContainerRoot());
                    model.set(uuidModel);
                } else {
                    broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Unable to initialize platform node " + getNodeName(), null);
                	Log.error("Unable to initialize platform node " + getNodeName());
                    this.stop();
                }
            }
        } catch (Throwable e) {
//            broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while bootstraping node instance", e);
        	Log.error("Error while bootstrapping node instance " + getNodeName(), e);
            if (nodeInstance != null) {
                Method met = resolver.resolve(org.kevoree.annotation.Stop.class);
                if (met != null) {
                	try {
                		met.invoke(nodeInstance);
                	} catch (Throwable ee) {
                		Log.error("Error while invoking platform node @Stop method" , ee);
                	}
                }
            }
            nodeInstance = null;
            resolver = null;
        }
    }

    private class PreCommand {

        private UpdateContext context;
        private KevoreeListeners modelListeners;

        private PreCommand(UpdateContext context, KevoreeListeners modelListeners) {
            this.context = context;
            this.modelListeners = modelListeners;
        }

        boolean alreadyCall = false;
        PrimitiveExecute preRollbackTest = new PrimitiveExecute() {
            @Override
            public boolean exec() {
                if (!alreadyCall) {
                    modelListeners.preRollback(context);
                    alreadyCall = true;
                }
                return true;
            }
        };
    }


    public void start() {
        if (getNodeName() == null || getNodeName().equals("")) {
            setNodeName("node0");
        }
        modelListeners.start(getNodeName());
        broadcastTelemetry(TelemetryEvent.Type.PLATFORM_START, "Kevoree Start event : node name = " + getNodeName(), null);
        scheduler = java.util.concurrent.Executors.newSingleThreadExecutor(new KevoreeCoreThreadFactory(getNodeName()));
        UUIDModelImpl uuidModel = new UUIDModelImpl(UUID.randomUUID(), kevoreeFactory.createContainerRoot());
        model.set(uuidModel);
    }


    public void stop() {
        broadcastTelemetry(TelemetryEvent.Type.PLATFORM_STOP, "Kevoree Core will be stopped !", null);
        modelListeners.stop();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (nodeInstance != null) {
            try {
                ContainerRoot modelCurrent = model.get().getModel();
                ContainerRoot stopModel = kevoreeFactory.createModelCloner().clone(modelCurrent);
                ContainerNode currentNode = stopModel.findNodesByID(getNodeName());
                for (ContainerNode childNode : currentNode.getHosts()) {
                    childNode.setStarted(false);
                }
                //TEST only stop local
                for (Group group : stopModel.getGroups()) {
                    group.setStarted(false);
                }
                for (Channel hub : stopModel.getHubs()) {
                    hub.setStarted(false);
                }
                for (ComponentInstance childComponent : currentNode.getComponents()) {
                    childComponent.setStarted(false);
                }

                //val stopModel = factory.createContainerRoot()
                AdaptationModel adaptationModel = nodeInstance.plan(modelCurrent, stopModel);
                PrimitiveExecute afterUpdateTest = new PrimitiveExecute() {
                    public boolean exec() {
                        return true;
                    }
                };
                ContainerNode rootNode = (ContainerNode) modelCurrent.findByPath("nodes[" + getNodeName() + "]");
                if (rootNode != null) {
                    PrimitiveCommandExecutionHelper.instance$.execute(this, rootNode, adaptationModel, nodeInstance, afterUpdateTest, afterUpdateTest, afterUpdateTest);
                } else {
                    broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Node is not defined into the model so unbootstrap cannot be correctly done", null);
                }
            } catch (Exception e) {
                broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while unbootstrap", e);
                //Log.error("Error while unbootstrap", e)
            }
            try {
                Log.trace("Call instance stop");
                if (nodeInstance != null) {
                    Method met = resolver.resolve(org.kevoree.annotation.Stop.class);
                    if (met != null) {
                        met.invoke(nodeInstance);
                    }
                    nodeInstance = null;
                    resolver = null;
                }
            } catch (Exception e) {
                broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while stopping node instance", e);
            }
        }
        Log.info("Kevoree core stopped");
        broadcastTelemetry(TelemetryEvent.Type.LOG_INFO, "Kevoree Stopped", null);
    }

    private class AcquireLock implements Runnable {

        private LockCallBack callBack;

        private AcquireLock(LockCallBack callBack, Long timeout) {
            this.callBack = callBack;
            this.timeout = timeout;
        }

        private Long timeout;

        public void run() {
            if (currentLock != null) {
                try {
                    callBack.run(null, true);
                } catch (Throwable t) {
                    broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Exception inside a LockCallback with argument", t);
                    //Log.error("Exception inside a LockCallback with argument {}, {}", t, null, true)
                }
            } else {
                UUID lockUUID = UUID.randomUUID();
                currentLock = new TupleLockCallBack(lockUUID, callBack);
                lockWatchDog = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
                futurWatchDog = lockWatchDog.schedule(new WatchDogCallable(), timeout, TimeUnit.MILLISECONDS);
                try {
                    callBack.run(lockUUID, false);
                } catch (Throwable t) {
                    broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Exception inside a LockCallback with argument. uuid:" + lockUUID.toString(), t);
                    //Log.error("Exception inside a LockCallback with argument {}, {}", t, lockUUID.toString(), false)
                }
            }
        }
    }

    private class WatchDogCallable implements Runnable {
        public void run() {
            lockTimeout();
        }
    }

    public void releaseLock(UUID uuid, String callerPath) {
        if (uuid != null) {
            if (scheduler != null) {
                scheduler.submit(new ReleaseLockCallable(uuid));
            }
        } else {
            broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "ReleaseLock method of Kevoree Core called with null argument, can release any lock", null);
        }
    }

    public void acquireLock(LockCallBack callBack, Long timeout, String callerPath) {
        scheduler.submit(new AcquireLock(callBack, timeout));
    }

    private class ReleaseLockCallable implements Runnable {

        private ReleaseLockCallable(UUID uuid) {
            this.uuid = uuid;
        }

        private UUID uuid;

        public void run() {
            if (currentLock != null) {
                if (currentLock.getUuid().compareTo(uuid) == 0) {
                    currentLock = null;
                    futurWatchDog.cancel(true);
                    futurWatchDog = null;
                    lockWatchDog.shutdownNow();
                    lockWatchDog = null;
                }
            }
        }
    }

    private void lockTimeout() {
        if (scheduler != null) {
            scheduler.submit(new LockTimeoutCallable());
        }
    }

    private class LockTimeoutCallable implements Runnable {
        public void run() {
            if (currentLock != null) {
                try {
                    currentLock.getCallback().run(null, true);
                } catch (Throwable t) {
                    broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Exception inside a LockCallback when it is called from the timeout trigger", t);
                }
                currentLock = null;
                lockWatchDog.shutdownNow();
                lockWatchDog = null;
                futurWatchDog = null;
            }
        }
    }


}
