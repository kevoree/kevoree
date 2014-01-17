package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.kevoree.tools.kevscript.idea.KevScriptLanguageType;


/**
 * Created by gregory.nain on 17/01/2014.
 */
public class KevScriptRunConfigurationProducer extends RunConfigurationProducer<KevScriptRunConfiguration> {


    protected KevScriptRunConfigurationProducer() {
        super(new KevScriptRunConfigurationType());
    }

    //Checks if the target of the click is a kevScript file
    @Override
    protected boolean setupConfigurationFromContext(KevScriptRunConfiguration kevScriptRunConfiguration, ConfigurationContext configurationContext, Ref<PsiElement> psiElementRef) {

        if(configurationContext.getLocation() != null && configurationContext.getLocation().getVirtualFile() != null && configurationContext.getLocation().getVirtualFile().getExtension() != null) {
            if(configurationContext.getLocation().getVirtualFile().getExtension().equals(KevScriptLanguageType.DEFAULT_EXTENSION)){
                kevScriptRunConfiguration.kevsFile = configurationContext.getLocation().getVirtualFile();
                kevScriptRunConfiguration.setName("Run " + configurationContext.getModule().getName());
                return true;
            }
        }
        return false;
    }

    //Checks if a RunConfiguration already exists for this project
    @Override
    public boolean isConfigurationFromContext(KevScriptRunConfiguration kevScriptRunConfiguration, ConfigurationContext configurationContext) {
        return kevScriptRunConfiguration.getName().equals("Run " + configurationContext.getModule().getName());
    }
}
