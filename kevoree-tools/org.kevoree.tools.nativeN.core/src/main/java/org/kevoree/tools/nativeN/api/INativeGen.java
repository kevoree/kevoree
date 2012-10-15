package org.kevoree.tools.nativeN.api;

import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/10/12
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public interface INativeGen {

    public int create_input(String name);
    public int create_output(String name);

    public String generateInputsPorts();
    public String generateOutputsPorts();

    public LinkedHashMap<String, Integer> getInputs_ports();
    public LinkedHashMap<String, Integer> getOuputs_ports();
    public String generateMethods();

}
