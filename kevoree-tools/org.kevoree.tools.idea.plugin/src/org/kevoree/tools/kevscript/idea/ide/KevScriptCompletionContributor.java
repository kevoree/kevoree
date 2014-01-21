package org.kevoree.tools.kevscript.idea.ide;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.KevScriptLanguage;
import org.kevoree.tools.kevscript.idea.psi.KevScriptTypes;

/**
 * Created by duke on 21/01/2014.
 */
public class KevScriptCompletionContributor extends CompletionContributor {

    public KevScriptCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(KevScriptTypes.IDENT).withLanguage(KevScriptLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {


                        resultSet.addElement(LookupElementBuilder.create("coming soon...:-)"));
                    }
                }
        );
    }

}
