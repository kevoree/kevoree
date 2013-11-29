package com.explodingpixels.macwidgets.plaf;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A collection of utilty method for painting Heads Up Style widgets. See the following for examples
 * of HUD widgets:
 * <ul>
 * <li>{@link com.explodingpixels.macwidgets.plaf.HudButtonUI}</li>
 * <li>{@link com.explodingpixels.macwidgets.plaf.HudCheckBoxUI}</li>
 * <li>{@link com.explodingpixels.macwidgets.plaf.HudComboBoxUI}</li>
 * <li>{@link com.explodingpixels.macwidgets.plaf.HudLabelUI}</li>
 * </ul>
 */
public class HudPaintingUtils {

    public static final float FONT_SIZE = 11.0f;
    public static final Color FONT_COLOR = Color.WHITE;
    public static final Color FONT_DISABLED_COLOR = new Color(0xaaaaaa);

    public static final Color PRESSED_MARK_COLOR = new Color(0, 0, 0, 225);

    private static final Color TOP_COLOR = new Color(170, 170, 170, 50);
    private static final Color BOTTOM_COLOR = new Color(17, 17, 17, 50);
    private static final Color TOP_PRESSED_COLOR = new Color(249, 249, 249, 153);
    private static final Color BOTTOM_PRESSED_COLOR = new Color(176, 176, 176, 153);

    public static final Color BORDER_COLOR = new Color(0xc5c8cf);
    private static final int BORDER_WIDTH = 1;

    private static final Color LIGHT_SHADOW_COLOR = new Color(0, 0, 0, 145);
    private static final Color DARK_SHADOW_COLOR = new Color(0, 0, 0, 50);

    private HudPaintingUtils() {
        // utility class - no constructor needed.
    }

    /**
     * Initializes the given {@link javax.swing.JComponent} as a HUD style widget. This includes setting the
     * font, foreground and opacity of the given component.
     *
     * @param component the component to initialize as a HUD component.
     */
    public static void initHudComponent(JComponent component) {
        component.setFont(HudPaintingUtils.getHudFont());
        component.setForeground(HudPaintingUtils.FONT_COLOR);
        component.setOpaque(false);
    }

    /**
     * Gets the font used by HUD style widgets.
     *
     * @return the font used by HUD style widgets.
     */
    public static Font getHudFont() {
        return UIManager.getFont("Button.font").deriveFont(Font.BOLD, FONT_SIZE);
    }

    /**
     * Gets the number of pixels that a HUD style widget's shadow takes up. HUD button's have a
     * shadow directly below them, that is, there is no top, left or right component to the shadow.
     *
     * @param button the button that the shadow is drawn on.
     * @return the number of pixels that a HUD style widget's shadow takes up.
     */
    public static int getHudControlShadowSize(AbstractButton button) {
        // TODO use the button's font size to figure this out.
        return 2;
    }

    /**
     * Paints a HUD style button background onto the given {@link java.awt.Graphics2D} context using the
     * given {@link com.explodingpixels.macwidgets.plaf.HudPaintingUtils.Roundedness}. The background will be painted from 0,0 to width/height.
     *
     * @param graphics    the graphics context to paint onto.
     * @param button      the button being painted.
     * @param width       the width of the area to paint.
     * @param height      the height of the area to paint.
     * @param roundedness the roundedness to use when painting the background.
     */
    public static void paintHudControlBackground(Graphics2D graphics, AbstractButton button,
                                                 int width, int height, Roundedness roundedness) {
        Paint paint = HudPaintingUtils.createButtonPaint(button, BORDER_WIDTH);
        paintHudControlBackground(graphics, new Rectangle(0, 0, width, height),
                roundedness.getShapeProvider(), paint);
    }

    /**
     * Paints a HUD style background in the given shape. This includes a drop shadow which will be
     * drawn under the shape to be painted. The shadow will be draw outside the given bounds.
     *
     * @param graphics      the {@code Graphics2D} context to draw in.
     * @param bounds        the bounds to paint in.
     * @param shapeProvider the delegate to request the {@link java.awt.Shape} from.
     * @param paint         the {@link java.awt.Paint} to use to fill the {@code Shape}.
     */
    public static void paintHudControlBackground(Graphics2D graphics, Rectangle bounds,
                                                 ShapeProvider shapeProvider, Paint paint) {

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // TODO replace with real drop shadow painting.

        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        graphics.setColor(LIGHT_SHADOW_COLOR);
        graphics.draw(shapeProvider.createShape(x, y, width - 1, height));

        graphics.setColor(DARK_SHADOW_COLOR);
        graphics.draw(shapeProvider.createShape(x, y, width - 1, height + 1));

        graphics.setPaint(paint);
        graphics.fill(shapeProvider.createShape(x, y + 1, width, height - 1));

        graphics.setColor(BORDER_COLOR);
        graphics.draw(shapeProvider.createShape(x, y, width - 1, height - 1));
    }

    private static Paint createButtonPaint(AbstractButton button, int lineBorderWidth) {
        boolean isPressed = button.getModel().isPressed();
        Color topColor = isPressed ? TOP_PRESSED_COLOR : TOP_COLOR;
        Color bottomColor = isPressed ? BOTTOM_PRESSED_COLOR : BOTTOM_COLOR;
        int bottomY = button.getHeight() - lineBorderWidth * 2;
        return new GradientPaint(0, lineBorderWidth, topColor, 0, bottomY, bottomColor);
    }

    /**
     * Installs an {@link java.awt.AlphaComposite} on the given {@link java.awt.Graphics2D) if the given
     * {@link java.awt.Component} is disabled.
     *
     * @param graphics  the {@code Graphics2D} to adjust.
     * @param component the {@code Component} whos enablement state should be queried.
     */
    public static void updateGraphicsToPaintDisabledControlIfNecessary(Graphics2D graphics,
                                                                       Component component) {
        if (!component.isEnabled()) {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }
    }

    /**
     * An enumeration representing the roundness styles of HUD buttons.
     */
    public enum Roundedness {

        ROUNDED_BUTTON(.95), COMBO_BUTTON(0.45), CHECK_BOX(0.4), RADIO(1.0), SLIDER_KNOB(1.0);

        private final double fRoundedPercentage;
        private final ShapeProvider fShapeProvider;

        private Roundedness(double roundedPercentage) {
            fRoundedPercentage = roundedPercentage;
            fShapeProvider = createShapeProvider();
        }

        private int getRoundedDiameter(int controlHeight) {
            // TODO make roudedness based on font size rather than component height.
            int roundedDiameter = (int) (controlHeight * fRoundedPercentage);
            int makeItEven = roundedDiameter % 2;
            return roundedDiameter - makeItEven;
        }

        private ShapeProvider getShapeProvider() {
            return fShapeProvider;
        }

        private ShapeProvider createShapeProvider() {
            return new ShapeProvider() {
                public Shape createShape(double x, double y, double width, double height) {
                    double arcDiameter = getRoundedDiameter((int) height);
                    return new RoundRectangle2D.Double(x, y, width, height, arcDiameter,
                            arcDiameter);
                }
            };
        }
    }

    public interface ShapeProvider {
        Shape createShape(double x, double y, double width, double height);
    }

}
