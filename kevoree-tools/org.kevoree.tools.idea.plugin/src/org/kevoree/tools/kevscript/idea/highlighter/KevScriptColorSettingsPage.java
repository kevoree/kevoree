package org.kevoree.tools.kevscript.idea.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kevoree.tools.kevscript.idea.KevIcons;

import javax.swing.*;
import java.util.Map;

/**
 * Created by duke on 21/01/2014.
 */
public class KevScriptColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("KEYWORD", KevScriptSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("STRING", KevScriptSyntaxHighlighter.STRING),
            new AttributesDescriptor("SEPARATOR", KevScriptSyntaxHighlighter.SEPARATOR),
            new AttributesDescriptor("COMMENT", KevScriptSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("IDENT", KevScriptSyntaxHighlighter.IDENT),
            new AttributesDescriptor("BAD_CHARACTER", KevScriptSyntaxHighlighter.BAD_CHARACTER),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return KevIcons.KEVS_ICON_16x16;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new KevScriptSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "repo \"org.sonatype.org/foo/bar?a=b&c=d\"\n" +
                "include mvn:org.kevoree.library.javase:org.kevoree.library.javase.websocketgrp:2.0.5-SNAPSHOT\n" +
                "\n" +
                "add node0, node1 : JavaSENode\n" +
                "add sync : HelloGroup/0.0.2-SNAPSHOT\n" +
                "add node0.comp0, node0.comp1 : HelloWorld\n" +
                "add chan0 : HelloChannel\n" +
                "\n" +
                "namespace space42\n" +
                "\n" +
                "// this is a comment\n" +
                "// comments allow any characters ! \\ù%*é=^``~&°.:!§,?/#çà][-|\n" +
                "\n" +
                "attach node0 sync\n" +
                "attach node0, node1 sync\n" +
                "attach * sync\n" +
                "attach * space42\n" +
                "\n" +
                "move node0.comp0 node1\n" +
                "move *.* node0 // ok this doesn't make sense maybe but it should parse!\n" +
                "move node0.comp0, node0.comp1 node1\n" +
                "\n" +
                "bind node0.comp0.sendMsg chan0\n" +
                "unbind node0.comp0.sendMsg chan1\n" +
                "unbind node0.* *\n" +
                "\n" +
                "set node0.comp0.foo = \"bar\"\n" +
                "set node0.*.baz = 'potato'\n" +
                "set sync.forcePush = \"false\"\n" +
                "set sync.port/node0 = '8000'\n" +
                "\n" +
                "network node0 192.168.0.1\n" +
                "network space42.node1 127.0.0.1\n" +
                "\n" +
                "remove sync\n" +
                "remove node0, node1.*\n" +
                "remove *\n" +
                "\n" +
                "detach node0 sync\n" +
                "detach space42.*, node1 sync\n" +
                "detach * space42.sync\n" +
                "// last comment";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "KevScript";
    }

}
