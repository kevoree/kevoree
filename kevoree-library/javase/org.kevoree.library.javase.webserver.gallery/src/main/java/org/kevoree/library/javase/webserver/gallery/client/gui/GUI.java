package org.kevoree.library.javase.webserver.gallery.client.gui;

import com.google.gwt.user.client.ui.*;
import org.kevoree.library.javase.webserver.gallery.client.KevoreeGWTGallery;
import org.kevoree.library.javase.webserver.gallery.client.datatypes.Media;

import java.util.*;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

public class GUI extends KevoreeGWTGallery {

    public static VerticalPanel HMENU_LEFT = new VerticalPanel();
    public static Label TkLogo = new Label("TkLogo");
    public static HorizontalPanel HMENU = new HorizontalPanel();

    public static VerticalPanel PLUGIN = new VerticalPanel();
    public static FocusPanel NAVIGATIONWRAP = new FocusPanel();
    public static HorizontalPanel NAVIGATION = new HorizontalPanel();
    public static Label Switch = new Label("Thumbs");
    public static Label Prev = new Label("Prev");
    public static Label Next = new Label("Next");
    public static Label Down = new Label("Down");

    public static DeckPanel CONTENT = new DeckPanel();
    public static VerticalPanel DATA = new VerticalPanel();

    public static FocusPanel HANDLERPANEL = new FocusPanel();
    public static VerticalPanel ORIGINAL = new VerticalPanel();
    public static AbsolutePanel IMAGE = new AbsolutePanel();
    public static HorizontalPanel TOOLBAR = new HorizontalPanel();
    public static SimplePanel SLIDESHOW = new SimplePanel();
    public static Image FULLSIZE = new Image();

    public static HorizontalPanel THUMBNAILVIEW = new HorizontalPanel();
    public static FlowPanel THUMBNAILS = new FlowPanel();

    public static VerticalPanel LOADER = new VerticalPanel();
    public static VerticalPanel NODATA = new VerticalPanel();

    public static Image loadingGif = new Image("ajax-loader.gif");
    public static String CurrentPictureId;

    public GUI() {
        build();
        resize();
        Action.showLoadingAnimation();
    }

    public void loadfinish(HashMap<String, ArrayList<Media>> dataItems) {
        ArrayList<String> menu = new ArrayList<String>();
        for (String key : dataItems.keySet()) {
            menu.add(key);
        }
        populateMenu(menu);
        populate(dataItems.get(currentAlbum));
    }

    /**
     * build() which is responsible for creating all public widget for the markup.
     */
    void build() {

        HMENU_LEFT.setStyleName("navigationleft");


        HMENU.add(HMENU_LEFT);
        // HMENU_LEFT.setWidth("100px");
        HMENU.add(PLUGIN);


        PLUGIN.add(NAVIGATIONWRAP);
        PLUGIN.add(CONTENT);

        PLUGIN.setStyleName("plugin");

        /* navigation */
        Switch.setStyleName("thumbs");
        Switch.setTitle(constants.next());
        Switch.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (CONTENT.getVisibleWidget() == 0) {
                    Action.showThumbnails();
                } else if (CONTENT.getVisibleWidget() == 1) {
                    Action.showOriginal();
                }
            }
        });

        Prev.setStyleName("prev");
        Prev.setTitle(constants.previous());
        Prev.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Action.changeThumbnail("prev");
            }
        });

        Next.setStyleName("next");
        Next.setTitle(constants.next());
        Next.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Action.changeThumbnail("next");
            }
        });

        NAVIGATIONWRAP.add(NAVIGATION);
        NAVIGATIONWRAP.addStyleName("navigationwrap");
        NAVIGATIONWRAP.addKeyUpHandler(EventHandler.Keyboard);
        NAVIGATIONWRAP.setHeight("70px");

        PLUGIN.setCellHeight(NAVIGATIONWRAP, "70px");

        NAVIGATION.add(Prev);
        NAVIGATION.add(Switch);
        NAVIGATION.add(Next);

        NAVIGATION.setStyleName("navigation");

        CONTENT.addStyleName("content");
        CONTENT.add(DATA);                             // 0
        CONTENT.add(THUMBNAILVIEW);                 // 1
        CONTENT.add(LOADER);                        // 2
        CONTENT.add(NODATA);                        // 3

        NODATA.setWidth("auto");

        LOADER.addStyleName("loader");
        LOADER.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        LOADER.add(loadingGif);

        DATA.add(HANDLERPANEL);
        DATA.setStyleName("data");

        HANDLERPANEL.add(ORIGINAL);
        HANDLERPANEL.addKeyUpHandler(EventHandler.Keyboard);
        HANDLERPANEL.setWidth("100%");

        THUMBNAILVIEW.add(THUMBNAILS);

        THUMBNAILS.addStyleName("thumbnails");

        ORIGINAL.add(IMAGE);
        ORIGINAL.setStyleName("original");

        IMAGE.setStyleName("image");
        IMAGE.add(TOOLBAR);
        IMAGE.add(FULLSIZE);
        //IMAGE.add(SLIDESHOW);

        SLIDESHOW.setStyleName("slideshow");

        FULLSIZE.addStyleName("fullsize");
        FULLSIZE.addClickHandler(EventHandler.OriginalListener);

        TOOLBAR.setHeight("30px");
        TOOLBAR.setWidth("100%");
        TOOLBAR.setStyleName("toolbar");

        RootPanel.get("gwtgall").add(HMENU);

        // TODO solve setFocus problem. only works in opera.
        HANDLERPANEL.setFocus(true);

        Window.addResizeHandler(EventHandler.BrowserResized);
    }

    /**
     * this function adjusts height and width of the fullsize image to avoid scrollbars after user resized his window.
     */
    static void resize() {

        int ClientWidth = Window.getClientWidth();
        int ClientHeight = Window.getClientHeight();

        HMENU.setWidth(ClientWidth - 120 + "px");
        HMENU.setHeight(ClientHeight + "px");

        int LowerGallery = HMENU.getElement().getClientHeight() - 70;
        int newHeight = LowerGallery - 100;

        FULLSIZE.setHeight(newHeight + "px");
    }

    static Label previousSelected = null;

    static void populateMenu(List<String> menu) {

        Image tklogo = new Image();
        tklogo.setUrl("TK_LOGO.jpg");

        HMENU_LEFT.add(tklogo);
        for (final String label : menu) {
            final Label bt = new Label(label);
            bt.setStyleName("navigationleftlabel");
            bt.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    currentAlbum = label;
                    populate(DataItem.get(label));
                    if (previousSelected != null) {
                        previousSelected.setStyleName("navigationleftlabel");
                    }
                    bt.setStyleName("navigationleftlabelsel");
                    previousSelected = bt;
                }
            });
            if (currentAlbum.equals(label)) {
                bt.setStyleName("navigationleftlabelsel");
                previousSelected = bt;
            }

            HMENU_LEFT.add(bt);
        }
    }

    static ArrayList<Widget> previousThumb = new ArrayList<Widget>();

    /**
     * @param dataItem populate() loads all the data generated by Model into the GUI.
     */
    static void populate(ArrayList<Media> dataItem) {

        for (Widget w : previousThumb) {
            THUMBNAILS.remove(w);
        }
        previousThumb.clear();

        int counter = 1;

        for (Iterator<Media> ImageData = dataItem.iterator(); ImageData.hasNext(); ) {

            final Media i = ImageData.next();

            FULLSIZE.getElement().setId("fullsize-" + i.getID());

            final Image Thumbnail = new Image(base + i.MediaThumbnailPath, 0, 0, 73, 73);

            Thumbnail.addClickHandler(EventHandler.ThumbnailListener);
            Thumbnail.addMouseOverHandler(EventHandler.ThumbnailMouseOver);
            Thumbnail.addMouseOutHandler(EventHandler.ThumbnailMouseOut);
            Thumbnail.addStyleName("thumbnail");
            Thumbnail.getElement().setId(i.getID());

            if (counter == 1) {
                //Window.alert("urlimg="+base+i.MediaFullsizePath);
                // FULLSIZE.setUrl(base + i.MediaFullsizePath);

                // TODO Preloader
                // hideLoadingAnimation as soon as first image is loaded completely
                /*
              FULLSIZE.addLoadHandler(new LoadHandler() {
                  public void onLoad(LoadEvent event) {
                      Action.hideLoadingAnimation();
                  }
              });  */

                //Thumbnail.setStyleName("active");
                THUMBNAILS.add(Thumbnail);
                previousThumb.add(Thumbnail);
                /* define class variables */

                // Window.alert("currentMEDIAID="+i.MediaID);

                // CurrentPictureId = i.MediaID;

                /* show description */
                //Action.showDescription(i);

            } else {
                THUMBNAILS.add(Thumbnail);
                previousThumb.add(Thumbnail);
            }

            counter++;
        }
        Action.showThumbnails();
        Action.showNavigation();
    }

}