package org.kevoree.nativeNode;

import javax.swing.event.EventListenerList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 01/08/12
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
public class Handler {

    protected EventListenerList listenerList = new EventListenerList();
    private NativeJNI nativeHandler;
    private String path;
    private  int key;
    private LinkedHashMap<String,Integer> inputs_ports = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String,Integer> ouputs_ports = new LinkedHashMap<String, Integer>();

    public Handler(final int key,int port,final String path)
    {
        this.key = key;
        this.path =path;
        nativeHandler = new NativeJNI(this);
        nativeHandler.configureCL();
        nativeHandler.init(key,port);
        nativeHandler.register();
    }


    public String GenerateInputsPorts(){

        StringBuilder gen = new StringBuilder();
        for (String name : inputs_ports.keySet()){
            gen.append("void "+name+"(void *input) {\n");
            gen.append("// PORT INPUT \n");
            gen.append("}\n");
        }

        return gen.toString();
    }

    public String generatorPorts(){
        StringBuilder gen = new StringBuilder();

        for (String name : ouputs_ports.keySet()){
            gen.append("void "+name+"(void *input) {\n");
            gen.append(" process_output("+ouputs_ports.get(name)+",input);\n");
            gen.append("}\n");

        }

        gen.append("void dispatch(int port,int id_queue) {\n");
        gen.append(" kmessage *msg = dequeue(id_queue);\n");
        gen.append("  if(msg !=NULL)  {\n\n" +
                "    switch(port)\n\n" +
                "    {\n");

        for (String name : inputs_ports.keySet()){
            gen.append("\t\t\t case "+inputs_ports.get(name)+":\n");
            gen.append("\t\t\t\t\t "+name+"(msg->value);\n");
            gen.append("\t\t\t break;\n");
        }
        gen.append("   }\n" +
                "   }\n" +
                "}\n");


        gen.append("int main (int argc,char *argv[])\n" +
                "{\n" +
                "   \tif(argc >2)\n" +
                "    {\n" +
                "\t    key_t key =   atoi(argv[1]);\n" +
                "\t    int port=   atoi(argv[2]);\n" +
                "\n" +
                "\t     bootstrap(key,port);\n" +
                "         register_start(start);\n" +
                "         register_stop(stop);\n" +
                "         register_update(update);\n" +
                "         register_dispatch(dispatch);\n" +
                "\t     ctx->start();\n" +
                "\n" +
                "\twhile(1)\n" +
                "\t{\n" +
                "        sleep(1000000);\n" +
                "\t}\n" +
                "     }\n" +
                "}");

        return gen.toString();
    }

    public  void start() throws Exception {
        if(nativeHandler.start(key,path) != 0){

        }
        //todo check started  remove sleep
        Thread.sleep(2000);
    }

    public void stop() throws InterruptedException
    {
        Thread.sleep(3500);
        nativeHandler.stop(key);
    }


    public void addEventListener (NativePortsListener listener) {
        listenerList.add(NativePortsListener.class, listener);
    }

    public void removeEventListener (NativePortsListener listener) {
        listenerList.remove(NativePortsListener.class, listener);
    }


    public void fireEvent(NativePortEvent evt,String queue,String msg)
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2)
        {
            if (evt instanceof NativePortEvent)
            {
                ((NativePortsListener) listeners[i + 1]).disptach(evt,queue,msg);
            }
        }
    }
    public void update(){
        nativeHandler.update(key);
    }

    public void create_input(String name)
    {
        int id= nativeHandler.create_input(key,name);
        if(id < 0)
        {
            System.out.println("ERROR");
        }
        inputs_ports.put(name,id);
    }

    public int create_output(String name)
    {
        int id= nativeHandler.create_output(key, name);
        if(id < 0)
        {
            System.out.println("ERROR");
        }
        ouputs_ports.put(name,id);
        return id;
    }
    public void enqueue(String port,String msg)
    {
        if(nativeHandler.enqueue(key,inputs_ports.get(port),msg) != 0){
            //error
            System.out.println("error");
        }
    }
}


