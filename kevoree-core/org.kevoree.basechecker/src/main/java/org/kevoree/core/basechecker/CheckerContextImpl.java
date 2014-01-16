package org.kevoree.core.basechecker;

import org.kevoree.api.service.core.checker.CheckerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/01/14
 * Time: 17:08
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class CheckerContextImpl implements CheckerContext {

    private Map<String, Object> contextValues;

    public CheckerContextImpl() {
        this.contextValues = new HashMap<String, Object>();
    }

    public void put(String id, Object value) {
        if (contextValues.containsKey(id)) {
            contextValues.remove(id);
        }
        contextValues.put(id, value);
    }

    public Object get(String id) {
        return contextValues.get(id);
    }
}
