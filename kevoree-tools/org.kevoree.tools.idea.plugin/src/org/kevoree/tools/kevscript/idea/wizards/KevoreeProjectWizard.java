package org.kevoree.tools.kevscript.idea.wizards;

import com.intellij.ide.util.newProjectWizard.ProjectNameStep;
import com.intellij.ide.util.newProjectWizard.StepSequence;
import com.intellij.ide.util.newProjectWizard.modes.WizardMode;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kevoree.tools.kevscript.idea.ide.KevoreeModuleBuilder;

/**
 * Created by duke on 17/01/2014.
 */
public class KevoreeProjectWizard extends WizardMode {

    private final KevoreeModuleBuilder kevoreeModuleBuilder;

    public KevoreeProjectWizard() {
        kevoreeModuleBuilder = new KevoreeModuleBuilder();
    }

    @NotNull
    @Override
    public String getDisplayName(WizardContext wizardContext) {
        return "Kevoree Project from Scratch";
    }

    @NotNull
    @Override
    public String getDescription(WizardContext wizardContext) {
        return "Will create a new Kevoree project";
    }

    @Override
    public boolean isAvailable(WizardContext wizardContext) {
        return wizardContext.isCreatingNewProject();
    }

    @Nullable
    @Override
    protected StepSequence createSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        StepSequence sequence = new StepSequence();
        ProjectWizardStepFactory factory = ProjectWizardStepFactory.getInstance();
        final boolean isNewProject = wizardContext.getProject() == null;
        if (isNewProject) {
            sequence.addCommonStep(new ProjectNameStep(wizardContext, this));
        }
        return sequence;
    }

    @Nullable
    @Override
    public ProjectBuilder getModuleBuilder() {
        return kevoreeModuleBuilder;
    }

    @Override
    public void onChosen(boolean b) {
    }
}
