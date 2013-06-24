package org.kevoree.api.dataspace;

/**
 * Created by duke on 24/06/13.
 */
public interface DataSpaceListener {

    public void trigger(String path, Object data);

}
