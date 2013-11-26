define(
    [
        'abstraction/KEntity',
        'kevoree',
        'util/Pooffs'
    ],

    function(KEntity, Kevoree, Pooffs) {
        var COUNT = 0;

        KGroup.ENTITY_TYPE = 'GroupType';

        Pooffs.extends(KGroup, KEntity);

        function KGroup(editor, type) {
            KEntity.prototype.constructor.call(this, editor, type);

            this._name = "group" + (COUNT++);
        }

        KGroup.prototype.getEntityType = function () {
            return KGroup.ENTITY_TYPE;
        }

        KGroup.prototype.accept = function (visitor) {
            visitor.visitGroup(this);
        }

        KGroup.prototype.getConnectedFragments = function () {
            if (this._instance) {
                return this._instance.getSubNodes();
            } else {
                return KEntity.prototype.getConnectedFragments.call(this);
            }
        }

        return KGroup;
    }
);