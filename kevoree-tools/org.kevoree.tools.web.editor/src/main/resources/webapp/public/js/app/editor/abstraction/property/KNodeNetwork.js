define(
    [],

    function () {

        var id = 0;

        function KNodeNetwork(initBy, target) {
            this._target = target;
            this._initBy = initBy;
            this._id = id++;
        }

        KNodeNetwork.prototype.setTarget = function (node) {
            this._target = node;
        }

        KNodeNetwork.prototype.getTarget = function () {
            return this._target;
        }

        KNodeNetwork.prototype.setInitBy = function (node) {
            this._initBy = node;
        }

        KNodeNetwork.prototype.getInitBy = function () {
            return this._initBy;
        }

        KNodeNetwork.prototype.remove = function () {
            this._target.getEditor().removeFromModel(this);
        }

        KNodeNetwork.prototype.accept = function (visitor) {
            visitor.visitNodeNetwork(this);
        }

        return KNodeNetwork;
    }
);