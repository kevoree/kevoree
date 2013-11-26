define(
  [
    'kevoree',
    'abstraction/KGroup',
    'abstraction/KInputPort',
    'abstraction/KOutputPort'
  ],

  function (Kevoree, KGroup, KInputPort, KOutputPort) {

    /*
     * Visit the editor entities list in order to add/update instances
     * in the model
     * @constructor
     */
    function UpdateModelVisitor() {
      this._factory = new Kevoree.org.kevoree.impl.DefaultKevoreeFactory();
      this._listener = function () {};
    }

    UpdateModelVisitor.prototype.setModel = function (model) {
      this._model = model;
    }

    UpdateModelVisitor.prototype.setListener = function (callback) {
      if (callback && typeof(callback) == 'function') {
        this._listener = callback;
      } else {
        throw "UpdateModelVisitor setListener's callback is not a function.";
      }
    }

    UpdateModelVisitor.prototype.visitChannel = function (chan) {
      chan._instance = chan._instance || this._factory.createChannel();

      chan._instance.setName(chan._name);
      chan._instance.setTypeDefinition(this._model.findTypeDefinitionsByID(chan._type));
      saveMetaData(chan);
      chan.getDictionary().accept(this);

      this._model.addHubs(chan._instance);

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitNode = function (node) {
      node._instance = node._instance || this._factory.createContainerNode();
      node._props._instance = node._instance; // we need to give node's instance to its node property
      // in order for node's properties to get displayed properly

      node._instance.setName(node._name);
      node._instance.setTypeDefinition(this._model.findTypeDefinitionsByID(node._type));
      saveMetaData(node);
      node.getDictionary().accept(this);

      this._model.addNodes(node._instance);

      if (node._parent) {
        var parent = this._model.findNodesByID(node._parent.getName());
        parent.addHosts(node._instance);
      }

      if (node._children.length > 0) {
        for (var i=0; i< node._children.length; i++) {
          node._children[i].accept(this);
        }
      }

      if (node._wires.length > 0) {
        for (var i=0; i < node._wires.length; i++) {
          node._wires[i].getOrigin().accept(this);
        }
      }

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitComponent = function (comp) {
      comp._instance = comp._instance || this._factory.createComponentInstance();

      comp._instance.setName(comp._name);
      comp._instance.setTypeDefinition(this._model.findTypeDefinitionsByID(comp._type));
      saveMetaData(comp);
      comp.getDictionary().accept(this);

      var node = this._model.findNodesByID(comp._parent.getName());
      node.addComponents(comp._instance);

      for (var i=0; i < comp._inputs.length; i++) {
        comp._inputs[i].accept(this);
      }

      for (var i=0; i < comp._outputs.length; i++) {
        comp._outputs[i].accept(this);
      }

      for (var i=0; i < comp._wires.length; i++) {
        comp._wires[i].accept(this);
      }

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitGroup = function (grp) {
      grp._instance = grp._instance || this._factory.createGroup();

      grp._instance.setName(grp._name);
      grp._instance.setTypeDefinition(this._model.findTypeDefinitionsByID(grp._type));
      saveMetaData(grp);
      grp.getDictionary().accept(this);

      if (grp._wires.length > 0) {
        for (var i=0; i < grp._wires.length; i++) {
          grp._wires[i].accept(this);
        }
      }
      this._model.addGroups(grp._instance);

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitWire = function (wire) {
      switch (wire.getOrigin().getEntityType()) {
        case KGroup.ENTITY_TYPE:
          var node = this._model.findNodesByID(wire.getTarget().getName()),
            grp = this._model.findGroupsByID(wire.getOrigin().getName());

          if (node && grp) grp.addSubNodes(node);
          break;

        case KInputPort.ENTITY_TYPE:
        case KOutputPort.ENTITY_TYPE:
          var hub = this._model.findHubsByID(wire.getTarget().getName());

          var update = (wire._instance) ? true : false;
          wire._instance = wire._instance || this._factory.createMBinding();
          if (!wire.getOrigin()._instance) wire.getOrigin().accept(this);
          wire._instance.setPort(wire.getOrigin()._instance);
          wire._instance.setHub(hub);

          if (!update) this._model.addMBindings(wire._instance);
          break;
      }

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitOutputPort = function (port) {
      var node = this._model.findNodesByID(port._component.getParent().getName()),
        comp = node.findComponentsByID(port._component.getName()),
        portRef = comp.getTypeDefinition().findRequiredByID(port.getName());

      var update = (port._instance) ? true : false;
      port._instance = port._instance || this._factory.createPort();

      if (!update) comp.addRequired(port._instance);
      port._instance.setPortTypeRef(portRef);

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitInputPort = function (port) {
      var node = this._model.findNodesByID(port._component.getParent().getName()),
        comp = node.findComponentsByID(port._component.getName()),
        portRef = comp.getTypeDefinition().findProvidedByID(port.getName());

      var update = (port._instance) ? true : false;
      port._instance = port._instance || this._factory.createPort();
      if (!update) comp.addProvided(port._instance);
      port._instance.setPortTypeRef(portRef);

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitNodeProperties = function (nodeProps) {
      var nets = nodeProps.getNodeNetworks();

      for (var i=0; i < nets.length; i++) {
        nets[i].accept(this);
      }
    }

    UpdateModelVisitor.prototype.visitNodeNetwork = function (net) {
      var target = this._model.findNodesByID(net._target.getName()),
        initBy = this._model.findNodesByID(net._initBy.getName());

      if (net._instance) {
        this._model.removeNodeNetworks(net._instance);
      }
      net._instance = this._factory.createNodeNetwork();

      net._instance.setTarget(target);
      net._instance.setInitBy(initBy);

      var links = net.getTarget().getNodeProperties().getLinks();
      for (var i in links) {
        var link = this._factory.createNodeLink();
        link.setNetworkType(links[i].getNetworkType());
        link.setEstimatedRate(links[i].getEstimatedRate());

        var props = links[i].getNetworkProperties();
        for (var j in props) {
          if (props[j].getKey() != null && props[j].getValue() != null) {
            var prop = this._factory.createNetworkProperty();

            prop.setName(props[j].getKey());
            prop.setValue(props[j].getValue());

            link.addNetworkProperties(prop);
          }
        }
        net._instance.addLink(link);
      }

      this._model.addNodeNetworks(net._instance);

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitNodeLink = function (link) {
      link.getNodeProperties().accept(this);
    }

    UpdateModelVisitor.prototype.visitNetworkProperty = function (prop) {
      prop.getLink().getNodeProperties().accept(this);
    }

    UpdateModelVisitor.prototype.visitDictionary = function (dict) {
      dict._instance = dict._instance || this._factory.createDictionary();
      dict._instance.removeAllValues();

      for (var i=0; i < dict.getValues().length; i++) {
        dict.getValues()[i].accept(this);
      }

      dict.getEntity()._instance.setDictionary(dict._instance);

      this._listener.call(this);
    }

    UpdateModelVisitor.prototype.visitValue = function (val) {
      if (val.getValue() != null) {
        var tDef = val.getAttribute().getDictionary().getEntity().getType(),
          dicType = this._model.findTypeDefinitionsByID(tDef).getDictionaryType(),
          value = this._factory.createDictionaryValue();

        value.setAttribute(dicType.findAttributesByID(val.getAttribute().getName()));
        value.setValue(val.getValue());
        var node = null;
        if (val.getAttribute().getFragmentDependant() == true) {
          node = this._model.findNodesByID(val.getTargetNode().getName());
        }
        value.setTargetNode(node);

        val.getAttribute().getDictionary()._instance.addValues(value);
        val._instance = value;

        this._listener.call(this);
      }
    }

    // private method
    function saveMetaData(entity) {
      if (typeof(entity.getUI) === 'function' && entity.getUI()) {
        if (entity.getUI().getShape()) {
          var pos = entity.getUI().getShape().getAbsolutePosition();
          entity._instance.setMetaData('x='+parseInt(pos.x)+',y='+parseInt(pos.y));
        }
      }
    }

    return UpdateModelVisitor;
  }
);