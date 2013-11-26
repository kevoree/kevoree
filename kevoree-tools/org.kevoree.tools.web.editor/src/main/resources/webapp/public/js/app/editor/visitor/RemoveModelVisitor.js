define(
    [
        'abstraction/KGroup',
        'abstraction/KInputPort',
        'abstraction/KOutputPort',
        'kevoree'
    ],

    function (KGroup, KInputPort, KOutputPort, Kevoree) {

        /**
         * Visit editor entities list in order to remove instances from model
         * @constructor
         */
        function RemoveModelVisitor() {
            this._factory = new Kevoree.org.kevoree.impl.DefaultKevoreeFactory();
            this._listener = function () {};
        }

        RemoveModelVisitor.prototype.setModel = function (model) {
            this._model = model;
        }

        RemoveModelVisitor.prototype.setListener = function (callback) {
            if (callback && typeof(callback) == 'function') {
                this._listener = callback;
            } else {
                throw "RemoveModelVisitor setListener's callback is not a function.";
            }
        }

        RemoveModelVisitor.prototype.visitChannel = function (chan) {
            var instance = this._model.findHubsByID(chan._name);
            if (instance != null) {
                // TODO remove wires and opposite ref in frag dep attributes

                this._model.removeHubs(instance);
                this._listener.call(this);
            }
        }

        RemoveModelVisitor.prototype.visitNode = function (node) {
            var instance = this._model.findNodesByID(node._name);
            if (instance != null) {
                var wires = node.getWires();
                for (var i in wires) {
                    var grp = this._model.findGroupsByID(wires[i].getOrigin().getName()),
                        dic = wires[i].getOrigin().getDictionary(),
                        values = dic.getValues();
                    for (var j in values) {
                        var targetNode = values[j].getTargetNode();
                        if (targetNode != null && targetNode.getName() == node.getName()) {
                            if (values[j]._instance != null) {
                                dic._instance.removeValues(values[j]._instance);
                            }
                        }
                    }

                    grp.removeSubNodes(instance);
                }

                this._model.removeNodes(instance);
                this._listener.call(this);
            }
        }

        RemoveModelVisitor.prototype.visitComponent = function (comp) {
            var node = this._model.findNodesByID(comp._parent.getName()),
                instance = node.findComponentsByID(comp._name);
            if (node != null && instance != null) {
                node.removeComponents(instance);
                this._listener.call(this);
            }
        }

        RemoveModelVisitor.prototype.visitGroup = function (grp) {
            var instance = this._model.findGroupsByID(grp._name);
            if (instance != null) {
                this._model.removeGroups(instance);
                this._listener.call(this);
            }
        }

        RemoveModelVisitor.prototype.visitOutputPort = function (port) {}

        RemoveModelVisitor.prototype.visitInputPort = function (port) {}

        RemoveModelVisitor.prototype.visitWire = function (wire) {
            if (wire._instance) {
                var port = wire.getOrigin()._instance,
                    chan = wire.getTarget()._instance;
                if (port) port.removeBindings(wire._instance);
                if (chan) chan.removeBindings(wire._instance);

                this._model.removeMBindings(wire._instance);
                this._listener.call(this);
            }
        }

        RemoveModelVisitor.prototype.visitNodeProperties = function (nodeProps) {
            var nets = nodeProps.getNodeNetworks();
            for (var i=0; i < nets.length; i++) {
                nets[i].accept(this);
            }
            this._listener.call(this);
        }

        RemoveModelVisitor.prototype.visitNodeNetwork = function (net) {
            if (net._instance != null) {
                this._model.removeNodeNetworks(net._instance);
                this._listener.call(this);
            }
        }

        RemoveModelVisitor.prototype.visitNodeLink = function (link) {
            var nets = link.getNodeProperties().getNodeNetworks();
            for (var i in nets) {
                if (nets[i]._instance) {
                    nets[i]._instance.removeLink(link._instance);
                    this._listener.call(this);
                }
            }
        }

        RemoveModelVisitor.prototype.visitNetworkProperty = function (prop) {
            var link = prop.getLink();
            if (link._instance) {
                link._instance.removeNetworkProperties(prop._instance);
                this._listener.call(this);
            }
        }

        return RemoveModelVisitor;
    }
);