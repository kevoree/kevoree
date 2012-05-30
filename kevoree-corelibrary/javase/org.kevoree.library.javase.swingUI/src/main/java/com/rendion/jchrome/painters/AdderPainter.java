package com.rendion.jchrome.painters;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.rendion.jchrome.JChromeTabbedPane;

public class AdderPainter
{
  private static final int HEIGHT = 16;
  static final int WIDTH = 27;
  static final int OFFSET = -6;

  private int xloc, yloc;
  private boolean mouseOver = false;
  private boolean mouseDown = false;
  private final JChromeTabbedPane panel;
  private Theme theme;

  public AdderPainter(JChromeTabbedPane panel)
  {
    this.panel = panel;
    this.theme = panel.getTheme();
    yloc = theme.ADDBUTTON_TOP;
  }

  boolean mouseMoved(MouseEvent e)
  {
    if (hitTest(e.getX(), e.getY()) != mouseOver)
    {
      mouseOver = !mouseOver;
      return true;
    }
    return false;
  }

  boolean mousePressed(MouseEvent e)
  {
    if (hitTest(e.getX(), e.getY()) != mouseDown)
    {
      mouseDown = !mouseDown;
      return true;
    }
    return false;
  }

  boolean mouseReleased(MouseEvent e)
  {
    if (mouseDown)
    {
      mouseDown = false;
      panel.addTab();
      return true;
    }
    return false;
  }

  public boolean hitTest(int x, int y)
  {

    if (y < yloc || y > yloc + 17) return false;

    if (x < xloc || x > xloc + 27) return false;

    x -= xloc;
    y -= (yloc + 2);

    switch (y)
    {
    case 1:
      if (x > 0 && x < 21) return true;
      break;
    case 2:
    case 3:
      if (x >= 0 && x < 23) return true;
      break;

    case 4:
    case 5:
      if (x > 0 && x < 24) return true;
      break;
    case 6:
      if (x > 0 && x < 25) return true;
      break;
    case 7:
    case 8:
      if (x > 1 && x < 25) return true;
      break;
    case 9:
    case 10:
    case 11:
      if (x > 2 && x < 26) return true;
      break;
    case 12:
    case 13:
      if (x > 3) return true;
      break;
    case 14:
      if (x > 4) return true;
      break;
    case 15:
    case 16:
    case 17:
      if (x > 5) return true;
      break;
    }
    return false;
  }

  public void paint(Graphics2D g, int x)
  {
    xloc = x;
    g.drawImage(mouseDown ? theme.ADD_MDOWN : mouseOver ? theme.ADD_MOVER : panel.hasFocus() ? theme.ADD
        : theme.ADD_NOFOCUS, x, theme.ADDBUTTON_TOP, WIDTH, HEIGHT, null);
  }

}