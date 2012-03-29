/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.freepastry;

import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;
import rice.p2p.past.PastContent;

/**
 *
 * @author sunye
 */
public class Content extends ContentHashPastContent implements PastContent {

    String content;

    public Content(Id id, String content) {
        super(id);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Content [" + content + "]";
    }
}
