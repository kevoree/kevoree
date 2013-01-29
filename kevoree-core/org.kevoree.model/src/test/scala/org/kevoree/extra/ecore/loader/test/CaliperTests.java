package org.kevoree.extra.ecore.loader.test;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 29/01/13
* (c) 2013 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import org.junit.Test;
import org.kevoree.ContainerRoot;
import org.kevoree.loader.ModelLoader;

import java.io.File;
import java.net.URISyntaxException;

public class CaliperTests extends SimpleBenchmark {

    ModelLoader loader = null;

    @Override protected void setUp() {
        loader = new ModelLoader();
    }

    public void timeDefaultLibsLoad(int reps) {
        try {
            for(int i = 0 ; i < reps ; i++) {

            ContainerRoot model = loader.loadModelFromPath(new File(getClass().getResource("/defaultlibs.kev").toURI())).get(0);

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void timeUnomasLibsLoad(int reps) {
        try {
            for(int i = 0 ; i < reps ; i++) {

                ContainerRoot model = loader.loadModelFromPath(new File(getClass().getResource("/unomas.kev").toURI())).get(0);

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void timeUnomas_deuxLibsLoad(int reps) {
        try {
            for(int i = 0 ; i < reps ; i++) {

                ContainerRoot model = loader.loadModelFromPath(new File(getClass().getResource("/unomas2.kev").toURI())).get(0);

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void timeParametersBugLibsLoad(int reps) {
        try {
            for(int i = 0 ; i < reps ; i++) {

                ContainerRoot model = loader.loadModelFromPath(new File(getClass().getResource("/ParametersBug.kev").toURI())).get(0);

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void caliperTest() {
        Runner.main(CaliperTests.class,new String[0]);
        Runner.MeasurementResults
    }

}
