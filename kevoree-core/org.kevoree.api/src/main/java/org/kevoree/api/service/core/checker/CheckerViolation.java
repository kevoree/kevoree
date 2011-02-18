/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.api.service.core.checker;

/**
 *
 * @author ffouquet
 */
public class CheckerViolation {

    private Object targetObject;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

}
