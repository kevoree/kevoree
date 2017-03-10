package org.kevoree;

import java.security.Permission;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/12/2013
 * Time: 12:20
 */
public class KevoreeSecurityManager extends SecurityManager {
	
    public void checkPermission(Permission permission) {
        if ("exitVM".equals(permission.getName())) {
            throw new KevoreeSecurityManager.Exception("System exit attempted and blocked (only Kevoree can exit itself)");
        }
    }

    @Override
    public void checkExit(int status) {
        throw new KevoreeSecurityManager.Exception("System exit attempted and blocked (only Kevoree can exit itself)");
    }

    public static final class Exception extends SecurityException {
	    public Exception(String msg) {
	        super(msg);
        }
    }
}
