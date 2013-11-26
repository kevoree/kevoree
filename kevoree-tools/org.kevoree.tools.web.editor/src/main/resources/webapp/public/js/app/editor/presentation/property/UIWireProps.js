define(
    function () {

        function UIWireProps(ctrl) {
            this._ctrl = ctrl;
            this._name = $('#prop-popup-name');
        }

        UIWireProps.prototype.show = function () {
            var that = this;
            $('#prop-popup-delete').off('click'); // get rid of old listeners on '#delete'
            $('#prop-popup-delete').on('click', function() {
                that._ctrl.p2cRemoveEntity();
            });

            $('#prop-popup-save').off('click');
            $('#prop-popup-save').on('click', function () {
                that._ctrl.p2cSaveProperties(that._name.val())
            });

            $('#prop-popup-subtitle').html('Wire');
            this._name.val(this._ctrl.getName());
            $('#prop-popup').modal({ show: true });
        }

        return UIWireProps;
    }
);