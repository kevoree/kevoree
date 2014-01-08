package org.kevoree.tools.kevscript.idea;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by duke on 04/12/2013.
 */
public class KevScriptFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(KevScriptLanguageType.INSTANCE, KevScriptLanguageType.INSTANCE.getDefaultExtension());
    }
}
