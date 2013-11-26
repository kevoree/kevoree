define(
    [
        'abstraction/KInputPort',
        'abstraction/KOutputPort',
        'control/CPort',
        'presentation/UIInputPort',
        'util/Pooffs'
    ],

    function (KInputPort, KOutputPort, CPort, UIInputPort, Pooffs) {

        Pooffs.extends(CInputPort, CPort);
        Pooffs.extends(CInputPort, KInputPort);

        function CInputPort (name) {
            CPort.prototype.constructor.call(this, name);

            // instantiate ui
            this._ui = new UIInputPort(this);
        }

        // Override CPort.isConnectable(KWire)
        CInputPort.prototype.isConnectable = function (wire) {
            return (wire.getOrigin() != this && wire.getOrigin().getEntityType() == KOutputPort.ENTITY_TYPE);
        }

        return CInputPort;
    }
);