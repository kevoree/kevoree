package org.kevoree.library.javase.webserver.gallery.client;

import org.kevoree.library.javase.webserver.gallery.client.datatypes.Media;
import org.kevoree.library.javase.webserver.gallery.client.gui.GUI;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;

import java.util.ArrayList;

public class Model extends KevoreeGWTGallery {

    public Model() {

        getAlbums();
    }

    void getAlbums() {
        final GUI  gui = new GUI();
        String url = base + "script/albums.php";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert("Could not load albums data.");
                }

                public void onResponseReceived(Request request, Response response) {
                    String body = response.getText();
                    if (body.startsWith("<dirs>")) {
                        body = body.replace("<dirs>", "");
                        body = body.replace("</dirs>", "");
                        String[] albumsDir = body.split(";");
                        for (int i = 0; i < albumsDir.length; i++) {
                            if(i == 0){currentAlbum = albumsDir[i];}
                            getAlbum(gui,albumsDir[i], (i == (albumsDir.length - 1)));
                        }
                    }
                }

            });
        } catch (RequestException e) {
            Window.alert(e.getLocalizedMessage());
        }

    }


    /**
     * this function gets all the images of a certain album in json format.
     *
     * @param albumid
     */
    ArrayList<Media> getAlbum(final GUI gui,final String albumid, final boolean callGUI) {

        String url = base + "script/album.php?filename=" + albumid;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert("Could not load server data.");
                }

                public void onResponseReceived(Request request, Response response) {
                    if (!response.getText().equals("{}")) {
                        if (response.getStatusCode() == 200) {
                            JSONValue JValue = JSONParser.parse(response.getText());
                            JSONObject JObject = JValue.isObject();
                            ArrayList<Media> datas = new ArrayList<Media>();
                            for (int i = 0; i < JObject.size(); i++) {
                                JSONValue Iterator = JObject.get("" + i);
                                JSONObject NewObject = Iterator.isObject();

                                JSONValue Thumbnail = NewObject.get("thumbnail");
                                JSONValue Fullsize = NewObject.get("fullsize");
                                JSONValue OFullsize = NewObject.get("ofullsize");
                                JSONValue photoId = NewObject.get("id");
                                //JSONValue descD = NewObject.get("desc_de");
                                //JSONValue descE = NewObject.get("desc_en");

                                String ThumbnailPath = Thumbnail.toString().replaceAll("\"", "");
                                String FullsizePath = Fullsize.toString().replaceAll("\"", "");
                                String OFullsizePath = OFullsize.toString().replaceAll("\"", "");
                                //String descDe = descD.toString().replaceAll("\"", "");
                                ;
                                // String descEn = descE.toString().replaceAll("\"", "");
                                ;
                                String Id = photoId.toString().replaceAll("\"", "");
                                ;

                                new Image(ThumbnailPath, 0, 0, 73, 73);
                                new Image(FullsizePath);

                                datas.add(new Media(Id, ThumbnailPath, FullsizePath,OFullsizePath));
                            }
                            DataItem.put(albumid, datas);

                        }
                    }
                    if (callGUI) {
                        gui.loadfinish(DataItem);
                    }
                }
            });
        } catch (RequestException e) {

        }
        return null;
    }
}
