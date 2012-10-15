package org.kevoree.tools.nativeN;




public class Test {





    public static void main(String[] args) throws Exception {
       /*
        int ipc_key = 251102;
        int portEvents_tcp = 9865;


        ArrayList<String> repos = new ArrayList<String>();
        repos.add("http://maven.kevoree.org/release/");
        repos.add("http://maven.kevoree.org/snapshots/");

        File binary =   AetherUtil.resolveMavenArtifact4J("org.kevoree.nativeN.testcomponent_c", "org.kevoree.nativeN", "1.5-SNAPSHOT", "uexe", repos);

        if(!binary.canExecute())
        {
            binary.setExecutable(true);
        }


        // loading model from jar
        ContainerRoot model = KevoreeXmiHelper.load("/home/jed/DAUM_PROJECT/daum-library/pocXenomai/org.kevoree.tools.nativeN/org.kevoree.tools.nativeN.core/src/main/resources/lib.kev");

        NativeManager nativeManager = new NativeManager(ipc_key,portEvents_tcp,"HelloWorld",binary.getPath(),model);


        nativeManager.addEventListener(new NativeListenerPorts() {

            @Override
            public void disptach(NativeEventPort event, String port_name, String msg) {
                System.out.println("DISPATCH from " + port_name + " =" + msg);
            }
        });

        boolean  started = nativeManager.start();

        if(started)
        {

            nativeManager.update();

            nativeManager.push("input_port", "hello world ");
           Thread.sleep(10000);
            nativeManager.stop();


        } else
        {
            System.out.println("error");
        }

                 System.exit(0);  */


    }


}
