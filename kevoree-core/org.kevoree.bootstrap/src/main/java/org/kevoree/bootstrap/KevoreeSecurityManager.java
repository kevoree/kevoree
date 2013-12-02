package org.kevoree.bootstrap;

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
            throw new SecurityException("System.exit attempted and blocked.");
        }
    }

    @Override
    public void checkExit(int status) {
        throw new SecurityException("System.exit attempted and blocked.");
    }

}
