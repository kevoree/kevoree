package org.kevoree.api.service.core.logging;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/03/12
 * Time: 13:41
 */
public interface KevoreeLogService {

    public void setCoreLogLevel(KevoreeLogLevel level);

    public void setUserLogLevel(KevoreeLogLevel level);

    public KevoreeLogLevel getCoreLogLevel();

    public KevoreeLogLevel getUserLogLevel();

}
