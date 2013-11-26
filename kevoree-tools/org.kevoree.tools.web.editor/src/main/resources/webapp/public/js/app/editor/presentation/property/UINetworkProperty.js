define(
    [
        'jquery',
        'templates/network-property',
        'util/Delayer'
    ],

    function ($, networkPropTemplate, Delayer) {

        var NAMESPACE   = '.network-property',
            KEY_VAL     = 'network-property-key-',
            VALUE_VAL   = 'network-property-val-',
            ERROR_TAG   = 'network-property-err-',
            SELECTORS   = [KEY_VAL, VALUE_VAL, ERROR_TAG];

        function UINetworkProperty(ctrl) {
            this._ctrl = ctrl;
            this._jqy = [];
        }

        UINetworkProperty.prototype.getHTML = function () {
            var jadeParams = {
                link: {
                    id: this._ctrl.getLink()._id
                },
                id: this._ctrl._id,
                key: this._ctrl.getKey(),
                value: this._ctrl.getValue()
            };

            return networkPropTemplate(jadeParams);
        }

        UINetworkProperty.prototype.onHTMLAppended = function () {
            var link        = this._ctrl.getLink(),
                id          = this._ctrl._id,
                that        = this;

            for (var i=0; i < SELECTORS.length; i++) {
                this._jqy[SELECTORS[i]] = $('#'+ SELECTORS[i] + link._id + '-' + id);
            }

            var keyDelayer = new Delayer(),
                valueDelayer = new Delayer();

            this._jqy[KEY_VAL].off(NAMESPACE);
            this._jqy[KEY_VAL].on('keyup'+NAMESPACE, function () {
                var keyValue = $(this).val();
                // clear error fields
                $(this).closest('.control-group').removeClass('error');
                that._jqy[ERROR_TAG].hide();

                // we use Delay to prevent model from being updated on each keyup event
                keyDelayer.delay(function delayedKeyupValidation() {
                    that._ctrl.p2cChangeKey(keyValue);
                }, 350);
            });

            this._jqy[VALUE_VAL].off(NAMESPACE);
            this._jqy[VALUE_VAL].on('keyup'+NAMESPACE, function () {
                var value = $(this).val();
                // we use Delay to prevent model from being updated on each keyup event
                valueDelayer.delay(function delayedKeyupValidation() {
                    that._ctrl.p2cChangeValue(value);
                }, 350);
            });
        }

        UINetworkProperty.prototype.c2pDisplayError = function (value) {
            this._jqy[KEY_VAL].closest('.control-group').addClass('error');
            this._jqy[ERROR_TAG].find('.text-error').html('"'+value+'" is already used');
            this._jqy[ERROR_TAG].show('fast');
        }

        UINetworkProperty.prototype.c2pKeyValueSaved = function () {
//            console.log("network property updated (key)");
        }

        UINetworkProperty.prototype.c2pValueValueSaved = function (value) {
//            console.log("network property updated (value)");
        }

        UINetworkProperty.prototype.c2pValueSaved = function (isSaved) {
            if (isSaved) $('#prop-popup-save').removeClass('disabled');
            else $('#prop-popup-save').addClass('disabled');
        }

        return UINetworkProperty;
    }
);