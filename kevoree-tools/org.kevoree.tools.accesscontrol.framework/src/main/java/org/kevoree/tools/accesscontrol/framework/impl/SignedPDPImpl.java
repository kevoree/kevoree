package org.kevoree.tools.accesscontrol.framework.impl;

import org.kevoree.adaptation.accesscontrol.api.ModelSignature;
import org.kevoree.adaptation.accesscontrol.api.PDPSignature;
import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.adaptation.accesscontrol.api.SignedPDP;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/02/13
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class SignedPDPImpl implements SignedPDP, Serializable {


    @Override
    public byte[] getSerialiedModel() {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getModelFormat() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PDPSignature getSignature() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
