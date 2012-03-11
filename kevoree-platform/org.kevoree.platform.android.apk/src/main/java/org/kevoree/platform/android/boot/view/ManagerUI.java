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
import org.kevoree.platform.android.boot.utils.KObservable;
import org.kevoree.platform.android.ui.KevoreeAndroidUIScreen;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 08/03/12
 * Time: 15:10
 */
public class ManagerUI extends KObservable<ManagerUI> implements KevoreeAndroidUIScreen, ActionBar.TabListener {

    private static final String TAG = ManagerUI.class.getSimpleName();
    private LinkedList<ActionBar.Tab> views = new LinkedList<ActionBar.Tab>();
    private FragmentActivity ctx = null;
    private ActionBar.Tab currentTab = null;

    public ManagerUI(FragmentActivity context) {
        this.ctx = context;
        ctx.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    public void restoreViews(FragmentActivity newctx) {
        ctx.getSupportActionBar().removeAllTabs(); //Remove from all context
        newctx.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ctx = newctx; //Save new context

        LinkedList<ActionBar.Tab> newViews = new LinkedList<ActionBar.Tab>();
        newViews.addAll(views);
        views.clear();

        for (ActionBar.Tab tab : newViews) {
            String tabName = tab.getText().toString();
            Log.i(TAG, "Restore " + tab.getText());
            // if exist remove parent
            if (tab.getCustomView().getParent() != null) {
                ((ViewGroup) tab.getCustomView().getParent()).removeView(tab.getCustomView());
            }
            
            ActionBar.Tab newTab = ctx.getSupportActionBar().newTab();
            newTab.setText(tab.getText());
            //newTab.setCustomView(tab.getCustomView());
            views.add(newTab);
            ctx.getSupportActionBar().addTab(newTab);
        }



        if (currentTab != null) {
            currentTab.select();
        }
    }


    @Override
    public void addToGroup(String groupKey, View view) {
        ActionBar.Tab idTab = getTabById(groupKey);
        Log.i("KevoreeBoot", "Add" + groupKey + "-" + idTab + "-" + view);

        if (idTab == null) {
            idTab = ctx.getSupportActionBar().newTab();
            idTab.setText(groupKey);
            idTab.setTabListener(this);
            ctx.getSupportActionBar().addTab(idTab);
            LinearLayout tabLayout = new LinearLayout(ctx);
            idTab.setCustomView(tabLayout);
            views.add(idTab);
        }
        ((LinearLayout) idTab.getCustomView()).addView(view);

        /// Set the screen content to an the groupkey
        ctx.setContentView(idTab.getCustomView());
        notifyObservers(this);
    }


    @Override
    public void removeView(View view) {
        for (ActionBar.Tab idTab : views) {
            if (idTab != null) {
                LinearLayout l = (LinearLayout) idTab.getCustomView();
                l.removeView(view);
                if (l.getChildCount() == 0) {
                    ctx.getSupportActionBar().removeTab(idTab);
                    views.remove(idTab);
                }
            }
        }
        notifyObservers(this);
    }


    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public ActionBar.Tab getTabById(String id) {
        for (ActionBar.Tab t : views) {
            if (t.getText().equals(id)) {
                return t;
            }
        }
        return null;
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (tab.getCustomView() != null) {
            if (tab.getCustomView().getParent() != null) {
                ((ViewGroup) tab.getCustomView().getParent()).removeView(tab.getCustomView());
            }
            ctx.setContentView(tab.getCustomView());
            Log.i("fuck",tab.getText().toString());
            currentTab = tab;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    public FragmentActivity getCtx() {
        return ctx;
    }
}