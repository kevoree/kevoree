package com.rendion.jchrome.painters;

import static com.rendion.jchrome.painters.TabState.highlighted;
import static com.rendion.jchrome.painters.TabState.notSelected;
import static com.rendion.jchrome.painters.TabState.selected;

import java.awt.Image;
import java.util.EnumMap;
import java.util.HashMap;

import com.rendion.util.ImageUtils;

public class ImageMaps
{
  private static HashMap<Boolean, EnumMap<TabState, EnumMap<TabState, Image>>> imageMaps;
  private static final boolean WINDOW_HAS_FOCUS = true;
  private static final boolean WINDOW_DOESNT_HAVE_FOCUS = false;
  public static Image CLOSE_BUTTON;
  public static Image CLOSE_BUTTON_PRESSED;
  public static Image ADD;
  public static Image ADD_NOFOCUS;
  public static Image ADD_MOVER;
  public static Image ADD_MDOWN;
  private static Theme theme;
  
  public static final EnumMap<TabState, Image> values(String start, String end, String center, String selected,
      String notSelected, String highlighted)
  {
    EnumMap<TabState, Image> values = new EnumMap<TabState, Image>(TabState.class);

    values.put(TabState.start, ImageUtils.load(theme.getPath(start)));
    values.put(TabState.end, ImageUtils.load(theme.getPath(end)));
    values.put(TabState.center, ImageUtils.load(theme.getPath(center)));
    if (selected != null) values.put(TabState.selected, ImageUtils.load(theme.getPath(selected)));
    if (notSelected != null) values.put(TabState.notSelected, ImageUtils.load(theme.getPath(notSelected)));
    if (highlighted != null) values.put(TabState.highlighted, ImageUtils.load(theme.getPath(highlighted)));

    return values;
  }

  public static final Image getTabImage(Boolean state, TabState state1, TabState state2)
  {
    return imageMaps.get(state).get(state1).get(state2);
  }
  
  public static void init(Theme aTheme)
  {
    theme = aTheme;
    
    /*
     * This is pretty bad juju, but the issue is that the first call to
     * drawString or createGlyphVector on OSX takes 500+ milliseconds due to
     * native library loading.
     * 
     * This gets that init out of the way synchronously before showing any tab
     * panels to avoid an initial slow ugly paint on OSX...
     * 
     * The "startup" time on drawString/createGlyphVector needs to be reported
     * as a bug on OSX, its unacceptable. On other platforms this will complete quickly
     * 
     */
    Image img = ImageUtils.load("images/yahoo.gif");
    img.getGraphics().drawString("foo", 0, 0);
    
    /*  end bad juju   */
    
    CLOSE_BUTTON = ImageUtils.load(theme.getPath("close.gif"));
    CLOSE_BUTTON_PRESSED = ImageUtils.load(theme.getPath("close-mdown.gif"));
    ADD = ImageUtils.load(theme.getPath("add.gif"));
    ADD_NOFOCUS = ImageUtils.load(theme.getPath("add-nofocus.gif"));
    ADD_MOVER = ImageUtils.load(theme.getPath("add-mover.gif"));
    ADD_MDOWN = ImageUtils.load(theme.getPath("add-mdown.gif"));
    
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

}
