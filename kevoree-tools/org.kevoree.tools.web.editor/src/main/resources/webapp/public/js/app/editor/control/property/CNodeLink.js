define(
    [
        'abstraction/property/KNodeLink',
        'presentation/property/UINodeLink',
        'control/AController',
        'util/Pooffs'
    ],

    function (KNodeLink, UINodeLink, AController, Pooffs) {

        Pooffs.extends(CNodeLink, AController);
        Pooffs.extends(CNodeLink, KNodeLink);

        function CNodeLink(net) {
            KNodeLink.prototype.constructor.call(this, net);

            this._ui = new UINodeLink(this);
        }

        // Override KNodeLink.setNetworkType
        CNodeLink.prototype.setNetworkType = function (type) {
            KNodeLink.prototype.setNetworkType.call(this, type);
            this._ui.c2pSetNetworkType(type);
        }

        // Override KNodeLink.setEstimatedRate
        CNodeLink.prototype.setEstimatedRate = function (rate) {
            KNodeLink.prototype.setEstimatedRate.call(this, rate);
        }

        // Override KNodeLink.addNetworkProperty
        CNodeLink.prototype.addNetworkProperty = function (prop) {
            KNodeLink.prototype.addNetworkProperty.call(this, prop);
            this._ui.c2pAddNetworkProperty(prop);
        }

        // Override KNodeLink.deleteNetworkProperty
        CNodeLink.prototype.deleteNetworkProperty = function (prop) {
            KNodeLink.prototype.deleteNetworkProperty.call(this, prop);
            this._ui.c2pDeleteNetworkProperty(prop);
        }

        CNodeLink.prototype.p2cChangeType = function (type) {
            this.setNetworkType(type);
        }

        CNodeLink.prototype.p2cChangeRate = function (rate) {
            this.setEstimatedRate(rate);
        }

        CNodeLink.prototype.p2cAddNetworkProperty = function () {
            var prop = require('factory/CFactory').getInstance().newNetworkProperty(this);
            this.addNetworkProperty(prop);
        }

        CNodeLink.prototype.p2cDeleteNetworkProperties = function (ids) {
            var props = this.getNetworkProperties();
            var i = 0;
            if (ids.length >= props.length) {
                i = 1;
            }
            for (i; i < ids.length; i++) {
                var prop = this.getNetworkProperty(ids[i]);
                this.deleteNetworkProperty(prop);
            }
        }

        return CNodeLink;
    }
);