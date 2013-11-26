define(
    [
        'abstraction/KPort',
        'util/Pooffs',
        'require'
    ],

    function (KPort, Pooffs, require) {

        KOutputPort.ENTITY_TYPE = 'OutputPortType';

        Pooffs.extends(KOutputPort, KPort);

        function KOutputPort (name) {
            KPort.prototype.constructor.call(this, name);
        }

        KOutputPort.prototype.createWire = function () {
            var wire = require('factory/CFactory').getInstance().newWire(this);
            this._component.addWire(wire);
            return wire;
        }

        KOutputPort.prototype.accept = function (visitor) {
            visitor.visitOutputPort(this);
        }

        KOutputPort.prototype.getEntityType = function () {
            return KOutputPort.ENTITY_TYPE;
        }

        return KOutputPort;
    }
);