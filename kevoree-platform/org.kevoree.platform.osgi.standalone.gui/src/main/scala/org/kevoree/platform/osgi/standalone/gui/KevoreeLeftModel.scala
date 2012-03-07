package org.kevoree.platform.osgi.standalone.gui

import com.explodingpixels.macwidgets._
import javax.swing.{SwingUtilities, JPanel}
import org.kevoree.{ContainerRoot, ContainerNode}
import org.kevoree.framework.aspects.KevoreeAspects._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/03/12
 * Time: 20:38
 */

class KevoreeLeftModel extends JPanel {

  val model = new SourceListModel()
  val sourceList = new SourceList(model)

  def getSourceList = sourceList


  // sourceList.setColorScheme(new SourceListDarkColorScheme())


  def reload(kmodel: ContainerNode) {
    if (model.getCategories.size() > 0) {
      model.removeCategoryAt(0)
    }

    val category = new SourceListCategory(kmodel.getName)
    val componentItem = new SourceListItem("Components")
    val channelItem = new SourceListItem("Channels")
    val groupItem = new SourceListItem("Groups")
    model.addCategory(category)
    model.addItemToCategory(componentItem, category)
    model.addItemToCategory(channelItem, category)
    model.addItemToCategory(groupItem, category)

    kmodel.getComponents.foreach {
      c =>
        val itc = new SourceListItem(c.getName + ":" + c.getTypeDefinition.getName)
        model.addItemToItem(itc, componentItem)
    }

    kmodel.eContainer.asInstanceOf[ContainerRoot].getHubs.filter(h => h.getRelatedNodes.exists(c => c.getName == kmodel.getName)).foreach {
      c =>
        val itc = new SourceListItem(c.getName + ":" + c.getTypeDefinition.getName)
        model.addItemToItem(itc, channelItem)
    }
    kmodel.eContainer.asInstanceOf[ContainerRoot].getGroups.filter(g => g.getSubNodes.exists(c => c.getName == kmodel.getName)).foreach{g =>
      val itc = new SourceListItem(g.getName + ":" + g.getTypeDefinition.getName)
      model.addItemToItem(itc, groupItem)
    }



    sourceList.useIAppStyleScrollBars()
    //sourceList.setExpanded(category, false)

    SwingUtilities.invokeLater(new Runnable() {
      def run() {
        doLayout()
        repaint()
        revalidate()
      }
    })
  }


}
