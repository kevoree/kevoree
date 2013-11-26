define(
    [
        'abstraction/KPort',
        'util/Pooffs',
        'require'
    ],

    function (KPort, Pooffs, require) {

        KInputPort.ENTITY_TYPE = 'InputPortType';

        Pooffs.extends(KInputPort, KPort);

        function KInputPort (name) {
            KPort.prototype.constructor.call(this, name);
        }

        KInputPort.prototype.createWire = function () {
            var wire = require('factory/CFactory').getInstance().newWire(this);
            this._component.addWire(wire);
            return wire;
        }

        KInputPort.prototype.accept = function (visitor) {
            visitor.visitInputPort(this);
        }

        KInputPort.prototype.getEntityType = function () {
            return KInputPort.ENTITY_TYPE;
        }

        return KInputPort;
    }
);