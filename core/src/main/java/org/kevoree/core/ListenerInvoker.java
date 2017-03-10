package org.kevoree.core;

import org.kevoree.adaptation.KevoreeAdaptationException;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class ListenerInvoker {

    private ExecutorService scheduler;
    private List<ModelListener> listeners;

    public ListenerInvoker() {
        this.listeners = new ArrayList<>();
    }

    public void start(final ThreadFactory threadFactory) {
        scheduler = Executors.newCachedThreadPool(threadFactory);
    }

    public void addListener(ModelListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(ModelListener l) {
        listeners.remove(l);
    }

    public void stop() {
        listeners.clear();

        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public Future<Boolean> preUpdate(final UpdateContext context) {
        if (scheduler != null) {
            return scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    if (!listener.preUpdate(context)) {
                        return false;
                    }
                }
                return true;
            });
        }
        return null;
    }

    public void updateSuccess(final UpdateContext context) {
        if (scheduler != null) {
            scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    listener.updateSuccess(context);
                }
            });
        }
    }

    public void updateError(final UpdateContext context, final KevoreeAdaptationException error) {
        if (scheduler != null) {
            scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    listener.updateError(context, error);
                }
            });
        }
    }

    public Future<Boolean> preRollback(final UpdateContext context) {
        if (scheduler != null) {
            return scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    if (!listener.preRollback(context)) {
                        return false;
                    }
                }
                return true;
            });
        }

        return null;
    }

    public void rollbackSuccess(final UpdateContext context) {
        if (scheduler != null) {
            scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    listener.rollbackSuccess(context);
                }
            });
        }
    }

    public void rollbackError(final UpdateContext context, final KevoreeAdaptationException e) {
        if (scheduler != null) {
            scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    listener.rollbackError(context, e);
                }
            });
        }
    }

    public void preUpdateRefused(final UpdateContext context) {
        if (scheduler != null) {
            scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    listener.preUpdateRefused(context);
                }
            });
        }
    }

    public void preRollbackRefused(final UpdateContext context) {
        if (scheduler != null) {
            scheduler.submit(() -> {
                for (ModelListener listener : listeners) {
                    listener.preRollbackRefused(context);
                }
            });
        }
    }
}
