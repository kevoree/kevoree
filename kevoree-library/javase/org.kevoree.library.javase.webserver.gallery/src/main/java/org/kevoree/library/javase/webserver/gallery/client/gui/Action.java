package org.kevoree.library.javase.webserver.gallery.client.gui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Hyperlink;
import org.kevoree.library.javase.webserver.gallery.client.datatypes.Media;
import org.kevoree.library.javase.webserver.gallery.client.extensions.Slideshow;

import java.util.Iterator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

/**
 * This class provides all functions to manage the GUI.
 *
 * @author Benjamin Gr�hbiel
 */

// TODO improve smoothness (improve effects in general)
public class Action extends GUI {

    static void showThumbnails() {
        Slideshow.stopSlideshow();
        Switch.addStyleName("thumbs_active");
        CONTENT.showWidget(1);
    }

    static void showOriginal() {
        Switch.removeStyleName("thumbs_active");
        CONTENT.showWidget(0);
    }

    static void showLoadingAnimation() {
        hideNavigation();
        CONTENT.showWidget(2);
    }

    static void hideLoadingAnimation() {
        CONTENT.showWidget(0);
        showNavigation();
    }

    static void hideNavigation() {
        NAVIGATION.setVisible(false);
    }

    static void showNavigation() {
        NAVIGATION.setVisible(true);
    }

    public static void showNoData() {
        Action.hideNavigation();
        Label message1 = new Label(constants.noalbum());
        Label message2 = new Label(constants.notice1());
        message1.addStyleName("nodata-title");
        message2.addStyleName("nodata-text");

        NODATA.add(message1);
        NODATA.add(message2);
        NODATA.addStyleName("nodata");
        CONTENT.showWidget(3);
    }

    /**
     * @param direction
     */
    public static void changeThumbnail(String direction) {

        if (direction.equals("next")) {
            Element current = DOM.getElementById(CurrentPictureId);
            if (current.getNextSiblingElement() != null) {

                current.removeClassName("active");
                current.getNextSiblingElement().addClassName("active");

                CurrentPictureId = current.getNextSiblingElement().getId();

                if (CONTENT.getVisibleWidget() == 0) {
                    switchOriginal();
                }

            } else {
                showThumbnails();
            }

        } else if (direction.equals("prev")) {
            Element current = (Element) DOM.getElementById(CurrentPictureId);

            if (current.getPreviousSibling() != null) {
                current.removeClassName("active");
                ((Element) current.getPreviousSibling()).addClassName("active");

                CurrentPictureId = ((Element) current.getPreviousSibling()).getId();

                if (CONTENT.getVisibleWidget() == 0) {
                    switchOriginal();
                }

            } else {
                showThumbnails();
            }
        }
    }


    /**
     * This function manages the switch of fullsize image and declarations
     */
    static void switchOriginal() {

        for (Iterator<Media> Fullsize = DataItem.get(currentAlbum).iterator(); Fullsize.hasNext(); ) {
            final Media Image = Fullsize.next();

            if (Image.MediaID.equals(CurrentPictureId)) {

                int imageWidth = Window.getClientWidth() - 200;

                FULLSIZE.setUrl(base + Image.MediaFullsizePath + "&size=" + imageWidth);
                FULLSIZE.setVisible(true);
                showDescription(Image);
            } else {
                DOM.getElementById(Image.MediaID).removeClassName("active");
            }

            Action.showOriginal();
        }
    }


    static void showDescription(Media Image) {

        TOOLBAR.clear();
        TOOLBAR.getElement().setAttribute("filter", "alpha(opacity=0)");
        TOOLBAR.getElement().setAttribute("opacity", "0");

        Anchor desc = new Anchor();
        desc.setHref(Image.getOFullsizePath());
        desc.setText("Download hi-resolution");
        desc.setTarget("_blank");
            TOOLBAR.add(desc);
            TOOLBAR.setVisible(true);

    }
}
