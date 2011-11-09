package org.kevoree.library.javase.webserver.gallery.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.kevoree.library.javase.webserver.gallery.client.datatypes.Media;
import org.kevoree.library.javase.webserver.gallery.client.gui.Loader;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;


public class KevoreeGWTGallery implements EntryPoint {

    /* preferences */
    public static String language;
    public static String currentAlbum;

    public static String base = GWT.getModuleBaseURL()+"../";
    //public static String base = "http://127.0.0.1:9888/takoon/";

    public static GWTGalleriaTranslations constants = GWT.create(GWTGalleriaTranslations.class);

    /**
     * DataItem stores all Media Objects (see below) in a ArrayList
     */
    public static HashMap<String, ArrayList<Media>> DataItem = new HashMap<String, ArrayList<Media>>();

    /**
     * Media Object contains all informations about an image.
     * id | thumbnailPath | fullsizePath | descDe | descEn
     */
    public static Media Media;

    public void onModuleLoad() {
        /* start loading data for gallery */

        final RootPanel loginPanel = RootPanel.get("gwtlogin");
        VerticalPanel loginLayout = new VerticalPanel();
        Image img = new Image("tkfamily.jpg");
        loginLayout.add(img);
        final TextBox login = new TextBox();
        final PasswordTextBox pass = new PasswordTextBox();
        Button loginBT = new Button("Login");
        loginBT.setStyleName("btn");
        loginBT.addStyleName("primary");
        loginLayout.add(new Label("Login : "));
        loginLayout.add(login);
        loginLayout.add(new Label("Pasword : "));
        loginLayout.add(pass);
        loginLayout.add(loginBT);
        loginPanel.add(loginLayout);
        final Label logresult = new Label();
        logresult.setStyleName("alert-message");
        logresult.addStyleName("error");
        loginPanel.add(logresult);

        loginBT.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {

                String url = base + "script/login.php?user="+login.getText()+"&pass="+pass.getText();
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
                try {
                    builder.sendRequest(null, new RequestCallback() {
                        public void onError(Request request, Throwable exception) {
                            logresult.setText("Error while connecting to server");
                        }

                        public void onResponseReceived(Request request, Response response) {
                            if (response.getText().equals("<loginack />")) {
                                loginPanel.setVisible(false);
                                new Loader();
                            } else {
                                logresult.setText(response.getText());
                            }
                        }
                    });

                } catch (Exception e) {
                    logresult.setText("Error while connecting to server");
                }
            }
        });
    }
}
