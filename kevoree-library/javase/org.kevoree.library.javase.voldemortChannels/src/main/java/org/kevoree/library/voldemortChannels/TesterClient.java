package org.kevoree.library.voldemortChannels;

import voldemort.client.StoreClient;
import voldemort.cluster.Node;
import voldemort.versioning.Versioned;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 18/04/12
 * Time: 11:07
 */
public class TesterClient {

    public static void  main(String argv[]){

        List<Node> nodes = new ArrayList<Node>();

        List<Integer> partitions =  new ArrayList<Integer>();
        partitions.add(0);

        List<Integer> partitions1 =  new ArrayList<Integer>();


        List<Integer> partitions2 =  new ArrayList<Integer>();




        Node node0 = new Node(0,"localhost",8081,9000,9000,partitions);
        Node node1 = new Node(1,"localhost",8082,6667,9001,partitions1);


        nodes.add(node0);
        nodes.add(node1);

        KClient t = new KClient(nodes);


        StoreClient store = t.getStore("kevoree");



        for(int i=0;i<100;i++){

                    store.put("hello","world"+i);


        }



        Versioned ver = store.get("hello");

        store.delete("hello");

        Versioned ver2 = store.get("hello");



        /*
Set<String> queryKeys = new HashSet<String>();
Map<String, List<Versioned<String>>> values = store.getAll(queryKeys, null);
System.out.println("Returned fewer keys than expected."+ queryKeys.size()+" "+values.size());

        */







    }
}
