define(
    [
        'abstraction/property/KNetworkProperty',
        'presentation/property/UINetworkProperty',
        'control/AController',
        'util/Pooffs'
    ],

    function (KNetworkProperty, UINetworkProperty, AController, Pooffs) {

        Pooffs.extends(CNetworkProperty, AController);
        Pooffs.extends(CNetworkProperty, KNetworkProperty);

        function CNetworkProperty(link) {
            KNetworkProperty.prototype.constructor.call(this, link);

            this._ui = new UINetworkProperty(this);

            this._isKeyReady    = true;
            this._isValueReady  = true;
        }

        // Override KNetworkProperty.setKey
        CNetworkProperty.prototype.setKey = function (key) {
            KNetworkProperty.prototype.setKey.call(this, key);
            this._isKeyReady = true;
            this._ui.c2pKeyValueSaved();
        }

        // Override KNetworkProperty.setValue
        CNetworkProperty.prototype.setValue = function (val) {
            KNetworkProperty.prototype.setValue.call(this, val);
            this._isValueReady = true;
            this._ui.c2pValueValueSaved();
        }

        CNetworkProperty.prototype.p2cChangeKey = function (key) {
            var link = this.getLink();

            if (link.hasNetworkProperty(this._id)) {
                // we already have a network property with this ID in the link
                if (link.containsKey(key, this._id)) {
                    // someone else already has this key (other than me)
                    // so this is not possible, display error message
                    this._ui.c2pDisplayError(key);
                } else {
                    // we are good to go, this key is available
                    this.setKey(key);
                }
            } else {
                if (link.containsKey(key)) {
                    // someone else already has this key
                    this._ui.c2pDisplayError(key);
                } else {
                    // we are good to go, this key is available
                    this.setKey(key);
                }
            }
        }

        CNetworkProperty.prototype.p2cChangeValue = function (value) {
            this.setValue(value);
        }

        CNetworkProperty.prototype.p2cStartChangeKey = function () {
            this._isKeyReady = false;
        }

        CNetworkProperty.prototype.p2cStartChangeValue = function () {
            this._isValueReady = false;
        }

        return CNetworkProperty;
    }
);