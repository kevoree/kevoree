package org.kevoree.api.dataspace;

/**
 * Created by duke on 24/06/13.
 */
public interface DataSpaceService {

    public void putData(String path, Object data);

    public Object getData(String path);

    public void registerListener(String pathQuery, DataSpaceListener listener);

    public void removeListener(DataSpaceService listener);

}
