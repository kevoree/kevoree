package org.kevoree.tools.ui.framework.data;

import com.explodingpixels.macwidgets.*;
import javax.swing.*;
import org.kevoree.*;

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 28/10/11
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeSourceListItem extends SourceListItem {

    protected Object item;

    public KevoreeSourceListItem(String text) {
        super(text);
    }

    public KevoreeSourceListItem(String text, Icon icon) {
        super(text, icon);
    }

    public KevoreeSourceListItem(Object item) {
      this(item, null);
    }

    public KevoreeSourceListItem(Object item, Icon icon) {
        super(item.toString(), icon);
        this.item = item;
        if(NamedElement.class.isAssignableFrom(item.getClass())) {
            this.setText(((NamedElement)item).getName());
        }
    }

    public Object getKevoreeObject() {
        return item;
    }

}
