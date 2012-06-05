package org.kevoree.library.android.nodeType;

import org.kevoree.api.PrimitiveCommand;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/06/12
 * Time: 23:41
 */
public class NoopPrimitiveCommand implements PrimitiveCommand {
    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public void undo() {
    }
}
