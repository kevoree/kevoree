package org.kevoree.library.javase.webserver.gallery.client.gui;

import org.kevoree.library.javase.webserver.gallery.client.extensions.Slideshow;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.Image;

public class EventHandler extends GUI {
	
	static ClickHandler ThumbnailListener = new ClickHandler() {
		public void onClick(ClickEvent event) {
			Object Sender = event.getSource();
			if(Sender instanceof Image) {	
				Image Thumbnail = (Image) Sender;
				
				/* update image Id and Position */
				String Id = Thumbnail.getElement().getId();
				CurrentPictureId = Id;
				
				Action.switchOriginal();
				Thumbnail.setStyleName("active");
				Action.showOriginal();
			}
		}
	};
	
	static ClickHandler OriginalListener = new ClickHandler() {
		public void onClick(ClickEvent event) {
			Action.changeThumbnail("next");
		}
	};
	
	static MouseOverHandler ThumbnailMouseOver = new MouseOverHandler() {
		public void onMouseOver(MouseOverEvent event) {
			Object Sender = event.getSource();
			if(Sender instanceof Image) {	
				Image Thumbnail = (Image) Sender;
				Effects.ThumbnailMouseOver(Thumbnail);
			}
		}
	};
	
	static MouseOutHandler ThumbnailMouseOut = new MouseOutHandler() {
		public void onMouseOut(MouseOutEvent event) {
			Object Sender = event.getSource();
			if(Sender instanceof Image) {	
				Image Thumbnail = (Image) Sender;
				Effects.ThumbnailMouseOut(Thumbnail);
			}
		}
	};
	
	/* adjust image width */
	static ResizeHandler BrowserResized = new ResizeHandler() {
		public void onResize(ResizeEvent event) {
			Action.resize();
		}
	};
	
	static  KeyUpHandler Keyboard = new KeyUpHandler() {
		public void onKeyUp(KeyUpEvent event) {
			
			Slideshow.stopSlideshow();
			
			int KeyCode = event.getNativeKeyCode();
			
			if (KeyCode == KeyCodes.KEY_RIGHT) {
				Action.changeThumbnail("next");
		    } 
			else if (KeyCode == KeyCodes.KEY_LEFT) {
		    	Action.changeThumbnail("prev");
		    }
		    else if (KeyCode == KeyCodes.KEY_UP) {
		    	Action.showThumbnails();
		    } 
		    else if(KeyCode == KeyCodes.KEY_ENTER) {
		    	if(CONTENT.getVisibleWidget() == 0) {
					Action.showThumbnails();
				} else if(CONTENT.getVisibleWidget() == 1) {
					Action.switchOriginal();
				}
		    } 
		    else if(KeyCode == 32) {
		    	Slideshow.handle();
		    }
		}
	};

}
