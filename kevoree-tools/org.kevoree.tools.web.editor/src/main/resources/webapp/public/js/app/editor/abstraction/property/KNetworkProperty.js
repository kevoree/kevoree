define(
    function () {

        var id = 0;

        function KNetworkProperty(link) {
            this._link = link;
            this._key = null;
            this._value = null;
            this._lastCheck = null;
            this._id = id++;
        }

        KNetworkProperty.prototype.getLink = function () {
            return this._link;
        }

        KNetworkProperty.prototype.setKey = function (key) {
            this._key = key;
            this._link.getNodeProperties().getNode().getEditor().updateModel(this);
        }

        KNetworkProperty.prototype.setValue = function (value) {
            this._value = value;
            this._link.getNodeProperties().getNode().getEditor().updateModel(this);
        }

        KNetworkProperty.prototype.getKey = function () {
            return this._key;
        }

        KNetworkProperty.prototype.getValue = function () {
            return this._value;
        }

        KNetworkProperty.prototype.setLastCheck = function (timestamp) {
            this._lastCheck = timestamp;
        }

        KNetworkProperty.prototype.getLastCheck = function () {
            return this._lastCheck;
        }

        KNetworkProperty.prototype.remove = function () {
            this._link.getNodeProperties().getNode().getEditor().removeFromModel(this);
        }

        KNetworkProperty.prototype.accept = function (visitor) {
            visitor.visitNetworkProperty(this);
        }

        return KNetworkProperty;
    }
);