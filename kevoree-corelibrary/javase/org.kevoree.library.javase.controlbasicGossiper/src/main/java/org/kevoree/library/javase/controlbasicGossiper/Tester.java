package org.kevoree.library.javase.controlbasicGossiper;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/02/13
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class Tester {


    public static void main(String argv[]) throws Exception
    {



        HashMap<String, Object> dico = new HashMap<String, Object>();
        dico.put("val1", "value");
        AccessControlGroup group =new AccessControlGroup();
        group.setDictionary(dico);

        group.startRestGroup();
        group.push(null, "node0");




    }

}

