define(
    [
        'abstraction/KWire',
        'presentation/UIWire',
        'control/AController',
        'util/Pooffs'
    ],

    function(KWire, UIWire, AController, Pooffs) {

        Pooffs.extends(CWire, KWire);
        Pooffs.extends(CWire, AController);

        function CWire(origin) {
            // KWire.super()
            KWire.prototype.constructor.call(this, origin);

            // instantiate ui
            this._ui = new UIWire(this, origin.getEditor().getUI().getWiresLayer());
        }

        // Override KWire.setOrigin(KEntity)
        CWire.prototype.setOrigin = function(entity) {
            KWire.prototype.setOrigin.call(this, entity);
            this._ui.setOrigin(entity.getUI());
        }

        // Override KWire.setTarget(KEntity)
        CWire.prototype.setTarget = function(entity) {
            KWire.prototype.setTarget.call(this, entity);
            this._ui.setTarget(entity.getUI());
        }

        // Override KWire.disconnect()
        CWire.prototype.disconnect = function () {
            KWire.prototype.disconnect.call(this);
            this._ui.remove();
        }

        CWire.prototype.p2cRemoveEntity = function () {
            this.disconnect();
        }

        CWire.prototype.p2cSaveProperties = function (name) {
            this.setName(name);
        }

        return CWire;
    }
);