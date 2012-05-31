package com.rendion.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageUtils
{
 // private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

  public static Image load(String path)
  {
    BufferedImage image = null;

    try
    {
      InputStream in = ImageUtils.class.getClassLoader().getResourceAsStream(path);
      image = ImageIO.read(in);
      in.close();
    }
    catch (Exception e)
    {
      throw new RuntimeException("Could not load image with path: " + path, e);
    }

    return image;

  }

  public static ImageIcon loadIcon(String path)
  {
    return new ImageIcon(load(path));

  }

}
