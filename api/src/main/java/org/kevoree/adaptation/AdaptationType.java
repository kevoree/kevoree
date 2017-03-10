package org.kevoree.adaptation;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 16/11/2013
 * Time: 09:31
 */

public enum AdaptationType {
    // highest priority
    STOP_INSTANCE(0),
    REMOVE_BINDING(1),
    REMOVE_INSTANCE(2),
    REMOVE_DEPLOYUNIT(3),
    ADD_DEPLOYUNIT(4),
    ADD_INSTANCE(5),
    ADD_BINDING(6),
    UPDATE_PARAM(7),
    UPDATE_INSTANCE(8),
    START_INSTANCE(9);
    // lowest priority

    private final int rank;
    AdaptationType(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return this.rank;
    }
}