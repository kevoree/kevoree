package org.kevoree.tools.ui.editor.command;

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.UIEventHandler;
import org.mapdb.Fun;

import javax.swing.*;
import java.util.Iterator;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/08/13
 * Time: 09:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class MergeDefaultLibraryAll implements Command {

    private KevoreeUIKernel kernel;
    Iterator<Fun.Tuple4<String, String, String, String>> commandIterator;

    public MergeDefaultLibraryAll(KevoreeUIKernel kernel, Iterator<Fun.Tuple4<String, String, String, String>> commandIterator) {
        this.kernel = kernel;
        this.commandIterator = commandIterator;
    }

    @Override
    public void execute(Object p) {
        UIEventHandler.info("Loading libraries");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                while (commandIterator.hasNext()) {
                    final Fun.Tuple4<String, String, String, String> entry = commandIterator.next();
                    MergeDefaultLibrary cmdLDEFL1 = new MergeDefaultLibrary(entry.b, entry.c, entry.d);
                    cmdLDEFL1.setKernel(kernel);
                    cmdLDEFL1.execute(null);
                }
                UIEventHandler.info("Libraries loaded !");
            }
        });


    }
}
