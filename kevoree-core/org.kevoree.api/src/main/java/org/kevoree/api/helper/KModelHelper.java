package org.kevoree.api.helper;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Package;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;

/**
 * Created by duke on 8/26/14.
 */
public class KModelHelper {

    public static ContainerRoot root(KMFContainer elem) {
        KMFContainer container = elem.eContainer();
        while (container != null) {
            if (container.eContainer() != null) {
                container = container.eContainer();
            } else {
                return (ContainerRoot) container;
            }
        }
        return null;
    }

    public static String fqnGroup(DeployUnit du) {
        StringBuilder buffer = new StringBuilder();
        org.kevoree.Package pack = (org.kevoree.Package) du.eContainer();
        while (pack != null) {
            buffer.insert(0, ".");
            buffer.insert(0, pack.getName());
            if (pack.eContainer() != null && pack.eContainer() instanceof Package) {
                pack = (org.kevoree.Package) pack.eContainer();
            } else {
                pack = null;
            }
        }
        return buffer.substring(0, buffer.length()-1);
    }

    public static Package fqnCreate(String groupName, ContainerRoot model, KevoreeFactory factory) {
        String[] packages = groupName.split("\\.");
        org.kevoree.Package pack = null;
        for (int i = 0; i < packages.length; i++) {
            if (!packages[i].equals("")) {
                if (pack == null) {
                    pack = model.findPackagesByID(packages[i]);
                    if (pack == null) {
                        pack = (org.kevoree.Package) factory.createPackage().withName(packages[i]);
                        model.addPackages(pack);
                    }
                } else {
                    org.kevoree.Package packNew = pack.findPackagesByID(packages[i]);
                    if (packNew == null) {
                        packNew = (org.kevoree.Package) factory.createPackage().withName(packages[i]);
                        pack.addPackages(packNew);
                    }
                    pack = packNew;
                }
            }

        }
        if (pack == null) {
            Log.error("Alert Package not created for name {}", groupName);
        }
        return pack;
    }

    public static void main(String[] args) {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot root = factory.createContainerRoot();
        System.out.println(fqnCreate("commons-logging", root, factory).path());
    }

}
