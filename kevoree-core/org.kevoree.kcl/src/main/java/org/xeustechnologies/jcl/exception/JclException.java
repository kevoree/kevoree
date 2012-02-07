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
package org.xeustechnologies.jcl.exception;

/**
 * General custom exception
 * 
 * @author Kamran Zafar
 * 
 */
public class JclException extends RuntimeException {
    /**
     * Default serial id
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public JclException() {
        super();
    }

    /**
     * @param message
     */
    public JclException(String message) {
        super( message );
    }

    /**
     * @param cause
     */
    public JclException(Throwable cause) {
        super( cause );
    }

    /**
     * @param message
     * @param cause
     */
    public JclException(String message, Throwable cause) {
        super( message, cause );
    }
}
