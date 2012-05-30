package com.rendion.jchrome.painters;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.rendion.jchrome.JChromeTabbedPane;
import com.rendion.jchrome.Tab;

public class TabRowPainter
{
  private final BorderPainter borderPainter;
  //private final AdderPainter adderPainter;
  private final JChromeTabbedPane panel;
  private final Theme theme;
  private Resize resize = Resize.MAX_WIDTH;
  private int tabCount;
  private int width;
  private int x;
  private int start;
  private int content;
  private int constant;
  private int flex;
  private int rem;
  private int end;

  private enum Resize
  {
    MAX_WIDTH, RIGHT, LEFT;
  }

  final ArrayList<TabPainter> tabPainters = new ArrayList<TabPainter>(100);

  public TabRowPainter(JChromeTabbedPane panel)
  {
    this.panel = panel;
    this.theme = panel.getTheme();
    flex = theme.TAB_WIDTH;
    borderPainter = new BorderPainter(theme);
   // adderPainter = new AdderPainter(panel);
  }

  public void addTab(Tab t)
  {
    tabPainters.add(new TabPainter(panel, this, t));
    resize = Resize.MAX_WIDTH;
  }

  public void removeTab(int index)
  {
    tabPainters.remove(index);
    if (index == tabPainters.size())
    {
      resize = Resize.RIGHT;
    }
    else
    {
      resize = Resize.LEFT;
    }

  }

  public void paint(Graphics2D g)
  {
    x = start = panel.getLeftMargin();
    width = ((int) g.getClipBounds().getWidth());
    tabCount = tabPainters.size();
    constant = tabCount == 0 ? 0 : TabPainter.LEFT + tabCount * TabPainter.RIGHT;
    content = width - (panel.getLeftMargin() + panel.getRightMargin() + AdderPainter.WIDTH + AdderPainter.OFFSET);

    if (tabCount == 0)
    {
      flex = 0;
      rem = 0;
      end = start;
    }
    else
    {
      // everything fits paint at max width regardless of mode
      if ((content - constant) / tabCount >= 179)
      {
        resize = Resize.MAX_WIDTH;
        flex = theme.TAB_WIDTH;
        rem = 0;
      }
      else
      {
        if (resize == Resize.RIGHT)
        {
          content = end - start;
        }

        // this actually resizes the tabs valid for RESIZE_TO_RIGHT and
        // RESIZE_TO_MAX_WIDTH
        if (resize != Resize.LEFT)
        {
          flex = (content - constant) / tabCount;
          rem = (content - constant) % tabCount;
        }
      }

      end = start + constant + (flex * tabCount) + rem;

    }

    for (int i = 0; i < tabCount; i++)
    {
      x = tabPainters.get(i).paint((Graphics2D) g, x, flex + (i < rem ? 1 : 0));
    }

   // adderPainter.paint((Graphics2D) g, end + AdderPainter.OFFSET);
    borderPainter.paint((Graphics2D) g, 0, theme.TABROW_HEIGHT, start - 1, panel.hasFocus());
    borderPainter.paint((Graphics2D) g, end, theme.TABROW_HEIGHT, width - end, panel.hasFocus());

  }

  public boolean mousePressed(MouseEvent e)
  {

    boolean repaint = false;//adderPainter.mousePressed(e);

    if (!repaint)
    {
      for (TabPainter tab : tabPainters)
      {
        repaint = tab.mousePressed(e);
        if (repaint)
        {
          break;
        }
      }
    }

    return repaint;

  }

  public boolean mouseMoved(MouseEvent e)
  {
    boolean repaint = false;//adderPainter.mouseMoved(e);

    if (!repaint)
    {
      for (TabPainter tab : tabPainters)
      {
        repaint = tab.mouseMoved(e);
        if (repaint)
        {
          break;
        }
      }
    }

    if (!repaint)
    {
      if (e.getY() > 24 && resize != Resize.MAX_WIDTH)
      {
        resize = Resize.MAX_WIDTH;
        repaint = true;
        try
        {
          // take it easy on the user so they know this change wasnt an accident
          Thread.sleep(250);
        }
        catch (Exception ex)
        {
        }
      }

    }
    return repaint;
  }

  public boolean mouseReleased(MouseEvent e)
  {
    boolean repaint = false;//adderPainter.mouseReleased(e);

    if (!repaint)
    {
      for (TabPainter tab : tabPainters)
      {
        repaint = tab.mouseReleased(e);
        if (repaint)
        {
          break;
        }
      }
    }

    return repaint;

  }

}
