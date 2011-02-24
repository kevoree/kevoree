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
package org.kevoree.platform.osgi.android;

import org.kevoree.api.configuration.ConfigConstants;
import org.kevoree.api.configuration.ConfigurationService;

import java.util.HashMap;
import java.util.Map;

public class KevoreeAndroidConfigService implements ConfigurationService {

    public Map<String,String> def = new HashMap<String,String>();

    @Override
    public String getProperty(ConfigConstants.ConfigConstant constant) {
        if(def.containsKey(constant.getValue())){
           return def.get(constant.getValue());
        }
        return constant.getDefaultValue();
    }
}
