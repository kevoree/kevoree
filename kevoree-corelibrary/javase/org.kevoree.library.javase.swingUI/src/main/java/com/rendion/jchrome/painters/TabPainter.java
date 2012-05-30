package com.rendion.jchrome.painters;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.rendion.jchrome.JChromeTabbedPane;
import com.rendion.jchrome.Tab;

public class TabPainter
{
  private static final int IMG_HEIGHT = 30;
  private static final int ICON_SIZE = 16;
  private static final int TOP = 0;
  static final int LEFT = 20;
  static final int RIGHT = 26;

  private JChromeTabbedPane panel;
  private TabRowPainter row;
  //private final CloseButtonPainter closeButtonPainter;

  private int xloc, yloc;
  private boolean mouseDown, mouseOver;
  private Tab tab;
  private TabState tabState;
  private TabState nextTabState;
  private Image img;
  private int width;
  private Theme theme;

  public TabPainter(JChromeTabbedPane panel, TabRowPainter row, Tab t)
  {
    this.panel = panel;
    this.theme = panel.getTheme();
    this.tab = t;
    this.row = row;
    //this.closeButtonPainter = new CloseButtonPainter(panel, t);
  }

  boolean mouseMoved(MouseEvent e)
  {
    /*if (closeButtonPainter.mouseMoved(e))
    {
      mouseOver = true;
      return true;
    }  */

    if (hitTest(e.getX(), e.getY()) != mouseOver)
    {
      mouseOver = !mouseOver;
      if (mouseOver)
      {
        for (TabPainter tab : row.tabPainters)
        {
          tab.mouseOver = false;
        }
        mouseOver = true;
      }
      return true;
    }

    return false;
  }

  boolean mousePressed(MouseEvent e)
  {
    /*if (closeButtonPainter.mousePressed(e))
    {
      mouseDown = false;
      return true;
    } */

    if (hitTest(e.getX(), e.getY()) != mouseDown)
    {
      mouseDown = !mouseDown;
      return true;
    }
    return false;
  }

  boolean mouseReleased(MouseEvent e)
  {
   /* if (closeButtonPainter.mouseReleased(e))
    {
      return true;
    } */

    if (mouseDown)
    {
      mouseDown = false;
      if (!tab.isSelected())
      {
        panel.setSelectedTab(tab);
      }
      return true;
    }
    return false;
  }

  public boolean hitTest(int x, int y)
  {
    x -= xloc;
    y -= (yloc + 2);
    int w = LEFT + width + RIGHT;

    if ((y < theme.TAB_HT_TOP || y > theme.TABROW_HEIGHT - 1) || (x < 0 || x > w)) return false;

    y -= theme.TABROW_HEIGHT - 24;

    int[] leftPixels;
    int[] rightPixels;

    if (tabState == null)
    {
      throw new IllegalArgumentException("tabState cannot be null!");
    }

    if (tab.getIndex() == 0)
    {
      leftPixels = TabHT.LEFT_END;
    }
    else if (tabState == TabState.selected)
    {
      leftPixels = TabHT.LEFT_TOP;
    }
    else
    {
      leftPixels = TabHT.LEFT_BOTTOM;
    }

    if (tab.getIndex() == row.tabPainters.size() - 1)
    {
      rightPixels = TabHT.RIGHT_END;
    }
    else if (tabState == TabState.selected)
    {
      rightPixels = TabHT.RIGHT_TOP;
    }
    else
    {
      rightPixels = row.tabPainters.get(tab.getIndex() + 1).getTabState() == TabState.selected ? TabHT.RIGHT_BOTTOM
          : TabHT.RIGHT_TOP;
    }

    return (x > leftPixels[y]) && (x < LEFT + width + rightPixels[y]);
  }

  private TabState getTabState()
  {
    return tab.isSelected() ? TabState.selected : mouseOver ? TabState.highlighted : TabState.notSelected;
  }

  public int paint(Graphics2D g, int x, int width)
  {
    xloc = x - LEFT;
    this.width = width;
    tabState = getTabState();
    nextTabState = tab.getIndex() == row.tabPainters.size() - 1 ? TabState.end : row.tabPainters
        .get(tab.getIndex() + 1).getTabState();

    if (width < 0)
    {
      throw new IllegalArgumentException("Tab width can not be less than " + ((int) LEFT + RIGHT));
    }

    if (tab.getIndex() == 0)
    {
      xloc = x;
      img = theme.getTabImage(panel.hasFocus(), tabState, TabState.start);

      g.drawImage(img, x, TOP, LEFT, IMG_HEIGHT, null);
      x += LEFT;
    }

    img = theme.getTabImage(panel.hasFocus(), tabState, TabState.center);
    g.drawImage(img, x, TOP, width, IMG_HEIGHT, null);
    x += width;

    img = theme.getTabImage(panel.hasFocus(), tabState, nextTabState);
    g.drawImage(img, x, TOP, RIGHT, IMG_HEIGHT, null);
    x += RIGHT;

    if (tab.getIcon() != null)
    {
      g.drawImage(tab.getIcon().getImage(), xloc + theme.TAB_ICON_INDENT, theme.TAB_ICON_TOP, ICON_SIZE, ICON_SIZE,
          null);
    }

    if (tab.getCaption() != null)
    {
      Rectangle r = g.getClipBounds();
      Paint p = g.getPaint();
      g.setClip(new Rectangle(xloc
          + (tab.getIcon() == null ? theme.TAB_ICON_INDENT + theme.TAB_TEXT_INDENT : theme.TAB_ICON_INDENT + ICON_SIZE
              + theme.TAB_TEXT_INDENT), TOP, width - (tab.getIcon() == null ? -1 : 19), ICON_SIZE + 4));
      GradientPaint painter = new GradientPaint(xloc + LEFT + (width > 70 ? width - 20 : width - 6), 18, new Color(0,
          0, 0, 255), xloc + LEFT + width, 18, new Color(0, 0, 0, 0));
      g.setPaint(painter);
      g.drawString(tab.getCaption(), xloc
          + (tab.getIcon() == null ? theme.TAB_ICON_INDENT + theme.TAB_TEXT_INDENT : theme.TAB_ICON_INDENT + ICON_SIZE
              + theme.TAB_TEXT_INDENT), theme.TAB_TEXT_BASELINE);
      g.setClip(r);
      g.setPaint(p);
    }

   // closeButtonPainter.paint((Graphics2D) g, x - 27);
    return x;
  }

}
