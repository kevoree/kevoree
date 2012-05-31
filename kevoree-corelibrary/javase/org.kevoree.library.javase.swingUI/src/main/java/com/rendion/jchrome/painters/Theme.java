package com.rendion.jchrome.painters;

import static com.rendion.jchrome.painters.TabState.highlighted;
import static com.rendion.jchrome.painters.TabState.notSelected;
import static com.rendion.jchrome.painters.TabState.selected;

import java.awt.Color;
import java.awt.Image;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Properties;

import com.rendion.util.ImageUtils;

public class Theme
{
  private HashMap<Boolean, EnumMap<TabState, EnumMap<TabState, Image>>> imageMaps;
  private static final boolean WINDOW_HAS_FOCUS = true;
  private static final boolean WINDOW_DOESNT_HAVE_FOCUS = false;
  private static final String THEME_PROPERTIES = "theme.properties";

  public Image CLOSE_BUTTON;
  public Image CLOSE_BUTTON_PRESSED;
  public Image CLOSE_BUTTON_HIGHLIGHT;
  public Image CLOSE_BUTTON_HIGHLIGHT_PRESSED;
  public Color BACKGROUND_FOCUSED;
  public Color BACKGROUND_UNFOCUSED;
  public Image ADD;
  public Image ADD_NOFOCUS;
  public Image ADD_MOVER;
  public Image ADD_MDOWN;
  public int ADDBUTTON_TOP;
  public int TABROW_HEIGHT;
  public int TAB_HT_TOP;
  public Image TBAR;
  public Image TBAR_NOFOCUS;
  public int TAB_ICON_TOP;
  public int TAB_ICON_INDENT;
  public int TAB_TEXT_BASELINE;
  public int TAB_WIDTH;
  public int TAB_TEXT_SIZE;
  public int TAB_TEXT_INDENT;
  public boolean HINT_FRACTIONAL;
  public boolean HINT_ANTIALIAS;
  public Color BORDER_COLOR;
  public Color BORDER_COLOR_NOFOCUS;
  public int BORDER_EXTEND;
  private String name;
  private String imageRoot = "images";
  private Properties p;

  public Theme(String aTheme)
  {
    this.name = aTheme;

    try
    {
      InputStream in = Theme.class.getClassLoader().getResourceAsStream(getPath(THEME_PROPERTIES));
      p = new Properties();
      p.load(in);
      in.close();
    }
    catch (Exception e)
    {
      throw new RuntimeException("Theme " + aTheme + " does not exist!", e);
    }

    /*
     * This is pretty bad juju, but the issue is that the first call to
     * drawString or createGlyphVector on OSX takes 500+ milliseconds due to
     * native library loading.
     * 
     * This gets that init out of the way synchronously before showing any tab
     * panels to avoid an initial slow ugly paint on OSX...
     */
    Image img = ImageUtils.load("images/yahoo.gif");
    img.getGraphics().drawString("foo", 0, 0);

    /* end bad juju */

    BACKGROUND_FOCUSED = getColor("background.focused");
    BACKGROUND_UNFOCUSED = getColor("background.unfocused");
    
    CLOSE_BUTTON = ImageUtils.load(getPath("close.gif"));
    CLOSE_BUTTON_PRESSED = ImageUtils.load(getPath("close-mdown.gif"));
    ADD = ImageUtils.load(getPath("add.gif"));
    ADD_NOFOCUS = ImageUtils.load(getPath("add-nofocus.gif"));
    ADD_MOVER = ImageUtils.load(getPath("add-mover.gif"));
    ADD_MDOWN = ImageUtils.load(getPath("add-mdown.gif"));

    TBAR = ImageUtils.load(getPath("tbar.gif"));
    TBAR_NOFOCUS = ImageUtils.load(getPath("tbar-nofocus.gif"));

    TAB_ICON_TOP = getInt("tab.icon.top");
    TAB_TEXT_BASELINE = getInt("tab.text.baseline");
    TAB_ICON_INDENT = getInt("tab.icon.indent");
    ADDBUTTON_TOP = getInt("addbutton.top");
    TABROW_HEIGHT = getInt("tabrow.height");
    TAB_HT_TOP = getInt("tab.ht.top");
    BORDER_COLOR = getColor("border.focused");
    BORDER_COLOR_NOFOCUS = getColor("border.unfocused");
    BORDER_EXTEND=getInt("border.extend");
    TAB_WIDTH=getInt("tab.width");
    HINT_ANTIALIAS = getBoolean("hint.antialias");
    HINT_FRACTIONAL = getBoolean("hint.fractional");
    TAB_TEXT_INDENT = getInt("tab.text.indent");
    TAB_TEXT_SIZE = getInt("tab.text.size");
    

    imageMaps = new HashMap<Boolean, EnumMap<TabState, EnumMap<TabState, Image>>>();
    EnumMap<TabState, EnumMap<TabState, Image>> tabJoins = new EnumMap<TabState, EnumMap<TabState, Image>>(
        TabState.class);

    tabJoins.put(selected, values("tab-f-l.gif", "tab-f-r.gif", "tab-f-span.gif", null, "tab-f-r-t.gif",
        "tab-f-r-t-h.gif"));

    tabJoins.put(notSelected, values("tab-f-l-ns.gif", "tab-f-r-ns.gif", "tab-f-span-ns.gif", "tab-f-r-ns-b.gif",
        "tab-f-r-ns-t.gif", "tab-f-r-ns-t-h.gif"));

    tabJoins.put(highlighted, values("tab-f-l-h.gif", "tab-f-r-h.gif", "tab-f-span-h.gif", "tab-f-r-h-b.gif",
        "tab-f-r-h-t.gif", null));

    imageMaps.put(WINDOW_HAS_FOCUS, tabJoins);

    tabJoins = new EnumMap<TabState, EnumMap<TabState, Image>>(TabState.class);

    tabJoins.put(selected, values("tab-nf-l.gif", "tab-nf-r.gif", "tab-nf-span.gif", null, "tab-nf-r-t.gif", null));

    tabJoins.put(notSelected, values("tab-nf-l-ns.gif", "tab-nf-r-ns.gif", "tab-nf-span-ns.gif", "tab-nf-r-ns-b.gif",
        "tab-nf-r-ns-t.gif", null));

    imageMaps.put(WINDOW_DOESNT_HAVE_FOCUS, tabJoins);

  }

  public String getPath(String imgFile)
  {
    return String.format("%s/themes/%s/%s", imageRoot, name, imgFile);

  }

  public String get(String prop)
  {
    return p.getProperty(prop);
  }
  
  public boolean getBoolean(String prop)
  {
   return Boolean.parseBoolean(get(prop)); 
  }

  public int getInt(String prop)
  {
    return Integer.parseInt(get(prop));
  }

  public Color getColor(String prop)
  {
    String[] rgb = p.getProperty(prop).split(",");

    return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
  }

  private EnumMap<TabState, Image> values(String start, String end, String center, String selected, String notSelected,
      String highlighted)
  {
    EnumMap<TabState, Image> values = new EnumMap<TabState, Image>(TabState.class);

    values.put(TabState.start, ImageUtils.load(getPath(start)));
    values.put(TabState.end, ImageUtils.load(getPath(end)));
    values.put(TabState.center, ImageUtils.load(getPath(center)));
    if (selected != null) values.put(TabState.selected, ImageUtils.load(getPath(selected)));
    if (notSelected != null) values.put(TabState.notSelected, ImageUtils.load(getPath(notSelected)));
    if (highlighted != null) values.put(TabState.highlighted, ImageUtils.load(getPath(highlighted)));

    return values;
  }

  public Image getTabImage(Boolean state, TabState state1, TabState state2)
  {
    return imageMaps.get(state).get(state1).get(state2);
  }

}
