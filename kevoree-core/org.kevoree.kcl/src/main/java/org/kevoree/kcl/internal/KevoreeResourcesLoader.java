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
package org.kevoree.kcl.internal;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/10/12
 * Time: 15:10
 */
public abstract class KevoreeResourcesLoader {

    private String extension = null;

    public KevoreeResourcesLoader(String _extension){
        extension = _extension;
    }


    public String getExtension() {
        return extension;
    }

    public abstract void doLoad(String key,InputStream stream);

}
