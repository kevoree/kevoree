package org.kevoree.core;

import org.kevoree.api.handler.ModelListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import org.kevoree.api.handler.UpdateContext;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.log.Log;

public class KevoreeListeners {
	
	private KevoreeCoreBean core;
    private ExecutorService scheduler = null;
    private ExecutorService schedulerAsync = null;
    private List<ModelListener> registeredListeners = new ArrayList<ModelListener>();
    
    public KevoreeListeners(KevoreeCoreBean core) {
    	this.core = core;
    }

    public void start(String nodeName) {
        scheduler = java.util.concurrent.Executors.newSingleThreadExecutor(new KL_ThreadFactory(nodeName));
        schedulerAsync = java.util.concurrent.Executors.newCachedThreadPool(new KL_ThreadFactory2(nodeName));
    }

    public void addListener(ModelListener l) {
        scheduler.submit(new AddListener(l));
    }
    
    public void removeListener(ModelListener l) {
    	if (scheduler != null) {
    		scheduler.submit(new RemoveListener(l));
    	}
    }

    public void notifyAllListener() {
        scheduler.submit(new NotifyAllListener());
    }

    public void stop() {
        registeredListeners.clear();
        schedulerAsync.shutdownNow();
        schedulerAsync = null;
        scheduler.shutdownNow();
        scheduler = null;
    }
    
    public boolean initUpdate(final UpdateContext context) {
    	try {
			return scheduler.submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					for (ModelListener listener : registeredListeners) {
						boolean result = listener.initUpdate(context);
						if (!result) {
							return false;
						}
					}
					return true;
				}
			}).get();
		} catch (Exception e) {
			return false;
		}
    }
    
    public boolean preUpdate(final UpdateContext context) {
    	try {
			return scheduler.submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					for (ModelListener listener : registeredListeners) {
						boolean result = listener.preUpdate(context);
						if (!result) {
							return false;
						}
					}
					return true;
				}
			}).get();
		} catch (Exception e) {
			return false;
		}
    }
    
    public boolean afterUpdate(final UpdateContext context) {
    	try {
			return scheduler.submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					for (ModelListener listener : registeredListeners) {
						boolean result = listener.afterLocalUpdate(context);
						if (!result) {
							return false;
						}
					}
					return true;
				}
			}).get();
		} catch (Exception e) {
			return false;
		}
    }
    
    public void preRollback(final UpdateContext context) {
    	scheduler.submit(new Runnable() {
    		@Override
    		public void run() {
    			for (ModelListener listener : registeredListeners) {
    				listener.preRollback(context);
    			}
    		}
		});
    }
    
    public void postRollback(final UpdateContext context) {
    	scheduler.submit(new Runnable() {
    		@Override
    		public void run() {
    			for (ModelListener listener : registeredListeners) {
    				listener.postRollback(context);
    			}
    		}
		});
    }
    
    private class AddListener implements Runnable {
    	private ModelListener listener;
    	
    	public AddListener(ModelListener listener) {
    		this.listener = listener;
    	}
    	
    	@Override
        public void run() {
            if (!registeredListeners.contains(listener)) {
                registeredListeners.add(listener);
            }
        }
    }
    
    private class RemoveListener implements Runnable {
    	private ModelListener listener;
    	
    	public RemoveListener(ModelListener listener) {
    		this.listener = listener;
    	}
    	
    	@Override
        public void run() {
            if (registeredListeners.contains(listener)) {
                registeredListeners.remove(listener);
            }
        }
    }
    
    private class NotifyAllListener implements Runnable {
    	
    	@Override
        public void run() {
    		for (ModelListener listener : registeredListeners) {
    			schedulerAsync.submit(new NotifyListener(listener));
    		}
        }
    }

    private class NotifyListener implements Runnable {
    	private ModelListener listener;
    	
    	public NotifyListener(ModelListener listener) {
    		this.listener = listener;
    	}
    	
    	@Override
        public void run() {
            try {
                Thread.currentThread().setContextClassLoader(listener.getClass().getClassLoader());
                listener.modelUpdated();
            } catch (Exception e) {
                core.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while triggering modelUpdated() listener method", e);
                Log.error("Error while triggering modelUpdated() listener method", e);
            }
        }
    }

    private class KL_ThreadFactory implements ThreadFactory {
    	private String name;
    	private SecurityManager manager = System.getSecurityManager();
    	private ThreadGroup group;
    	
    	public KL_ThreadFactory(String name) {
    		this.name = name;
    		this.manager = System.getSecurityManager();
    		if (this.manager != null) {
    			this.group = this.manager.getThreadGroup();
    		} else {
    			this.group = Thread.currentThread().getThreadGroup();
    		}
    	}
    	
    	@Override
    	public Thread newThread(Runnable runnable) {
            Thread t = new Thread(group, runnable, "Kevoree_ListenerScheduler_" + name);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
		}
    }
    
    private class KL_ThreadFactory2 implements ThreadFactory {
    	private String name;
    	private AtomicInteger numCreated;
    	private SecurityManager manager = System.getSecurityManager();
    	private ThreadGroup group;
    	
    	public KL_ThreadFactory2(String name) {
    		this.name = name;
    		this.numCreated = new AtomicInteger();
    		this.manager = System.getSecurityManager();
    		if (this.manager != null) {
    			this.group = this.manager.getThreadGroup();
    		} else {
    			this.group = Thread.currentThread().getThreadGroup();
    		}
    	}
    	
    	@Override
    	public Thread newThread(Runnable runnable) {
    		Thread t = new Thread(group, runnable, "Kevoree_AsyncListenerScheduler_" + name + "_" + numCreated.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
		}
    }
}
