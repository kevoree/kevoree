package org.kevoree.library.javase.nodejs.proxy;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.nodejs.AbstractNodeJSComponentType;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/08/12
 * Time: 13:25
 */
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "ip", defaultValue = "127.0.0.1", optional = true),
        @DictionaryAttribute(name = "port", defaultValue = "8666", optional = true),
        @DictionaryAttribute(name = "remotePort", defaultValue = "80", optional = true),
        @DictionaryAttribute(name = "timeout", defaultValue = "-1", optional = true)
})
public class NodeJSProxy extends AbstractNodeJSComponentType {
    @Override
    public String getMainFile() {
        return "server.js";
    }
}
