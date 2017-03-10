package org.kevoree.api.checker;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/01/14
 * Time: 17:11
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface CheckerContext {

    void put(String id, Object value);
    Object get(String id);
}
