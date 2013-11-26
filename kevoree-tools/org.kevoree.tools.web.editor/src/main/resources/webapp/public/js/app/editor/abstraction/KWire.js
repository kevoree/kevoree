define(
    function() {
        var COUNT = 0;

        KWire.ENTITY_TYPE = 'Wire';

        function KWire(origin) {
            this._origin = origin;

            this._name = 'wire'+COUNT++;
        }

        KWire.prototype.setOrigin = function(entity) {
            this._origin = entity;
        }

        KWire.prototype.setTarget = function(entity) {
            this._target = entity;
        }

        KWire.prototype.getOrigin = function() {
            return this._origin;
        }

        KWire.prototype.getTarget = function() {
            return this._target;
        }

        KWire.prototype.getName = function () {
            return this._name;
        }

        KWire.prototype.setName = function (name) {
            this.getEditor().removeFromModel(this);
            this._name = name;
            this.getEditor().addToModel(this);
        }

        KWire.prototype.getEntityType = function () {
            return KWire.ENTITY_TYPE;
        }

        KWire.prototype.getEditor = function () {
            return this._origin.getEditor();
        }

        KWire.prototype.canConnect = function (entity) {
            for (var i=0; i < this._origin.getWires().length; i++) {
                var wire = this._origin.getWires()[i];
                if (wire.getTarget() && wire.getTarget() != null && wire.getTarget() == entity) return false;
            }
            return true;
        }

        KWire.prototype.disconnect = function() {
            if (this._origin != null) {
                this._origin.disconnect(this);
            }
            if (this._target != null) {
                this._target.disconnect(this);
            }
            this._origin = null;
            this._target = null;
        }

        KWire.prototype.accept = function (visitor) {
            visitor.visitWire(this);
        }

        return KWire;
    }
);