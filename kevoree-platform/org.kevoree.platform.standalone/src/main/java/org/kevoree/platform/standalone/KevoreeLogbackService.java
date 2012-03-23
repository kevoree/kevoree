package org.kevoree.platform.standalone;

import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/03/12
 * Time: 14:08
 */
public class KevoreeLogbackService implements KevoreeLogService {
    @Override
    public void setCoreLogLevel(KevoreeLogLevel kevoreeLogLevel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setUserLogLevel(KevoreeLogLevel kevoreeLogLevel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLogLevel(String s, KevoreeLogLevel kevoreeLogLevel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getCoreLogLevel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getUserLogLevel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevoreeLogLevel getLogLevel(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
