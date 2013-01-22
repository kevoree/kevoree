import org.junit.Before;
import org.junit.Test;
import org.kevoree.tools.control.framework.utils.HelperSignature;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
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
