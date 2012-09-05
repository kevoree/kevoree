package org.kevoree.library.javase.nodejs.visu;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.nodejs.AbstractNodeJSComponentType;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/08/12
 * Time: 13:25
 */
@ComponentType
public class MetricVisu extends AbstractNodeJSComponentType {
    @Override
    public String getMainFile() {
        return "metric.js";
    }
}
