package org.kevoree.tools.ui.editor.command;

import org.kevoree.*;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.ChannelPanel;
import org.kevoree.tools.ui.framework.elements.GroupPanel;
import scala.util.Random;

import java.awt.*;

public class AddGroupCommand implements Command {

    private KevoreeUIKernel kernel;
    private Random random = new Random();

    private Point point = null;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    public void setPoint(Point p) {
        this.point = p;
    }

    @Override
    public void execute(Object p) {
        Group newgroup = KevoreeFactory.eINSTANCE.createGroup();
        GroupType type = (GroupType) kernel.getUifactory().getMapping().get(p);
        newgroup.setTypeDefinition(type);

        //CREATE NEW NAME
        newgroup.setName("group" + Math.abs(random.nextInt()));
        GroupPanel newgrouppanel = kernel.getUifactory().createGroup(newgroup);
        kernel.getModelHandler().getActualModel().getGroups().add(newgroup);
        kernel.getModelPanel().addGroup(newgrouppanel);

        if ((point.x - newgrouppanel.getPreferredSize().getHeight() / 2 > 0) && (point.y - newgrouppanel.getPreferredSize().getHeight() / 2 > 0)) {
            newgrouppanel.setLocation((int) (point.x - newgrouppanel.getPreferredSize().getHeight() / 2), (int) (point.y - newgrouppanel.getPreferredSize().getWidth() / 2));
        } else {
            newgrouppanel.setLocation(point.x, point.y);
        }

    }
}