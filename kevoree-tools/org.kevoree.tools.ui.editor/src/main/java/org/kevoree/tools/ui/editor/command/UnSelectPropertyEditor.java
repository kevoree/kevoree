package org.kevoree.tools.ui.editor.command;


import org.kevoree.tools.ui.editor.KevoreeUIKernel;

public class UnSelectPropertyEditor implements Command {

    private KevoreeUIKernel kernel;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void execute(Object p) {
          kernel.getEditorPanel().unshowPropertyEditor();
    }

}
