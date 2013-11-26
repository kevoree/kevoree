define(
    [
        'abstraction/KEntity',
        'util/Pooffs',
        'kotlin/kotlin'
    ],

    function(KEntity, Pooffs, Kotlin) {
        var COUNT = 0;

        KChannel.ENTITY_TYPE = 'ChannelType';

        Pooffs.extends(KChannel, KEntity);

        function KChannel(editor, type) {
            KEntity.prototype.constructor.call(this, editor, type);

            this._name = 'chan' + (COUNT++);
        }

        KChannel.prototype.getEntityType = function () {
            return KChannel.ENTITY_TYPE;
        }

        KChannel.prototype.accept = function (visitor) {
            visitor.visitChannel(this);
        }

        // Overriding addWire from KEntity in order to add the instance to the model
        // cause if the wire has been added here, it means that it is plugged from one hand
        // to another (port -> chan)
        KChannel.prototype.addWire = function (wire) {
            if (this._wires.indexOf(wire) == -1) { // do not duplicate wire in array
                this._wires.push(wire);
                this.getEditor().addWire(wire);

                // add default values for fragment dependant attributes
                var dictionary = this._dictionary,
                    attrs = dictionary.getAttributes(),
                    factory = require('factory/CFactory').getInstance();
                for (var i=0; i < attrs.length; i++) {
                    if (attrs[i].getFragmentDependant()) {
                        var value = factory.newValue(attrs[i], wire.getOrigin().getComponent().getParent());
                        dictionary.addValue(value);
                    }
                }
            }
        }

        KChannel.prototype.getConnectedFragments = function () {
            var nodes = new Kotlin.ArrayList(),
                alreadyAddedNode = {},
                model = this.getEditor().getModel();

            var wires = this.getWires();
            for (var i=0; i < wires.length; i++) {
                var nodeName = wires[i].getOrigin().getComponent().getParent().getName();
                if (!alreadyAddedNode[nodeName]) {
                    var instance = model.findNodesByID(nodeName);
                    if (instance != null) {
                        nodes.add(instance);
                        alreadyAddedNode[nodeName] = nodeName;
                    }
                }
            }

            return nodes;
        }

        return KChannel;
    }
);