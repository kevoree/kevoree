package org.kevoree.library.voldemortChannels;

import voldemort.client.StoreClient;
import voldemort.cluster.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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


            nodes.add(node0);

        KClient t = new KClient(nodes);

        StoreClient store = t.getStore("kevoree");
        for(int i= 0;i<100;i++){

                     System.out.println(i);

                    store.put("hello "+new Random().nextInt(100),"hllo");
        }



    }
}
