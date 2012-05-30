package com.rendion.jchrome;

import javax.swing.*;

public class Tab
{
  private int index;
  private ImageIcon icon = null;
  private String caption = "New Tab";
  private boolean isSelected = false;

  // private final JChromeTabbedPane panel;

  Tab(JChromeTabbedPane p)
  {
    // panel = p;
  }

  public int getIndex()
  {
    return index;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public ImageIcon getIcon()
  {
    return icon;
  }

  public void setIcon(ImageIcon icon)
  {
    this.icon = icon;
  }

  public String getCaption()
  {
    return caption;
  }

  public void setCaption(String caption)
  {
    this.caption = caption;
  }

  public boolean isSelected()
  {
    return isSelected;
  }

  // todo fireTabSelectionChanged
  public void setSelected(boolean isSelected)
  {
    this.isSelected = isSelected;
  }

    private JComponent internPanel;

    public JComponent getInternPanel() {
        return internPanel;
    }

    public void setInternPanel(JComponent internPanel) {
        this.internPanel = internPanel;
    }
}
