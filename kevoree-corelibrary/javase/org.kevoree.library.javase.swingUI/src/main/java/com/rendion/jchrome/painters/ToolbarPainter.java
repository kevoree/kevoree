package com.rendion.jchrome.painters;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.rendion.jchrome.JChromeTabbedPane;

public class ToolbarPainter
{
  private static final int HEIGHT = 20;

  private final JChromeTabbedPane panel;
  private Theme theme;

  public ToolbarPainter(JChromeTabbedPane panel)
  {
    this.panel = panel;
    this.theme = panel.getTheme();
  }

  public boolean mouseMoved(MouseEvent e)
  {
    return false;
  }

  public boolean mousePressed(MouseEvent e)
  {
    return false;
  }

  public boolean mouseReleased(MouseEvent e)
  {
    return false;
  }

  public void paint(Graphics2D g)
  {
    g.drawImage(panel.hasFocus() ? theme.TBAR : theme.TBAR_NOFOCUS, 0, theme.TABROW_HEIGHT + 1,
        g.getClipBounds().width, HEIGHT, null);
  }

}