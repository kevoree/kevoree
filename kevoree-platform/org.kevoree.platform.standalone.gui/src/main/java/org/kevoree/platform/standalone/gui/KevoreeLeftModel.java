package org.kevoree.platform.standalone.gui;

import com.explodingpixels.macwidgets.SourceList;
import com.explodingpixels.macwidgets.SourceListCategory;
import com.explodingpixels.macwidgets.SourceListItem;
import com.explodingpixels.macwidgets.SourceListModel;
import org.kevoree.*;
import org.kevoree.framework.kaspects.ChannelAspect;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/06/13
 * Time: 07:50
 */
public class KevoreeLeftModel extends JPanel {

    private SourceListModel model = new SourceListModel();
    private SourceList _sourceList = new SourceList(model);
    private ChannelAspect channelAspect = new ChannelAspect();

    public SourceList getSourceList() {
        return _sourceList;
    }

    public void reload(ContainerNode kmodel) {
        if (model.getCategories().size() > 0) {
            model.removeCategoryAt(0);
        }
        SourceListCategory category = new SourceListCategory(kmodel.getName());
        SourceListItem componentItem = new SourceListItem("Components");
        SourceListItem channelItem = new SourceListItem("Channels");
        SourceListItem groupItem = new SourceListItem("Groups");
        SourceListItem childItem = new SourceListItem("ChildNodes");
        model.addCategory(category);
        model.addItemToCategory(componentItem, category);
        model.addItemToCategory(channelItem, category);
        model.addItemToCategory(groupItem, category);
        model.addItemToCategory(childItem, category);
        for (ComponentInstance c : kmodel.getComponents()) {
            SourceListItem itc = new SourceListItem(c.getName() + ":" + c.getTypeDefinition().getName());
            model.addItemToItem(itc, componentItem);
        }
        for (Channel c : ((ContainerRoot) kmodel.eContainer()).getHubs()) {
            if (channelAspect.getRelatedNodes(c).contains(kmodel)) {
                SourceListItem itc = new SourceListItem(c.getName() + ":" + c.getTypeDefinition().getName());
                model.addItemToItem(itc, channelItem);
            }
        }
        for (Group g : ((ContainerRoot) kmodel.eContainer()).getGroups()) {
            if (g.getSubNodes().contains(kmodel)) {
                SourceListItem itc = new SourceListItem(g.getName() + ":" + g.getTypeDefinition().getName());
                model.addItemToItem(itc, groupItem);
            }
        }
        for (ContainerNode child : kmodel.getHosts()) {
            SourceListItem itc = new SourceListItem(child.getName() + ":" + child.getTypeDefinition().getName());
            model.addItemToItem(itc, childItem);
        }
        try {
            _sourceList.useIAppStyleScrollBars();

        } catch (Exception e) {

        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                doLayout();
                repaint();
                revalidate();
            }
        });
    }

}
