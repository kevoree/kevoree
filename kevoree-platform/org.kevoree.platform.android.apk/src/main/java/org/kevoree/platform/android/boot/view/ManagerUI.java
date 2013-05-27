/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.kevoree.android.framework.service.events.IntentListener;
import org.kevoree.platform.android.boot.utils.KObservable;

import org.kevoree.platform.android.ui.KevoreeAndroidUIScreen;
import java.util.LinkedList;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 15:10
 */
public class ManagerUI extends KObservable<ManagerUI> implements KevoreeAndroidUIScreen, ActionBar.TabListener {

    private static final String TAG = ManagerUI.class.getSimpleName();
    private LinkedList<ActionBar.Tab> tabs = new LinkedList<ActionBar.Tab>();
    private FragmentActivity ctx = null;
    private int selectedTab = 0;
    private int ktab = -1;

    public ManagerUI(FragmentActivity context) {
        this.ctx = context;
        ctx.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    /**
     * this method is charge of restoring views during a rotation of the screen
     */
    public void restoreViews(FragmentActivity newctx)
    {
        if(ctx.getSupportActionBar().getTabAt(selectedTab) == null)
        {
            selectedTab = ktab;
        }
        ctx.getSupportActionBar().removeAllTabs();
        newctx.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ctx = newctx;
        LinkedList<ActionBar.Tab>  backup =new LinkedList<ActionBar.Tab>();
        backup.addAll(tabs);
        tabs.clear();
        for (ActionBar.Tab tab : backup) {
            Log.i(TAG,"Restore "+tab.getText());
            // if exist remove parent
            View contentView = (View) tab.getTag();
            if (contentView.getParent() != null) {
                ((ViewGroup) contentView.getParent()).removeView(contentView);
            }
            LinearLayout tabLayout = (LinearLayout) contentView;
            int childcount = tabLayout.getChildCount();
            for (int i=0; i < childcount; i++)
            {
                View v = tabLayout.getChildAt(i);
                if(v != null){
                    // remove link to linearlayout
                    if(v.getParent() != null)
                    {
                        ((ViewGroup) v.getParent()).removeView(v);
                    }
                    addToGroup(tab.getText().toString(),v);
                }
            }
        } try {
        ctx.getSupportActionBar().setSelectedNavigationItem(selectedTab);
    } catch (Exception e){
        // ignore
    }
    }


    @Override
    public void addToGroup(String groupKey, View view) {

        if(groupKey == null || view == null){
            return ;
        }

        ActionBar.Tab idTab = getTabById(groupKey);
        Log.i("KevoreeBoot", "Add" + groupKey + "-" + idTab + "-" + view);
        if (idTab == null) {
            idTab = ctx.getSupportActionBar().newTab();
            idTab.setText(groupKey);
            idTab.setTabListener(this);
            ctx.getSupportActionBar().addTab(idTab);
            LinearLayout tabLayout = new LinearLayout(ctx);
            idTab.setTag(tabLayout);
            tabs.add(idTab);
        }
        ((LinearLayout) idTab.getTag()).addView(view);

        if(selectedTab == idTab.getPosition()){
            ctx.setContentView((View) idTab.getTag());
            ktab = selectedTab;
        }
        /// Set the screen content to an the groupkey
        notifyObservers(this);
    }


    @Override
    public void removeView(View view)
    {

        if(view == null){
            return ;
        }

        Log.i("Request remove view","");
        LinkedList<ActionBar.Tab>  newtabs =new LinkedList<ActionBar.Tab>();
        for (ActionBar.Tab tab : tabs)
        {
            LinearLayout tabLayout = (LinearLayout) tab.getTag();
            int childcount = tabLayout.getChildCount();
            for (int i=0; i < childcount; i++)
            {
                View v = tabLayout.getChildAt(i);
                if(v != null){
                    if(v.equals(view))
                    {
                        tabLayout.removeView(view);
                    }
                }
            }
            Log.i("Remove view "+tab.getText()," "+((LinearLayout) tab.getTag()).getChildCount());
            if(((LinearLayout) tab.getTag()).getChildCount() == 0)
            {
                if(tab.getPosition() == selectedTab)
                {
                    selectedTab =ktab;
                }
                ctx.getSupportActionBar().removeTab(tab);
            }
            else
            {
                newtabs.add(tab);
            }
        }
        tabs.clear();
        tabs.addAll(newtabs);
        notifyObservers(this);
    }

    @Override
    public void addIntentListener(IntentListener listener) {
            // empty we don't care here
    }

    @Override
    public void removeIntentListener(IntentListener listener) {

    }


    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public ActionBar.Tab getTabById(String id) {
        for (ActionBar.Tab t : tabs) {
            if (t.getText().equals(id)) {
                return t;
            }
        }
        return null;
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        View contentView = (View) tab.getTag();
        if (tab.getTag() != null) {

            if (contentView.getParent() != null) {
                ((ViewGroup) contentView.getParent()).removeView(contentView);
            }
            ctx.setContentView(contentView);
            // if you don't call this method some views can behave
            // weirdly after setContentView(View)
            doRequestLayout(contentView);
            selectedTab = tab.getPosition();
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    public FragmentActivity getCtx() {
        return ctx;
    }

    /**
     * Call requestLayout() recursively on each child view
     * contained into this view if it has any. Otherwise
     * the call will only be made on the given view
     *
     * @param view a View object
     */
    private void doRequestLayout(View view) {
        ViewGroup vg = null;
        if (view instanceof ViewGroup) {
            vg = (ViewGroup) view;
            for (int i=0; i<vg.getChildCount(); i++) {
                doRequestLayout(vg.getChildAt(i));
            }

        } else {
            view.requestLayout();
        }
    }
}