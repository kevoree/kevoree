package org.kevoree.library.voldemortChannels;

import voldemort.cluster.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 18/04/12
 * Time: 10:07
 */
public class TesterServer {

    public static void  main(String argv[]){

        //  ContainerRoot model= KevoreeXmiHelper.load("/home/jed/Desktop/voldemor.kev");


        List<Node> nodes = new ArrayList<Node>();

        List<Integer> partitions =  new ArrayList<Integer>();
        partitions.add(0);

        List<Integer> partitions1 =  new ArrayList<Integer>();


        List<Integer> partitions2 =  new ArrayList<Integer>();



        Node node0 = new Node(0,"localhost",8081,6666,9000,partitions);
        Node node1 = new Node(1,"localhost",8082,6667,9001,partitions1);
        Node node2 = new Node(2,"localhost",8083,6668,9002,partitions2);

        nodes.add(node0);
        nodes.add(node1);
        nodes.add(node2);


        try {
            KServer t = new KServer(Utils.createServerConfig(0),"kcluster",nodes);
            t.start();


            KServer t1 = new KServer(Utils.createServerConfig(1),"kcluster",nodes);
            //   t1.start();

            KServer t2 = new KServer(Utils.createServerConfig(2),"kcluster",nodes);
            t2.start();
            Thread.sleep(2000000);

            t.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
