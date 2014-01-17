package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

/**
 * Created by gregory.nain on 17/01/2014.
 */
public class KevScriptRunConfigurationProducer extends RunConfigurationProducer<KevScriptRunConfiguration> {
    protected KevScriptRunConfigurationProducer(ConfigurationFactory configurationFactory) {
        super(new KevScriptRunConfigurationFactory(new KevScriptRunConfigurationType()));
    }

    protected KevScriptRunConfigurationProducer(ConfigurationType configurationType) {
        super(new KevScriptRunConfigurationType());
    }

    @Override
    protected boolean setupConfigurationFromContext(KevScriptRunConfiguration kevScriptRunConfiguration, ConfigurationContext configurationContext, Ref<PsiElement> psiElementRef) {
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(KevScriptRunConfiguration kevScriptRunConfiguration, ConfigurationContext configurationContext) {
        return true;
    }
}
