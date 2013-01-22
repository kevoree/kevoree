package org.kevoree.tools.control.framework.command;

import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.adaptation.control.api.ModelSignature;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoree.tools.control.framework.api.Command;
import org.kevoree.tools.control.framework.impl.ModelSignatureImpl;
import org.kevoree.tools.control.framework.utils.HelperSignature;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 11:08
 * This Command sign the model with the private key and store the signature and the publicKey
 */

public class CreateSignatureCommand implements Command {

    private SignedModel signedModel;
    private KeyPair key;

    public void setSignedModel(SignedModel signedModel)
    {
        this.signedModel = signedModel;
    }

    public void setKey(KeyPair key) {
        this.key = key;
    }

    @Override
    public void execute() throws ControlException {
        ModelSignature signature = null;
        try {
            signature = new ModelSignatureImpl(HelperSignature.getSignature(key.getPrivate(), signedModel.getSerialiedModel()));
            signedModel.getSignatures().put(key.getPublic().getEncoded(), signature);
        } catch (Exception e)
        {
          throw new ControlException(e);
        }
    }
}
