package org.kevoree.api.handler;

import org.kevoree.ContainerRoot;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/12/11
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
public interface UUIDModel {

    UUID getUUID();

    ContainerRoot getModel();
}
