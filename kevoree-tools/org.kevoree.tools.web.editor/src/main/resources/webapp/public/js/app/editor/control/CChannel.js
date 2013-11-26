define(
    [
        'abstraction/KChannel',
        'abstraction/KComponent',
        'abstraction/KInputPort',
        'abstraction/KOutputPort',
        'presentation/UIChannel',
        'control/AController',
        'control/CEntity',
        'util/Pooffs'
    ],

    function(KChannel, KComponent, KInputPort, KOutputPort, UIChannel, AController, CEntity, Pooffs) {
        Pooffs.extends(CChannel, AController);
        Pooffs.extends(CChannel, CEntity);
        Pooffs.extends(CChannel, KChannel);

        function CChannel(editor, type) {
            // KChannel.super(type)
            KChannel.prototype.constructor.call(this, editor, type);

            // CEntity.super(editor, type)
            CEntity.prototype.constructor.call(this, editor, type);

            // instantiate UI
            this._ui = new UIChannel(this);
        }

        CChannel.prototype.p2cMouseOver = function () {
            var wire = this.getEditor().getCurrentWire();
            if (wire) {
                // there is a wire task in progress
                var origin = wire.getOrigin();
                if (origin.getEntityType() == KInputPort.ENTITY_TYPE || origin.getEntityType() == KOutputPort.ENTITY_TYPE) {
                    if (isConnectable(origin, this)) {
                        // connection can be made
                        this._ui.c2pDropPossible();
                    } else {
                        // connection cannot be made
                        this._ui.c2pDropImpossible();
                    }
                } else {
                    // connection cannot be made
                    this._ui.c2pDropImpossible();
                }
            } else {
                // user is just hovering the shape
                this._ui.c2pPointerOverShape();
            }
        }

        CChannel.prototype.p2cMouseUp = function () {
            var wire = this.getEditor().getCurrentWire();
            if (wire) {
                // there is a wire task in progress
                var origin = wire.getOrigin();
                if (origin.getEntityType() == KInputPort.ENTITY_TYPE || origin.getEntityType() == KOutputPort.ENTITY_TYPE) {
                    if (isConnectable(origin, this)) {
                        // we are good to go
                        wire.setTarget(this);
                        this.addWire(wire);
                        this.getEditor().endWireCreationTask();
                        this._ui.c2pWireCreated(wire.getUI());
                    } else {
                        // connection cannot be made
                        this._ui.c2pDropImpossible();
                        // connection cannot be made
                        this.getEditor().abortWireCreationTask();
                    }
                } else {
                    // connection cannot be made
                    this._ui.c2pDropImpossible();
                    // connection cannot be made
                    this.getEditor().abortWireCreationTask();
                }
            } else {
                // user as just released the mouse over the channel
                this._ui.c2pPointerOverShape();
            }
        }

        CChannel.prototype.p2cDragMove = function () {
            var wires = this.getWires();
            if (wires.length > 0) this.getEditor().getUI().getWiresLayer().draw();
        }

        // Override remove() because we extend KChannel after CEntity which
        // override the redefined method in CEntity (well, this might not be clear,
        // but trust me, this is mandatory)
        CChannel.prototype.remove = function () {
            CEntity.prototype.remove.call(this);
        }

        function isConnectable(origin, target) {
            var originWires = origin.getWires();

            for (var i=0; i < originWires.length; i++) {
                var wire = originWires[i],
                    wireOrigin = wire.getOrigin(),
                    wireTarget = wire.getTarget();
                if (wireOrigin && wireTarget && wireOrigin == origin && wireTarget == target) {
                    return false;
                }
            }
            return true;
        }

        return CChannel;
    }
);