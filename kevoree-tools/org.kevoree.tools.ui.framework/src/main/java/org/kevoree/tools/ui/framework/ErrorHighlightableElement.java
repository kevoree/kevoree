package org.kevoree.tools.ui.framework;


public interface ErrorHighlightableElement {

    public enum STATE {
        IN_ERROR,NO_ERROR
    }

    public void setState(STATE state);

    public STATE getCurrentState();

}
