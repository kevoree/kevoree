package com.explodingpixels.macwidgets;

import javax.swing.UIManager;
import java.awt.Font;

public class MacFontUtils {

    public static final Font ITUNES_FONT =
            UIManager.getFont("Table.font").deriveFont(11.0f);

    public static final Font ITUNES_TABLE_HEADER_FONT =
            UIManager.getFont("Table.font").deriveFont(Font.BOLD, 11.0f);

    public static final Font TOOLBAR_LABEL_FONT =
            UIManager.getFont("Label.font").deriveFont(11.0f);

}
