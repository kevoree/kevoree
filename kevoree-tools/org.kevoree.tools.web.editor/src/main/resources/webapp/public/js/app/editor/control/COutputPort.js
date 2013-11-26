define(
    [
        'abstraction/KOutputPort',
        'abstraction/KInputPort',
        'control/CPort',
        'presentation/UIOutputPort',
        'util/Pooffs'
    ],

    function (KOutputPort, KInputPort, CPort, UIOutputPort, Pooffs) {

        Pooffs.extends(COutputPort, CPort);
        Pooffs.extends(COutputPort, KOutputPort);

        function COutputPort (name) {
            CPort.prototype.constructor.call(this, name);

            // instantiate ui
            this._ui = new UIOutputPort(this);
        }

        // Override CPort.isConnectable(KWire)
        COutputPort.prototype.isConnectable = function (wire) {
            return (wire.getOrigin() != this && wire.getOrigin().getEntityType() == KInputPort.ENTITY_TYPE);
        }

        return COutputPort;
    }
);