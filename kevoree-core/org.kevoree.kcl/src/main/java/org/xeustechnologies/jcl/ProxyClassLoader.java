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
package org.xeustechnologies.jcl;

import java.io.InputStream;

/**
 * @author Kamran Zafar
 * 
 */
public abstract class ProxyClassLoader implements Comparable<ProxyClassLoader> {
    // Default order
    protected int order = 5;
    // Enabled by default
    protected boolean enabled = true;

    public int getOrder() {
        return order;
    }

    /**
     * Set loading order
     * 
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Loads the class
     * 
     * @param className
     * @param resolveIt
     * @return class
     */
    public abstract Class loadClass(String className, boolean resolveIt);

    /**
     * Loads the resource
     * 
     * @param name
     * @return InputStream
     */
    public abstract InputStream loadResource(String name);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int compareTo(ProxyClassLoader o) {
        return order - o.getOrder();
    }
}
