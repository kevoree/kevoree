define(
    [
        'abstraction/KPort',
        'control/AController',
        'presentation/UIPort',
        'util/Pooffs',
        'kotlin/kotlin',
        'kevoree'
    ],

    function (KPort, AController, UIPort, Pooffs, Kotlin, Kevoree) {

        Pooffs.extends(CPort, KPort);
        Pooffs.extends(CPort, AController);

        function CPort(name) {
            KPort.prototype.constructor.call(this, name);
        }

        CPort.prototype.p2cMouseDown = function () {
            var wire = this.createWire();

            // tell editor that we have started a new wire task
            this.getComponent().getEditor().startWireCreationTask(wire);

            // give the ui the newly created wire's UI
            this._ui.c2pWireCreationStarted(wire.getUI());
        }

        CPort.prototype.p2cMouseUp = function () {
            var wire = this.getComponent().getEditor().getCurrentWire();
            if (wire) {
                if (this.isConnectable(wire)) {
                    // wire is connectable with this entity
                    // show user a list of available channels
                    var typeDefs = this.getComponent().getEditor().getModel().getTypeDefinitions(),
                        channels = [];
                    for (var i=0; i < typeDefs.size(); i++) {
                        if (Kotlin.isType(typeDefs.get(i), Kevoree.org.kevoree.impl.ChannelTypeImpl)) {
                            channels.push(typeDefs.get(i).getName());
                        }
                    }

                    // do some checking over available channel type definitions
                    if (channels.length == 0) {
                        // no channel defined in the current model
                        this._ui.c2pWireCreationImpossibleNoChannel();

                    } else if (channels.length == 1) {
                        bindPortToPort(this.getComponent().getEditor(), channels[0], this);

                    } else {
                        // there is multiple channels available
                        // ask user to choose one of them
                        this._ui.c2pWireCreationPossible(wire.getOrigin(), channels);
                    }

                } else {
                    // it is impossible to connect this wire to this target
                    // TODO
                    this.getComponent().getEditor().endWireCreationTask();
                }

            } else {
                // the user as just released the mouse over this port
                // without any wire creation task pending
                // TODO
            }
        }

        CPort.prototype.p2cChannelSelectedForWireCreation = function (originPort, channelName) {
            originPort.p2cMouseDown();
            bindPortToPort(this.getComponent().getEditor(), channelName, this);
        }

        CPort.prototype.isConnectable = function (wire) {
            return false;
        }

        function bindPortToPort(editor, channelName, targetPort) {
            // there is only one channel available, use it
            var chanEntity = require('factory/CFactory').getInstance().newChannel(editor, channelName);
            // add channel entity to editor instances
            editor.addEntity(chanEntity);
            // default position for new channel
            chanEntity.getUI().getShape().setPosition(100, 100);
            // fake 'mouseup' event on this new channel in order to trigger the wiring ending logic
            // between the current wire.origin and the channel
            chanEntity.p2cMouseUp();
            // fake 'mousedown' event on this port in order to trigger the wiring creation logic
            // between the channel and this port
            targetPort.p2cMouseDown();
            chanEntity.p2cMouseUp();
        }

        return CPort;
    }
);