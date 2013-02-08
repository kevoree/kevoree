/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        //Runner.MeasurementResults
    }

}
