package org.kevoree.library.javase.kinect;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;


/**
 * <p>Manager for a greyscale alpha mask which can be used to blend
 * bitmaps onto a surface.</p>
 * <p> </p>
 * <p> Copyright (c) 2000-2002, David J. Yazel</p>
 * <p> Teseract Software, LLP</p>
 * @author David Yazel
 *
 */
public class DirectBufferedImage extends BufferedImage {

    public void setDirectType(int directType) {
        this.directType = directType;
    }

    public static final int DIRECT_RGB = 0;
    public static final int DIRECT_RGBA = 1;
    public static final int DIRECT_GRAY = 2;

    byte []data;

    public int getDirectType() {
        return directType;
    }

    int directType;

    private DirectBufferedImage (int type, byte[] buffer, ColorModel model, WritableRaster raster,
			boolean rasterPremultiplied) {
        super(model,raster,rasterPremultiplied,null);
        this.data = buffer;
        this.directType = type;
    }

    public static DirectBufferedImage make(int width, int height, int type) {

        switch(type) {
            case DIRECT_RGB:
                return (DirectBufferedImage)getDirectImageRGB(width,height);
            case DIRECT_RGBA:
                return (DirectBufferedImage)getDirectImageRGBA(width,height);
            case DIRECT_GRAY:
                return (DirectBufferedImage)getDirectImageGrey(width,height);
        }
        throw new Error("Unknown direct image type "+type);
    }

    public byte[] getBackingStore() {
        return data;
    }

    /**
     * creates a writable raster which is backed by the specified byteBuffer
     * @param width Width of the image
     * @param height Height of the image
     * @param pixelBytes Number of bytes in a pixel, usually 4
     * @param byteBuffer Buffer to use as backing store
     * @return
     */
    public WritableRaster getDirectRaster(int width, int height, int pixelBytes, ByteBuffer byteBuffer) {

        int[] bandOffset = { 0 };

        DataBufferByte buffer = new DataBufferByte(byteBuffer.array() , width * height * pixelBytes);

        WritableRaster newRaster = Raster.createInterleavedRaster(buffer, width,
                height, width, 1, bandOffset, null);

        return newRaster;
    }

    public static BufferedImage getCustomRGB(int width, int height) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = { 8, 8, 8 };
        ColorModel cm = new ComponentColorModel(cs, nBits, false, false,
                Transparency.OPAQUE, 0);
        int[] bandOffset = { 0, 1, 2 };

        WritableRaster newRaster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                width, height, width * 3, 3, bandOffset, null);
        BufferedImage newImage = new BufferedImage(cm, newRaster, false, null);

        return newImage;
    }

    public static BufferedImage getCustomRGBA(int width, int height) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = { 8, 8, 8, 8 };
        ColorModel cm = new ComponentColorModel(cs, nBits, true, false,
                Transparency.OPAQUE, 0);
        int[] bandOffset = { 0, 1, 2, 3 };

        WritableRaster newRaster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                width, height, width * 4, 4, bandOffset, null);
        BufferedImage newImage = new BufferedImage(cm, newRaster, false, null);

        return newImage;
    }

    /**
     * Creates a buffered image which is backed by a NIO byte buffer
     * @return
     */
    public static DirectBufferedImage getDirectImageRGB(int width, int height) {
        return getDirectImageRGB(width, height, null);
    }

    /**
     * Creates a buffered image which is backed by a NIO byte buffer
     * @return
     */
    public static DirectBufferedImage getDirectImageRGB(int width, int height, byte[] backingStore) {

        int[] bandOffset = { 0, 1, 2 };

        // create the backing store

        byte bb[] = (backingStore == null) ? new byte[width*height*3] : backingStore;

        // create a data buffer wrapping the byte array

        DataBuffer buffer = new DataBufferByte(bb,width * height * 3);

        // build the raster with 3 bytes per pixel

        WritableRaster newRaster = Raster.createInterleavedRaster(buffer, width,
                height, width*3, 3, bandOffset, null);

        // create a standard sRGB color space
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        // create a color model which has three 8 bit values for RGB
        int[] nBits = { 8, 8, 8 };
        ColorModel cm = new ComponentColorModel(cs, nBits, false, false,
                Transparency.OPAQUE, 0);

        // create the buffered image

        DirectBufferedImage newImage = new DirectBufferedImage(DIRECT_RGB,bb,cm, newRaster, false);

        return newImage;
    }

    public static DirectBufferedImage getDirectImageGrey(int width, int height) {

        int[] bandOffset = { 0 };

        // create the backing store

        byte bb[] = new byte[width*height];

        // create a data buffer wrapping the byte array

        DataBuffer buffer = new DataBufferByte(bb,width * height );

        // build the raster with 3 bytes per pixel

        WritableRaster newRaster = Raster.createInterleavedRaster(buffer, width,
                height, width, 1, bandOffset, null);

        // create a standard sRGB color space
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);

        // create a color model which has three 8 bit values for RGB
        int[] nBits = { 8 };
        ColorModel cm = new ComponentColorModel(cs, nBits, false, false,
                Transparency.OPAQUE, 0);

        // create the buffered image

        DirectBufferedImage newImage = new DirectBufferedImage(DIRECT_GRAY,bb,cm, newRaster, false);

        return newImage;
    }

    /**
     * Creates a buffered image which is backed by a NIO byte buffer
     * @return
     */
    public static DirectBufferedImage getDirectImageRGBA(int width, int height) {

        int[] bandOffset = { 0, 1, 2, 3 };

        // create the backing store

        byte bb[] = new byte[width*height*4];

        // create a data buffer wrapping the byte array

        DataBufferByte buffer = new DataBufferByte(bb,width * height * 4);

        // build the raster with 4 bytes per pixel

        WritableRaster newRaster = Raster.createInterleavedRaster(buffer, width,
                height, width*4, 4, bandOffset, null);

        // create a standard sRGB color space
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        // create a color model which has three 8 bit values for RGB
        int[] nBits = { 8, 8, 8, 8 };
        ColorModel cm = new ComponentColorModel(cs, nBits, true, false,
                Transparency.TRANSLUCENT, 0);

        // create the buffered image

        DirectBufferedImage newImage = new DirectBufferedImage(DIRECT_RGBA,bb, cm, newRaster, false);
        return newImage;
    }

    /**
     * takes the source buffered image and converts it to a buffered image
     * which is backed by a direct byte buffer
     * @param source
     * @return
     */
    static DirectBufferedImage makeDirectImageRGB( BufferedImage source ) {

        DirectBufferedImage dest = getDirectImageRGB(source.getWidth(), source.getHeight());
        source.copyData(dest.getRaster());
        return dest;
    }

    /**
     * takes the source buffered image and converts it to a buffered image
     * which is backed by a direct byte buffer
     * @param source
     * @return
     */

    static DirectBufferedImage makeDirectImageRGBA( BufferedImage source ) {
        DirectBufferedImage dest = getDirectImageRGBA(source.getWidth(), source.getHeight());
        source.copyData(dest.getRaster());
        return dest;
    }

    private static DirectBufferedImage convertViaDrawing( BufferedImage source, DirectBufferedImage dest ) {
        Graphics2D g = (Graphics2D)dest.getGraphics();
        g.drawImage(source,0,0,dest.getWidth(),dest.getHeight(),null);
        return dest;
    }

    public static DirectBufferedImage make(BufferedImage bi) {
        return make(bi,false);
    }

    public static DirectBufferedImage make(BufferedImage bi, boolean expectAlpha) {

        boolean hasAlpha = bi.getColorModel().hasAlpha() && !bi.getColorModel().isAlphaPremultiplied();
        if (expectAlpha && hasAlpha) {
//            System.out.println("   as alpha");
            return convertViaDrawing(bi,getDirectImageRGBA(bi.getWidth(),bi.getHeight()));
        } else {
//            System.out.println("   as non-alpha");
            return convertViaDrawing(bi,getDirectImageRGB(bi.getWidth(),bi.getHeight()));
        }

        /*
//        return convertViaDrawing(bi,getDirectImageRGBA(bi.getWidth(),bi.getHeight()));

            switch (bi.getType()) {
                case BufferedImage.TYPE_CUSTOM:
//                    return convertViaDrawing(bi,getDirectImageRGBA(bi.getWidth(),bi.getHeight()));
                    if (bi.getColorModel().hasAlpha())
                        return makeDirectImageRGBA(bi);
                    else
                    return makeDirectImageRGB(bi);
                case BufferedImage.TYPE_INT_ARGB:
                    return makeDirectImageRGBA(bi);
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_INT_RGB:
                    return makeDirectImageRGB(bi);
                default:
                    if (bi.getColorModel().hasAlpha())
                        return convertViaDrawing(bi,getDirectImageRGBA(bi.getWidth(),bi.getHeight()));
                    else return convertViaDrawing(bi,getDirectImageRGBA(bi.getWidth(),bi.getHeight()));
//                    throw new java.io.IOException("cannot convert this buffered image to direct "+name+" because it is type "+bi.getType());
            }
          */
    }

    /**
     * reads in an image using image io.  It then detects if this is a RGBA or RGB image
     * and converts it to the appropriate direct image.  Unfortunly this does mean
     * we are loading a buffered image which is thrown away, but there is no help
     * for that currently.
     * @param name
     * @return
     * @throws java.io.IOException
     */
    public static BufferedImage loadDirectImage(String name, boolean expectAlpha) throws java.io.IOException {

//        System.out.println("Loading "+name);
        File f = new File(name);
        BufferedImage bi = ImageIO.read(f);
        return make(bi, expectAlpha);
    }

    public static BufferedImage loadDirectImage(String name) throws java.io.IOException {
        return loadDirectImage(name,false);
    }

    /**
     * reads in an image using image io.  It then detects if this is a RGBA or RGB image
     * and converts it to the appropriate direct image.  Unfortunly this does mean
     * we are loading a buffered image which is thrown away, but there is no help
     * for that currently.
     * @return
     * @throws java.io.IOException
     */
    public static BufferedImage loadDirectImage(URL url, boolean expectAlpha) throws java.io.IOException {

            BufferedImage bi = ImageIO.read(url);
            return make(bi, expectAlpha);
    }

    public static BufferedImage loadDirectImage(URL url) throws java.io.IOException {
        return loadDirectImage(url,false);
    }
}