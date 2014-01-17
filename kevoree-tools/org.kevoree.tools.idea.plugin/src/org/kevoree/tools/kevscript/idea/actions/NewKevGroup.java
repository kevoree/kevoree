package org.kevoree.tools.kevscript.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;

/**
 * Created by duke on 16/01/2014.
 */
public class NewKevGroup extends DefaultActionGroup {

    @Override
    public void update(AnActionEvent e)
    {
        super.update(e);
        Sdk sdk = ProjectRootManager.getInstance(e.getProject()).getProjectSdk();
        e.getPresentation().setVisible(true);


                 /*

        final Module data = LangDataKeys.MODULE.getData(e.getDataContext());
        e.getPresentation().setVisible(data != null &&
                sdk != null &&
                sdk.getSdkType() instanceof GoSdkType);
                */



    }

}
