package org.kevoree.library.javase.webserver.gallery.client.datatypes;

import org.kevoree.library.javase.webserver.gallery.client.KevoreeGWTGallery;

public class Media extends KevoreeGWTGallery {

    public String MediaThumbnailPath;
    public String MediaFullsizePath;
    public String MediaOFullsizePath;

    public String MediaID;

    //	public String MediaDescriptionDe;
//		public String MediaDescriptionEn;

    public Media() {
    }

    public Media(
            String id,
            String thumbnailPath,
            String fullsizePath,
            String ofullsizePath
    ) {

        this.MediaID = id;
        this.MediaFullsizePath = fullsizePath;
        this.MediaThumbnailPath = thumbnailPath;
        this.MediaOFullsizePath = ofullsizePath;
        //	this.MediaDescriptionDe = descDe;
        //	this.MediaDescriptionEn = descEn;

    }

    public String getFullsizePath() {
        return this.MediaFullsizePath;
    }

    public String getOFullsizePath() {
        return this.MediaOFullsizePath;
    }

    public String getThumbnailPath() {
        return this.MediaThumbnailPath;
    }

    public String getID() {
        return this.MediaID;
    }
    /*
         public String getDescriptionDe() {
             return this.MediaDescriptionDe;
         }

         public String getDescriptionEn() {
             return this.MediaDescriptionEn;
         }   */

}