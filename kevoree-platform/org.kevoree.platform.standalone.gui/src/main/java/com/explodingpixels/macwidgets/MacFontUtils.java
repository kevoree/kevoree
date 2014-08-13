package com.explodingpixels.macwidgets;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.UIManager;

public class MacFontUtils {

    public static final float DEFAULT_FONT_SIZE = 11.0f;
    
    public static final float HUD_FONT_SIZE = 11.0f;
 	
	public static final Font DEFAULT_FONT = new Font("Serif", Font.PLAIN, 11); 

    public static final Font ITUNES_FONT =
    		UIManager.getFont("Table.font") != null ? UIManager.getFont("Table.font").deriveFont(DEFAULT_FONT_SIZE) : DEFAULT_FONT.deriveFont(DEFAULT_FONT_SIZE);

    public static final Font ITUNES_TABLE_HEADER_FONT =
    		UIManager.getFont("Table.font") != null ? UIManager.getFont("Table.font").deriveFont(Font.BOLD, DEFAULT_FONT_SIZE) : DEFAULT_FONT.deriveFont(Font.BOLD, DEFAULT_FONT_SIZE);

    public static final Font TOOLBAR_LABEL_FONT =
    		UIManager.getFont("Label.font") != null ? UIManager.getFont("Label.font").deriveFont(DEFAULT_FONT_SIZE) : DEFAULT_FONT.deriveFont(DEFAULT_FONT_SIZE);

    public static final Font DEFAULT_BUTTON_FONT =
    		UIManager.getFont("Button.font") != null ? UIManager.getFont("Button.font").deriveFont(DEFAULT_FONT_SIZE) : DEFAULT_FONT.deriveFont(DEFAULT_FONT_SIZE);
    				
    public static final Font HUD_BUTTON_FONT =
    		UIManager.getFont("Button.font") != null ? UIManager.getFont("Button.font").deriveFont(DEFAULT_FONT_SIZE) : DEFAULT_FONT.deriveFont(DEFAULT_FONT_SIZE);

    public static final Font DEFAULT_LABEL_FONT =
    		UIManager.getFont("Label.font") != null ? UIManager.getFont("Label.font").deriveFont(DEFAULT_FONT_SIZE) : DEFAULT_FONT.deriveFont(DEFAULT_FONT_SIZE);

    public static final Font BOLD_LABEL_FONT =
    	    UIManager.getFont("Label.font") != null ? UIManager.getFont("Label.font").deriveFont(DEFAULT_FONT_SIZE) : DEFAULT_FONT.deriveFont(Font.BOLD, DEFAULT_FONT_SIZE);
    		
    		
    public static void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
