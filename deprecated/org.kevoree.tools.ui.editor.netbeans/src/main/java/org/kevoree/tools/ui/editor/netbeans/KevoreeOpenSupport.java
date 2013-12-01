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

import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.OpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author ffouquet
 */
public class KevoreeOpenSupport extends OpenSupport implements OpenCookie,CloseCookie {

    public KevoreeOpenSupport(KevoreeNbDataObject.Entry entry) {
        super(entry);
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {


        //Handler handler =new Handler();

        System.out.println(this.entry.getFile());



        KevoreeNbDataObject dobj = (KevoreeNbDataObject) entry.getDataObject();
        KevoreeNbTopComponent tc = KevoreeNbTopComponent.getDefault();
        tc.setDisplayName(dobj.getName());

        tc.getEditor().loadModel("file://"+this.entry.getFile().getPath());
        tc.getEditor().setDefaultSaveLocation("file://"+this.entry.getFile().getPath());

        return tc;

    }

}
