package org.kevoree.library.javase.webserver.gallery.client.gui;

import org.kevoree.library.javase.webserver.gallery.client.KevoreeGWTGallery;
import org.kevoree.library.javase.webserver.gallery.client.Model;

import com.google.gwt.user.client.Window;

public class Loader extends KevoreeGWTGallery {

    public Loader() {
        this.setLanguage();
        this.setAlbum();
        new Model();
    }

    void setLanguage() {
        KevoreeGWTGallery.language = "en";
    }

    void setAlbum() {
        String getAlbum = Window.Location.getParameter("album");
        if (getAlbum == null) {
            KevoreeGWTGallery.currentAlbum = "alls";
        } else {
            KevoreeGWTGallery.currentAlbum = getAlbum;
        }
    }

}
