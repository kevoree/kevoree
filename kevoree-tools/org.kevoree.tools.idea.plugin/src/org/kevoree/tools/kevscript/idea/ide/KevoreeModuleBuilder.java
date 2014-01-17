package org.kevoree.tools.kevscript.idea.ide;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.KevIcons;
import org.kevoree.tools.kevscript.idea.KevTemplatesFactory;

import javax.swing.*;

/**
 * Created by duke on 17/01/2014.
 */
public class KevoreeModuleBuilder extends JavaModuleBuilder implements SourcePathsBuilder, ModuleBuilderListener {

    private static final Logger LOG = Logger.getInstance(KevoreeModuleBuilder.class);

    public KevoreeModuleBuilder() {
        addListener(this);
    }

    @Override
    public String getBuilderId() {
        return "kevoree";
    }

    @Override
    public Icon getNodeIcon() {
        return KevIcons.KEVS_ICON_16x16;
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdk) {
        return sdk == JavaSdk.getInstance();
    }

    @Override
    public void moduleCreated(@NotNull final Module module) {

        module.setOption("org.jetbrains.idea.maven.project.MavenProjectsManager.isMavenModule", "true");
        final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);


        VirtualFile sourceRoots[] = moduleRootManager.getSourceRoots();
        if (sourceRoots.length != 1) {
            return;
        }
        final PsiDirectory directory = PsiManager.getInstance(module.getProject()).findDirectory(sourceRoots[0]);
        if (directory == null || directory.getParentDirectory() == null) {
            return;
        }
        final PsiDirectory baseDir = directory.getParentDirectory();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    PsiDirectory srcDir = baseDir.findSubdirectory("src");
                    if (srcDir == null) {
                        srcDir = baseDir.createSubdirectory("src");
                    }
                    PsiDirectory mainDir = srcDir.findSubdirectory("main");
                    if (mainDir == null) {
                        mainDir = srcDir.createSubdirectory("main");
                    }
                    PsiDirectory java = mainDir.findSubdirectory("java");
                    if (java == null) {
                        java = mainDir.createSubdirectory("java");
                    }

                    ModifiableRootModel model = moduleRootManager.getModifiableModel();
                    ContentEntry contentEntry = model.getContentEntries()[0];
                    contentEntry.clearSourceFolders();
                    contentEntry.addSourceFolder(java.getVirtualFile(), false);
                    model.commit();
                    PsiDirectory kevs = mainDir.findSubdirectory("kevs");
                    if (kevs == null) {
                        kevs = mainDir.createSubdirectory("kevs");
                    }
                    PsiDirectory kevoree = java.findSubdirectory("kevoree");
                    if (kevoree == null) {
                        kevoree = java.createSubdirectory("kevoree");
                    }
                    KevTemplatesFactory.createFromTemplate(kevoree, "HelloWorld", "HelloWorld.java", KevTemplatesFactory.Template.KevComponentFile);
                    try {
                        String projectName = module.getName();
                        projectName = projectName.toLowerCase();
                        projectName = projectName.replace(" ", "");

                        KevTemplatesFactory.createFromTemplate(baseDir, projectName, "pom.xml", KevTemplatesFactory.Template.KevMavenProjectPomFile);
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }
        });
    }

    @Override
    public String getDescription() {
        return "Simple Kevoree";
    }

    @Override
    public String getPresentableName() {
        return "Kevoree Module";
    }

    @Override
    public String getGroupName() {
        return "Kevoree";
    }

}
