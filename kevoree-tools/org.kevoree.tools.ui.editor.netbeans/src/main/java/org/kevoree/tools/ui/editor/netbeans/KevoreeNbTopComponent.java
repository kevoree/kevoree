/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.editor.netbeans;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.kevoree.tools.ui.editor.KevoreeEditor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author ffouquet
 */
public class KevoreeNbTopComponent extends CloneableTopComponent {

    public KevoreeEditor getEditor() {
        return editor;
    }

    public void setEditor(KevoreeEditor editor) {
        this.editor = editor;
    }

    KevoreeEditor editor = new KevoreeEditor();

    public KevoreeNbTopComponent() {




        setName(NbBundle.getMessage(KevoreeNbTopComponent.class, "CTL_KevoreeNbTopComponent"));
        setToolTipText(NbBundle.getMessage(KevoreeNbTopComponent.class, "HINT_KevoreeNbTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));


        try {

// Get all resource paths (classpath paths) of the current classloader.
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getSystemResources("");

            System.out.println("Size  " + resources.hasMoreElements());
// Show them all.
            while (resources.hasMoreElements()) {
                System.out.println(resources.nextElement());
            }

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }


        setLayout(new BorderLayout());


        //org.kermeta.Kevoree.ui.editor.panel.KevoreeEditorPanel panel = new org.kermeta.Kevoree.ui.editor.panel.KevoreeEditorPanel();
        add(editor.getPanel(), BorderLayout.CENTER);

    }
    private static KevoreeNbTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/kermeta/Kevoree/editor/nb/socket16.png";
    private static final String PREFERRED_ID = "KevoreeNbTopComponent";

    // Variables declaration - do not modify
    // End of variables declaration
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized KevoreeNbTopComponent getDefault() {
        if (instance == null) {
            instance = new KevoreeNbTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the KevoreeNbTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized KevoreeNbTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(KevoreeNbTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof KevoreeNbTopComponent) {
            return (KevoreeNbTopComponent) win;
        }
        Logger.getLogger(KevoreeNbTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
