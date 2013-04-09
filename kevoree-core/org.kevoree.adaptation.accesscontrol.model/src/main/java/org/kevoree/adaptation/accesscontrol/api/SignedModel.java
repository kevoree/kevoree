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
package org.kevoree.adaptation.accesscontrol.api;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/02/13
 * Time: 9:12
 */
public interface SignedModel extends Signed {

    /**
     * This method returns the model
     * @return
     */
    public byte[] getSerialiedModel();

    /**
     * This method indicates the format storage
     * @return   format
     */
    public String getModelFormat();

    public ModelSignature getSignature();

}
