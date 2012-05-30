package com.rendion.jchrome.painters;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.rendion.jchrome.JChromeTabbedPane;
import com.rendion.jchrome.Tab;

public class CloseButtonPainter
{
  private static int SIZE;
  private static int TOP;

  private int xloc, yloc;
  private boolean mouseOver, mouseDown;
  private final JChromeTabbedPane panel;
  private final Tab tab;
  private Theme theme;

  public CloseButtonPainter(JChromeTabbedPane panel, Tab tab)
  {
    this.panel = panel;
    this.tab = tab;
    this.theme = panel.getTheme();
    SIZE = theme.getInt("closebutton.size");
    TOP = theme.getInt("closebutton.top");
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
      panel.closeTab(tab);
      return true;
    }
    return false;
  }

  public boolean hitTest(int x, int y)
  {
    if (y < yloc || y > yloc + SIZE) return false;

    if (x < xloc || x > xloc + SIZE) return false;

    x -= xloc;
    y -= (yloc + 2);

    switch (y)
    {
    case 0:
      if (x > 2 && x < SIZE - 3) return true;
      break;
    case 1:
      if (x > 1 && x < SIZE - 2) return true;
      break;
    case 2:
      if (x > 0 && x < SIZE - 1) return true;
      break;
    case 3:
    case 4:
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
      return true;
    case 10:
      if (x > 0 && x < SIZE - 1) return true;
      break;
    case 11:
      if (x > 1 && x < SIZE - 2) return true;
      break;
    case 12:
      if (x > 2 && x < SIZE - 3) return true;
      break;
    }
    return false;
  }

  public void paint(Graphics2D g, int x)
  {
    xloc = x;
    yloc = TOP;

    if (mouseOver)
    {
      g.drawImage(mouseDown ? theme.CLOSE_BUTTON_PRESSED : theme.CLOSE_BUTTON, x, TOP, SIZE, SIZE, null);
    }
  }

}