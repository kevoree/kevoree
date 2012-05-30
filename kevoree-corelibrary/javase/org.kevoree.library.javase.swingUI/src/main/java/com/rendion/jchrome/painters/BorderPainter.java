package com.rendion.jchrome.painters;

import java.awt.Graphics2D;

public class BorderPainter
{
  private Theme theme;

  public BorderPainter(Theme theme)
  {
    this.theme = theme;
  }

  public void paint(Graphics2D g, int x, int y, int width, boolean hasFocus)
  {
    g.setColor(hasFocus ? theme.BORDER_COLOR : theme.BORDER_COLOR_NOFOCUS);
    g.drawLine(x, y, x + width+theme.BORDER_EXTEND, y);

  }
}
