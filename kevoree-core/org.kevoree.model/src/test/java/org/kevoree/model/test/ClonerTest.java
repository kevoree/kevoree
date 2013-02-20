package org.kevoree.model.test;

import org.junit.Test;
import org.kevoree.ContainerRoot;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.loader.ModelLoader;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/02/13
 * Time: 11:37
 */
public class ClonerTest {

    @Test
    public void testSelector() throws URISyntaxException {
        ModelLoader loader = new ModelLoader();
        ContainerRoot model = loader.loadModelFromPath(new File(ClonerTest.class.getResource("/node0.kev").toURI())).get(0);

        ModelCloner cloner = new ModelCloner();
        cloner.clone(model);


    }

}
