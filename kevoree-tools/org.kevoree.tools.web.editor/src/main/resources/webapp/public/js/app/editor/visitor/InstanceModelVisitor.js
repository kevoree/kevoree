define(
  [
    'util/Util',
    'abstraction/KComponent',
    'abstraction/KNode'
  ],
  function (Util, KComponent, KNode) {

    /**
     * Visit model in order to load instances in the editor
     * @constructor
     */
    function InstanceModelVisitor() {
      this._factory = require('factory/CFactory').getInstance();
    }

    InstanceModelVisitor.prototype.visitEditor = function (editor) {
      var model = editor.getModel();

      // visit node instances
      visitNodes(editor, this._factory, model.getNodes());

      // visit group instances
      visitGroups(editor, this._factory, model.getGroups());

      // visit channel instances
      visitChannels(editor, this._factory, model.getHubs());

      // visit components instances
      visitComponents(editor, this._factory, model.getNodes());

      // visit bindings instances
      visitBindings(editor, this._factory, model.getMBindings());

      // visit subNodes instances
      visitSubNodes(editor, this._factory, model.getGroups());

      // visit nodeNetworks instances
      visitNodeNetworks(editor, this._factory, model.getNodeNetworks());
    }

    // private methods
    function visitNodes(editor, factory, nodes) {
      for (var i=0; i < nodes.size(); i++) {
        var node = nodes.get(i);
        var entity = factory.newNode(editor, node.getTypeDefinition().getName());
        entity._instance = node;
        entity.getDictionary()._instance = node.getDictionary();
        entity.setName(node.getName());

        // check if this node has already been added to editor
        if (!editor.hasEntity(entity)) {
          // this is a new node for the editor
          if (!node.getHost()) {
            // this node has no parent, add it to editor
            editor.addEntity(entity);
            loadMetaData(entity, node);
            loadDictionaryValues(entity, node, factory);
          }

          if (node.getHost()) {
            // this node has a parent
            var parent = editor.getEntity(node.getHost().getName());
            parent.addChild(entity);
            loadDictionaryValues(entity, node, factory);
          }
        }
      }
    }

    function visitGroups(editor, factory, grps) {
      var entity = null;
      var grp = null;
      for (var i=0; i < grps.size(); i++) {
        grp = grps.get(i);
        entity = factory.newGroup(editor, grp.getTypeDefinition().getName());
        entity._instance = grp;
        entity.getDictionary()._instance = grp.getDictionary();
        entity.setName(grp.getName());
        editor.addEntity(entity);
        loadMetaData(entity, grp);
        loadDictionaryValues(entity, grp, factory);
      }
    }

    function visitComponents(editor, factory, nodes) {
      var entity = null;
      var node = null;
      for (var i=0; i < nodes.size(); i++) {
        node = nodes.get(i);
        var entityNode = editor.getEntity(node.getName());
        if (entityNode != null) {
          var compz = node.getComponents();
          for (var j=0; j < compz.size(); j++) {
            var comp = compz.get(j);
            entity = factory.newComponent(editor, comp.getTypeDefinition().getName());
            entity._instance = comp;
            entity.getDictionary()._instance = comp.getDictionary();
            entity.setName(comp.getName());
            entityNode.addChild(entity);
            loadDictionaryValues(entity, comp, factory);
          }
        }
      }
    }

    function visitChannels(editor, factory, chans) {
      var entity = null;
      var chan = null;
      for (var i=0; i < chans.size(); i++) {
        chan = chans.get(i);
        entity = factory.newChannel(editor, chan.getTypeDefinition().getName());
        entity._instance = chan;
        entity.getDictionary()._instance = chan.getDictionary();
        entity.setName(chan.getName());
        editor.addEntity(entity);
        loadMetaData(entity, chan);
        loadDictionaryValues(entity, chan, factory);
      }
    }

    function visitSubNodes(editor, factory, grps) {
      for (var i=0; i < grps.size(); i++) {
        var subNodes = grps.get(i).getSubNodes();
        for (var j=0; j < subNodes.$size; j++) {
          var grp = editor.getEntity(grps.get(i).getName());
          var node = editor.getEntity(subNodes.get(j).getName());
          if (grp != null && node != null) {
            var wire = factory.newWire(grp);
            wire.setTarget(node);
            grp.addWire(wire);
            node.addWire(wire);
          }
        }
      }
    }

    function visitBindings(editor, factory, bindings) {
      for (var i=0; i < bindings.size(); i++) {
        var port = bindings.get(i).getPort(),
          hub = bindings.get(i).getHub();

        if (port && hub) {
          var comp = editor.getEntity(port.eContainer().getName()),
            chan = editor.getEntity(hub.getName());
          if (comp && chan) {
            for (var j=0; j < port.eContainer().getProvided().size(); j++) {
              var provided = port.eContainer().getProvided().get(j);
              if (port.getPortTypeRef() == provided.getPortTypeRef()) {
                var portEntity = comp.getPort(port.getPortTypeRef().getName());
                if (portEntity != null) {
                  addPortToEditor(portEntity, comp, chan);
                }
              }
            }

            for (var j=0; j < port.eContainer().getRequired().size(); j++) {
              var required = port.eContainer().getRequired().get(j);
              if (port.getPortTypeRef() == required.getPortTypeRef()) {
                var portEntity = comp.getPort(port.getPortTypeRef().getName());
                if (portEntity != null) {
                  addPortToEditor(portEntity, comp, chan);
                }
              }
            }
          }

          function addPortToEditor(portEntity, component, chan) {
            portEntity.setComponent(component);
            portEntity._instance = port;
            var wire = portEntity.createWire();
            wire._instance = bindings.get(i);
            wire.setTarget(chan);
            chan.addWire(wire);
          }
        }
      }
    }

    function visitNodeNetworks(editor, factory, nets) {
      for (var i=0; i < nets.size(); i++) {
        var initByNode = editor.getEntity(nets.get(i).getInitBy().getName()),
          targetNode = editor.getEntity(nets.get(i).getTarget().getName());

        if (initByNode && targetNode) {
          // check if targetNode already has a node network for this initBy node
          var nodeNetwork = getNodeNetwork(initByNode, targetNode);
          if (!nodeNetwork) {
            nodeNetwork = factory.newNodeNetwork(initByNode, targetNode);
            nodeNetwork._instance = nets.get(i);
            targetNode.getNodeProperties().addNodeNetwork(nodeNetwork);
          }

          // create node links for targetNode if not already done
          var links = nets.get(i).getLink();
          if (links.size() > 0) targetNode.getNodeProperties().removeAllLinks();
          for (var j=0; j < links.size(); j++) {
            var link = factory.newNodeLink(targetNode.getNodeProperties());
            link.setNetworkType(links.get(j).getNetworkType());
            link.setEstimatedRate(links.get(j).getEstimatedRate());

            // create network properties for this node link
            var props = links.get(j).getNetworkProperties();
            if (props.size() > 0) link.removeAllNetworkProperties();
            for (var k=0; k < props.size(); k++) {
              var prop = factory.newNetworkProperty(link);
              prop.setKey(props.get(k).getName());
              prop.setValue(props.get(k).getValue());
              link.addNetworkProperty(prop);
            }

            // add node link to node properties
            targetNode.getNodeProperties().addLink(link);
          }
        }
      }

      function getNodeNetwork(initBy, target) {
        var nets = target.getNodeProperties().getNodeNetworks();
        for (var i in nets) {
          if (nets[i].getInitBy().getName() == initBy.getName()
            && nets[i].getTarget().getName() == target.getName()) {
            return nets[i];
          }
        }
        return null;
      }
    }

    function loadMetaData(entity, instance) {
      var metaData = instance.getMetaData(),
        x = 100,
        y = 100;

      if (metaData != null) {
        var commaSplitted = metaData.split(',');
        for (var i=0; i < commaSplitted.length; i++) {
          if (commaSplitted[i].substr(0, 'x='.length) == 'x=') {
            x = parseInt(commaSplitted[i].substr('x='.length, commaSplitted[i].length-1));
          }

          if (commaSplitted[i].substr(0, 'y='.length) == 'y=') {
            y = parseInt(commaSplitted[i].substr('y='.length, commaSplitted[i].length-1));
          }
        }
      }

      entity.getUI().getShape().setAbsolutePosition(x, y);
    }

    function loadDictionaryValues(entity, instance, factory) {
      var dictionary = instance.getDictionary();
      if (dictionary != null && dictionary != undefined) {
        var values = dictionary.getValues();
        for (var i=0; i < values.size(); i++) {
          var dicVal = values.get(i),
            attrName = dicVal.getAttribute().getName(),
            fragDep = Util.parseBoolean(dicVal.getAttribute().getFragmentDependant()),
            kValue = null,
            attr = entity.getDictionary().getAttribute(attrName);

          // bug fix: prevent component && node attributes to be fragDep=true
          // because it doesn't make sense and leads to bugs
          if (entity.getEntityType() == KComponent.ENTITY_TYPE || entity.getEntityType() == KNode.ENTITY_TYPE) {
            fragDep = false;
            attr.setFragmentDependant(false);
          }

          kValue = factory.newValue(attr);
          kValue.setValue(dicVal.getValue());
          kValue._instance = dicVal;
          if (fragDep) kValue.setTargetNode(entity.getEditor().getEntity(dicVal.getTargetNode().getName()));
          entity.getDictionary().addValue(kValue);
        }
      }
    }

    return InstanceModelVisitor;
  }
);