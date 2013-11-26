define(
    [
        'jquery',
        'util/Config'
    ],
    function ($, Config) {
        var NAMESPACE = '.settings-popup';

        function SettingsCommand () {}

        SettingsCommand.prototype.execute = function (editor) {
            $('#save-settings').off(NAMESPACE);

            // check browser compatibily with HTML5 Local Storage
             var storage = window.localStorage;
            if (storage) {
                // Local storage available
                $('#save-settings').on('click'+NAMESPACE, function () {
                    var askBeforeLeaving = $('#ask-before-leaving').prop('checked'),
                        componentTooltip = $('#component-tooltip').prop('checked'),
                        alertPopups      = $('#display-alert-popups').prop('checked'),
                        confirmOnLoad    = $('#confirm-on-load-setting').prop('checked');

                    // ASK BEFORE LEAVING Saving procress
                    storage.setItem(Config.LS_ASK_BEFORE_LEAVING, askBeforeLeaving);

                    // COMPONENT TOOLTIP Saving procress
                    storage.setItem(Config.LS_COMPONENT_TOOLTIP, componentTooltip);
                    editor.getUI().enableTooltips(componentTooltip);

                    // DISPLAY ALERT POPUPS
                    storage.setItem(Config.LS_DISPLAY_ALERT_POPUPS, alertPopups);
                    editor.getUI().enableAlertPopups(alertPopups);

                    // CONFIRM ON LOAD saving process
                    storage.setItem(Config.LS_CONFIRM_ON_LOAD, confirmOnLoad);
                });

            } else {
                // local storage is not available
                $('#save-settings').addClass('disabled');
                $('#local-storage-unavailable').show();
            }

            $('#settings-popup').modal({ show: true });
        }

        return SettingsCommand;
    }
);