/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.framework;


public class UITools {

    public static String formatTitle(String title,int maxLenght) {


        String res = "";
        if (title.length() <= maxLenght) {
            int spacesNeeded = maxLenght - title.length();
            int spacesNeededBefore = spacesNeeded / 2;
            int spacesNeededAfter = spacesNeeded / 2 + spacesNeeded % 2;
            res = title;
            for (int i = 0; i < spacesNeededBefore; i++) {
                res = " " + res;
            }
            for (int i = 0; i < spacesNeededAfter; i++) {
                res =  res+" ";
            }

        } else {
            res = title.substring(0, ((maxLenght/2)-1) ) + ".." + title.substring(title.length() - (maxLenght/2), title.length() - 1);
        }
       // System.out.println(title.length() + "-" + res.length());
        return res;
    }

}
