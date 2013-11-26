define(
    [
        'abstraction/KEntity',
        'util/Pooffs',
        'util/Util',
        'kevoree'
    ],

    function(KEntity, Pooffs, Util, Kevoree) {
        Pooffs.extends(CEntity, KEntity);

        function CEntity(editor, type) {}

        CEntity.prototype.p2cRemoveEntity = function () {
            this.remove();
        }

        CEntity.prototype.p2cMouseDown = function (position) {}

        CEntity.prototype.p2cMouseUp = function (position) {}

        CEntity.prototype.p2cMouseMove = function (position) {}

        CEntity.prototype.p2cDragMove = function () {
            // refresh wires layer if any
            if (this.hasWires()) {
                this._ui.c2pRefreshWires(this.getEditor().getUI());
            }
        }

        // Override KEntity.remove()
        CEntity.prototype.remove = function () {
            KEntity.prototype.remove.call(this);
            this._ui.c2pRemoveEntity();
        }

        CEntity.prototype.p2cSaveProperties = function (props) {
            this.setName(props['name']);
            this._ui.c2pPropertiesUpdated();
        }

        CEntity.prototype.p2cDragEnd = function () {
            this.getEditor().updateModel(this);
        }

        return CEntity;
    }
);