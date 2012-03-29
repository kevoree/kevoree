package org.kevoree.library.freepastry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.past.Past;
import rice.p2p.past.PastImpl;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.routing.RouteSet;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.PersistentStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManagerImpl;
import rice.tutorial.forwarding.MyMsg;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import rice.pastry.routing.RoutingTable;

public class PastryPeer implements Application {

    private PastryIdFactory pastryIdFactory;
    private Environment environment;
    private NodeIdFactory nidFactory;
    // construct the PastryNodeFactory, this is how we use rice.pastry.socket
    private PastryNodeFactory factory;
    // This will return null if we there is no node at that location
    private NodeHandle bootHandle;
    // construct a node, passing the null boothandle on the first loop will cause the node to start its own ring
    private PastryNode node;
    private Past app;
    private static final Logger LOG = Logger.getLogger(PastryPeer.class.getName());
    private List<Content> resultSet = new ArrayList<Content>();
    private List<Id> nullResult = new ArrayList<Id>();
    private List<Content> insertedContent = new ArrayList<Content>();
    private List<Content> failedContent = new ArrayList<Content>();
    private Endpoint endpoint;
    //private TesterUtil defaults;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition dataInserted = lock.newCondition();
    private final Condition dataRetrieved = lock.newCondition();
    private InetSocketAddress socketAddress;

    public PastryPeer(InetSocketAddress address) throws IOException {
        socketAddress = address;
        environment = new Environment();
        nidFactory = new RandomNodeIdFactory(environment);
    }

    public PastryPeer() throws UnknownHostException, IOException {
        this(new InetSocketAddress(InetAddress.getLocalHost(), 1200));
    }

    public boolean bootsrap() throws InterruptedException, IOException {

        factory = new SocketPastryNodeFactory(nidFactory, socketAddress.getPort(), environment);
        node = factory.newNode();
        pastryIdFactory = new rice.pastry.commonapi.PastryIdFactory(environment);
        node.boot(socketAddress);

        synchronized (this) {
            while (!node.isReady() && !node.joinFailed()) {
                // delay so we don't busy-wait
                this.wait(500);
                // abort if can't join
                if (node.joinFailed()) {
                    throw new IOException("Could not join the FreePastry ring.  Reason:" + node.joinFailedReason());
                }
            }
        }
        return true;
    }

    public boolean join() throws InterruptedException, IOException {
        FreeLocalPort port = new FreeLocalPort();
        int freePort = port.getPort();

        factory = new SocketPastryNodeFactory(nidFactory, freePort, environment);
        node = factory.newNode();
        pastryIdFactory = new rice.pastry.commonapi.PastryIdFactory(environment);
        node.boot(socketAddress);
        // the node may require sending several messages to fully boot into the ring
        synchronized (this) {
            int tries = 0;
            while (!node.isReady() && !node.joinFailed()) {
                // delay so we don't busy-wait
                this.wait(200);

                // abort if can't join
                if (node.joinFailed()) {
                    LOG.log(Level.SEVERE, "Could not join the FreePastry ring.  Reason:{0}", node.joinFailedReason());
                    throw new IOException("Could not join the FreePastry ring.  Reason:" + node.joinFailedReason());
                } else if (tries > 300) {
                    return false;
                }
                tries++;
            }

            // We are only going to use one instance of this application on each PastryNode
            endpoint = node.buildEndpoint(this, "myinstance");

            // now we can receive messages
            endpoint.register();
        }

        return true;

    }

    public void createPast() throws IOException {
        // Setting log(n) replicas
        Double replica = Math.log(8) / Math.log(2);

        //	create a different storage root for each node
        String storageDirectory = "./storage" + node.getId().hashCode();

        // create the persistent part
        Storage stor = new PersistentStorage(pastryIdFactory, storageDirectory, 4 * 1024 * 1024, node.getEnvironment());

        app = new PastImpl(node, new StorageManagerImpl(pastryIdFactory, stor, new LRUCache(
                new MemoryStorage(pastryIdFactory), 512 * 1024, node.getEnvironment())), replica.intValue(), "");

        //log.log(Level.INFO, "Started with node id : {0}", node.getLocalHandle().toString());

    }

    public void put(String key, String value) throws InterruptedException {
        Content myContent = new Content(pastryIdFactory.buildId(key), value);
        this.insert(myContent);
    }

    private void insert(final Content content) throws InterruptedException {
        app.insert(content, new Continuation() {

            public void receiveResult(Object result) {
                lock.lock();
                try {
                    LOG.log(Level.INFO, "Data inserted: {0}", result);
                    dataInserted.signal();
                } finally {
                    lock.unlock();
                }

                insertedContent.add(content);
            }

            public void receiveException(Exception result) {
                failedContent.add(content);
                throw new RuntimeException("Error storing " + content, result);
            }
        });

        lock.lock();
        try {
            LOG.info("Waiting for insertion.");
            dataInserted.await();
            LOG.info("Insertion finished.");
        } finally {
            lock.unlock();
        }
    }

    public String get(String key) throws InterruptedException {

        Id id = pastryIdFactory.buildId(key);
        String result = this.retrieve(id);
        LOG.log(Level.INFO, "Retrieved value: {0}", result);
        return result;

    }

    private String retrieve(final Id key) throws InterruptedException {
        assert key != null : "Trying to retrieve null key.";

        final AtomicReference<Content> result = new AtomicReference<Content>();

        Continuation cont = new Continuation() {

            public void receiveResult(Object o) {
                Content c = (Content) o;
                if ((o != null) && (key.equals(c.getId()))) {
                    result.set(c);
                    LOG.log(Level.INFO, "Content received: {0}", o.toString());
                    lock.lock();
                    try {
                        dataRetrieved.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            }

            public void receiveException(Exception result) {
                LOG.log(Level.SEVERE, "Error receiving content: {0}",
                        result.getMessage());
                throw new RuntimeException("Error receiving " + result);
            }
        };



        lock.lock();
        app.lookup(key, true, cont);
        try {
            LOG.info("Waiting for data reception.");
            while (result.get() == null) {
                dataRetrieved.await();
            }
            LOG.info("Data arrived");
        } finally {
            lock.unlock();
        }

        return result.get().getContent();
    }

    public void lookup(Id key) {
        LOG.log(Level.INFO, "Looking up ...");

        final Id lookupKey = key;
        LOG.log(Level.INFO, "Looking up {0} at node {1}",
                new Object[]{lookupKey, app.getLocalNodeHandle()});

        app.lookup(lookupKey, true, new Continuation() {

            public void receiveResult(Object result) {
                LOG.log(Level.INFO, "Successfully looked up {0} for key {1}.",
                        new Object[]{result, lookupKey});

                if (result == null) {
                    nullResult.add(lookupKey);
                } else {
                    if (!resultSet.contains(result)) {
                        resultSet.add((Content) result);
                    }
                }
            }

            public void receiveException(Exception result) {
                LOG.log(Level.SEVERE, "Error looking up " + lookupKey, result);
                result.printStackTrace();
            }
        });
    }

    public List<Content> getResultSet() {
        return resultSet;
    }

    public int getSizeExpected() {
        return resultSet.size();
    }

    public List<Content> getInsertedContent() {
        return insertedContent;
    }

    public List<Content> getFailedContent() {
        return failedContent;
    }

    public void leave() {
        node.destroy();
        environment.destroy();
    }

    @Deprecated
    public boolean isAlive() {
        return bootHandle.isAlive();
    }

    @Deprecated
    public List<NodeHandle> oldGetRoutingTable() {
        List<NodeHandle> list = new ArrayList<NodeHandle>();
        RouteSet[] routeSetVector;
        RouteSet routeSet;
        for (int i = 0; i < node.getRoutingTable().numRows(); i++) {
            routeSetVector = node.getRoutingTable().getRow(i);
            for (int j = 0; j < routeSetVector.length; j++) {
                if (routeSetVector[j] != null) {
                    routeSet = routeSetVector[j];
                    for (int k = 0; k < routeSet.size(); k++) {
                        if (!list.contains(routeSet.get(k))) {
                            list.add(routeSet.get(k));
                        }
                    }
                }
            }
        }
        return list;
    }

    public Set<String> getRoutingTable() {
        Set<String> result = new HashSet<String>();

        RoutingTable rt = node.getRoutingTable();
        for (int i = 0; i < rt.numRows(); i++) {
            for (RouteSet rs : rt.getRow(i)) {
                // rs can be null
                if (rs != null) {
                    for (NodeHandle handle : rs) {
                        Id id = handle.getNodeId();
                        result.add(id.toStringFull());
                    }
                }
            }
        }

        return result;
    }

    public void pingNodes() {
        RouteSet[] routeSetVector;
        for (int i = 0; i < node.getRoutingTable().numRows(); i++) {
            routeSetVector = node.getRoutingTable().getRow(i);
            for (int j = 0; j < routeSetVector.length; j++) {
                if (routeSetVector[j] != null) {
                    routeSetVector[j].pingAllNew();
                }
            }
        }
    }

    /*	public void ping(PastryNode nd){
    Ping pg = new Ping(nd);
    }*/
    @Deprecated
    public Id oldGetId() {
        return node.getNodeId();
    }

    /**
     *
     * @return The node Id, as String
     */
    public String getId() {
        return node.getId().toStringFull();
    }

    public boolean isReady() {
        if (node.joinFailed()) {
            return false;
        } else if (node.isReady()) {
            return true;
        } else {
            return false;
        }
    }

    public List<Id> getNullResulKeys() {
        return nullResult;
    }

    public void deliver(Id id, Message message) {
        System.out.println(this + " received " + message);
    }

    public boolean forward(RouteMessage message) {
        try {
            MyMsg msg = (MyMsg) message.getMessage(endpoint.getDeserializer());
            msg.addHop(endpoint.getLocalNodeHandle());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return true;
    }

    public void update(rice.p2p.commonapi.NodeHandle arg0, boolean arg1) {
        // TODO Auto-generated method stub
    }

    public void routeMyMsg(Id id) {
        System.out.println(this + " sending to " + id);
        Message msg = new MyMsg(endpoint.getId(), id);
        endpoint.route(id, msg, null);
    }

    public Past getPast() {
        return app;
    }

    public int getPort() {
        return socketAddress.getPort();
    }


    public void print() {
        
    }


    public static void main(String[] argv) throws UnknownHostException, IOException, InterruptedException {
        InetSocketAddress address =
                new InetSocketAddress(InetAddress.getLocalHost(), 1200);
        PastryPeer p = new PastryPeer(address);
        System.out.println("#");

        System.out.println(Integer.parseInt("*"));
        p.bootsrap();

    }
}
