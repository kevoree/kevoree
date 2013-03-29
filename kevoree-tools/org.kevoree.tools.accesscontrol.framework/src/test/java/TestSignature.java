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
import org.junit.Before;
import org.junit.Test;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class TestSignature {

    KeyPair sign;
    @Before
    public void init() throws NoSuchAlgorithmException {

        sign = HelperSignature.generateKeys(1024);

    }
    @Test
    public void test_sign() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        byte [] signaturebytes =   HelperSignature.getSignature(sign.getPrivate(),"test".getBytes());
        assertNotNull(signaturebytes);
        assertTrue(HelperSignature.verifySignature(signaturebytes, sign.getPublic(), "test".getBytes()));
    }
}
