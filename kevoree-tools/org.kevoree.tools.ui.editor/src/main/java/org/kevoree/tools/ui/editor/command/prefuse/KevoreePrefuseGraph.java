package org.kevoree.tools.ui.editor.command.prefuse;

import prefuse.data.Schema;

/**
 * Created with IntelliJ IDEA.
 * User: dvojtise
 * Date: 20/11/12
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
public class KevoreePrefuseGraph {
    public static final String LABEL = "label" ;

    /** Node table schema used for kevoree Graphs */
    public static final Schema KEVOREE_SCHEMA = new Schema();
    static {
        KEVOREE_SCHEMA.addColumn(LABEL, String.class, "");
    }
}
