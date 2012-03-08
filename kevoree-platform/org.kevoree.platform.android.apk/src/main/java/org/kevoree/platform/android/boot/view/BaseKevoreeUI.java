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
package org.kevoree.platform.android.boot.view;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.*;
import org.kevoree.platform.android.boot.controller.KController;
import org.kevoree.platform.android.boot.controller.Request;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 16:27
 */
public class BaseKevoreeUI extends LinearLayout {

    private Context ctx=null;
    private  KController kController;

    // UI
    private  Button btstart=null;
    private  Button btstop=null;
    private  EditText nodeNameView=null;
    private OnClickListener checkbox_list;
    private  CheckBox checkbox_info=null;
    private  CheckBox checkbox_debug=null;
    private  CheckBox checkbox_warn=null;
    private  Scroller scroller=null;
    private TextView messages;

    public BaseKevoreeUI(Context context,KController kController)
    {
        super(context);
        ctx = context;
        this.kController = kController;

        initUI();
        configUI();
        callbacksUI();
    }


    public void initUI(){
        btstart = new Button(ctx);
        btstop = new Button(ctx);
        nodeNameView = new EditText(ctx);
        checkbox_info = new CheckBox(ctx);
        checkbox_debug = new CheckBox(ctx);
        checkbox_warn = new CheckBox(ctx);
        scroller = new Scroller(ctx);
        messages = new TextView(ctx);
    }

    public void configUI(){
        LinearLayout layout = new LinearLayout(ctx);
        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.FILL_PARENT));
        setOrientation(LinearLayout.VERTICAL);

        btstart.setText("Start");
        btstop.setText("Stop");
        nodeNameView.setText("node0");
        nodeNameView.setWidth(150);
        checkbox_info.setText("INFO");
        checkbox_info.setChecked(true);
        checkbox_debug.setText("DEBUG");
        checkbox_warn.setChecked(true);
        checkbox_warn.setText("WARN");
        messages.setSingleLine(false);
        messages.setScroller(scroller);
        messages.setMovementMethod(new ScrollingMovementMethod());
        messages.setText("");


        layout.addView(nodeNameView);
        layout.addView(btstart);
        layout.addView(btstop);
        layout.addView(checkbox_info);
        layout.addView(checkbox_debug);
        layout.addView(checkbox_warn);
        addView(layout);
        addView(messages);

    }


    public void callbacksUI(){

        btstart.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {

                kController.handleMessage(Request.KEVOREE_START);
            }
        });


        btstop.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {
                kController.handleMessage(Request.KEVOREE_STOP);
            }
        });


        checkbox_warn.setOnClickListener(checkbox_list);
        checkbox_debug.setOnClickListener(checkbox_list);
        checkbox_info.setOnClickListener(checkbox_list);

        checkbox_list = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };


    }

}
