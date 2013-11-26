define(
    function () {

        AlertPopupHelper.SUCCESS    = 'alert-success';
        AlertPopupHelper.ERROR      = 'alert-error';
        AlertPopupHelper.WARN       = 'alert-warn';
        AlertPopupHelper.isEnabled  = true;

        function AlertPopupHelper() {}

        AlertPopupHelper.setText = function (text) {
            $('#alert-content').text(text);
        }

        AlertPopupHelper.setHTML = function (html) {
            $('#alert-content').html(html);
        }

        AlertPopupHelper.setType = function (type) {
            // remove old alert class type
            $('#alert').removeClass(this._type);

            if (type != AlertPopupHelper.SUCCESS && type != AlertPopupHelper.ERROR && type != AlertPopupHelper.WARN) {
                console.warn('AlertPopupHelper Error: Type \"'+type+'\" is unknown! Using \"SUCCESS\" instead.');
                this._type = AlertPopupHelper.SUCCESS;
            } else {
                this._type = type;
            }

            // add new alert class type
            $('#alert').addClass(this._type);
        }

        /**
         * Shows the alert popup for 'timeout' milliseconds.
         * If no timeout is given, then the alert will never hide unless
         * you call hide()
         * @param timeout {Number} show popup for 'timeout' ms before hiding it
         */
        AlertPopupHelper.show = function (timeout) {
            clearTimeout(this._timeoutID);

            if (AlertPopupHelper.isEnabled) {
                // show the alert popup
                $('#alert').addClass(this._type+' in');

                // if a timeout is given, register a callback in 'timeout' milliseconds
                // to close the alert popup
                if (timeout != undefined) {
                    var that = this;
                    if (this._timeoutID) clearTimeout(this._timeoutID);
                    this._timeoutID = setTimeout(function () {
                        $('#alert').removeClass('in');
                        that._timeoutID = null;
                    }, timeout);
                }
            }
        }

        /**
         * Hides the alert popup immediately if no 'timeout' is given
         * otherwise it will wait 'timeout' milliseconds before hiding it
         * @param timeout {Number} time to wait before hiding the alert popup (in ms)
         */
        AlertPopupHelper.hide = function (timeout) {
            var that = this;
            var timetowait = (timeout) ? timeout : 0;
            if (this._timeoutID) clearTimeout(this._timeoutID);
            this._timeoutID = setTimeout(function () {
                $('#alert').removeClass('in');
                that._timeoutID = null;
            }, timetowait);
        }

        AlertPopupHelper.setEnabled = function (isEnabled) {
            AlertPopupHelper.isEnabled = isEnabled;
        }

        return AlertPopupHelper;
    }
);