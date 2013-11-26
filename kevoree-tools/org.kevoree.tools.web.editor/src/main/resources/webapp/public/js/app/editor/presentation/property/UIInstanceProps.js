define(
    function () {

        function UIInstanceProps(ctrl) {
            this._ctrl = ctrl;

            $('#prop-popup').on('hidden', function () {
                $('#prop-popup-content').empty(); // clear props content when hide
            });
        }

        UIInstanceProps.prototype.onDeleteInstance = function () {
            this._ctrl.p2cRemoveEntity();
        }

        UIInstanceProps.prototype.onSaveProperties = function () {
            // tell controller that user wants to save properties
            this._ctrl.p2cSaveProperties(this.getPropertiesValues());
        }

        UIInstanceProps.prototype.getPropertiesValues = function () {
            return { name: $('#instance-attr-name').val() };
        }

        UIInstanceProps.prototype.show = function () {
            var that = this;
            $('#prop-popup-delete').off('click'); // get rid of old listeners on '#delete'
            $('#prop-popup-delete').on('click', function() {
                that.onDeleteInstance();
            });

            $('#prop-popup-save').off('click');
            $('#prop-popup-save').on('click', function () {
                if (!$(this).hasClass('disabled')) that.onSaveProperties();
            });

            $('#prop-popup-subtitle').html(this._ctrl.getEntityType());
            $('#instance-prop-name').val(this._ctrl.getName());
            $('#prop-popup-content').html(this.getHTML());
            this.onHTMLAppended();
            $('#prop-popup').modal({ show: true });
        }

        UIInstanceProps.prototype.getHTML = function () {
            return this._ctrl.getDictionary().getUI().getHTML();
        }

        UIInstanceProps.prototype.onHTMLAppended = function () {
            this._ctrl.getDictionary().getUI().onHTMLAppended();
        }

        return UIInstanceProps;
    }
);