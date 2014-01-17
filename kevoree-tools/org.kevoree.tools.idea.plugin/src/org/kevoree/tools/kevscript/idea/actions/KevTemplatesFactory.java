package org.kevoree.tools.kevscript.idea.actions;

import com.intellij.ide.fileTemplates.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.kevoree.tools.kevscript.idea.KevBundle;
import org.kevoree.tools.kevscript.idea.KevIcons;
import org.kevoree.tools.kevscript.idea.KevScriptLanguageType;

import java.io.File;
import java.util.Properties;

/**
 * Created by duke on 16/01/2014.
 */
public class KevTemplatesFactory implements FileTemplateGroupDescriptorFactory {

    public enum Template {
        KevScriptFile("KevScript File");
        final String file;

        Template(String file) {
            this.file = file;
        }

        public String getFile() {
            return file;
        }
    }

    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        String title = KevBundle.message("file.template.group.title.kev");
        final FileTemplateGroupDescriptor group =
                new FileTemplateGroupDescriptor(title, KevIcons.KEVS_ICON_16x16);

        for (Template template : Template.values()) {
            group.addTemplate(new FileTemplateDescriptor(template.getFile(), KevIcons.KEVS_ICON_16x16));
        }

        return group;
    }

    public static PsiElement createFromTemplate(PsiDirectory directory, String name, String fileName, Template template) {
        String packageName = directory.getName();
        if (packageName.equalsIgnoreCase("src")) {
            packageName = "main";
        }
        return createFromTemplate(directory, packageName, name, fileName, template);
    }

    public static PsiElement createFromTemplate(PsiDirectory directory, String packageName, String name, String fileName, Template template) {

        final FileTemplate fileTemplate = FileTemplateManager.getInstance().getInternalTemplate(template.getFile());

        Properties properties = new Properties(FileTemplateManager.getInstance().getDefaultProperties());

        properties.setProperty("PACKAGE_NAME", packageName);
        properties.setProperty("NAME", name);

        String text;
        try {
            text = fileTemplate.getText(properties);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template for " + template.getFile(), e);
        }

        final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());

        if ((new File(fileName)).exists()) {
            throw new RuntimeException("File already exists");
        }

        final PsiFile file = factory.createFileFromText(fileName, KevScriptLanguageType.INSTANCE, text);

        return directory.add(file);
    }
}
