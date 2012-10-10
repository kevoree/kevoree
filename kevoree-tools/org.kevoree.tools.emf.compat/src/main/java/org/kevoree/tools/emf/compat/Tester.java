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
package org.kevoree.tools.emf.compat;

import org.kevoree.framework.KevoreeXmiHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/10/12
 * Time: 13:14
 */
public class Tester {

    public static void main(String[] args) throws IOException {

        Integer nbTest = 10;
        Long beginTime = System.currentTimeMillis();
        System.out.println("Begin ->");
        File input = new File("/Users/duke/Desktop/arduinoLedSimple.kev");
        for (int i = 0; i < nbTest; i++) {
            org.kemf.compat.kevoree.ContainerRoot model = EMFXmiHelper.loadStream(new FileInputStream(input));
            File temp = File.createTempFile("yop","yop");
            temp.deleteOnExit();
            FileOutputStream fout = new FileOutputStream(temp);
            EMFXmiHelper.saveStream(fout,model);
            fout.close();
            input = temp;

        }
        System.out.println("EMF AVG = "+((System.currentTimeMillis()-beginTime)/nbTest));
        beginTime = System.currentTimeMillis();
        for (int i = 0; i < nbTest; i++) {
            org.kevoree.ContainerRoot model = KevoreeXmiHelper.loadStream(new FileInputStream("/Users/duke/Desktop/arduinoLedSimple.kev"));

        }
        System.out.println("KMF AVG = "+((System.currentTimeMillis()-beginTime)/nbTest));
    }

}
