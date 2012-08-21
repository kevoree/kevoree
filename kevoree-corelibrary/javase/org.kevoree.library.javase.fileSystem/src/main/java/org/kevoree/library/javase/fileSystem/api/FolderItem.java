package org.kevoree.library.javase.fileSystem.api;


import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FolderItem extends AbstractItem implements IsSerializable, Serializable {


    private static final long serialVersionUID = 2388319784164372900L;
    private List<AbstractItem> items;

    public FolderItem(){
        super();
        this.items = new ArrayList<AbstractItem>();

    }

    /*public FolderItem(String name){
            this();
            this.name = name;
            this.path = name;
        }*/

    public void add(AbstractItem itemToAdd){
        this.items.add(itemToAdd);
    }

    public List<AbstractItem> getChilds(){
        return this.items;
    }

}
