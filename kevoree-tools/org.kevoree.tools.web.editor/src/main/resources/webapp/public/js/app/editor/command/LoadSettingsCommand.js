define(
    [
        'jquery',
        'util/Config'
    ],

    function ($, Config) {

        function LoadSettingsCommand() {}

        LoadSettingsCommand.prototype.execute = function (editor) {
            var storage = window.localStorage;
            if (storage) { // check if browser is compatible with HTML5 Local Storage
                var askBeforeLeaving = storage.getItem(Config.LS_ASK_BEFORE_LEAVING) || "true",
                    componentTooltip = storage.getItem(Config.LS_COMPONENT_TOOLTIP) || "true",
                    alertPopups      = storage.getItem(Config.LS_DISPLAY_ALERT_POPUPS) || "true",
                    confirmOnLoad    = storage.getItem(Config.LS_CONFIRM_ON_LOAD) || "true";

                processSetting('#ask-before-leaving', askBeforeLeaving);

                processSetting('#component-tooltip', componentTooltip, function (checked) {
                    editor.getUI().enableTooltips(checked);
                });

                processSetting('#display-alert-popups', alertPopups, function (checked) {
                    editor.getUI().enableAlertPopups(checked);
                });

                processSetting('#confirm-on-load-setting', confirmOnLoad);

            } else {
                console.warn('Your browser is not compatible with HTML5 Local Storage. ' +
                    'KevWebEditor won\'t be able to save/load your settings!');
            }
        }

        function processSetting(domID, checked, callback) {
            var isChecked = (checked == 'true'),
                domItem = $(domID);
            domItem.prop('checked', isChecked);
            if (callback && typeof (callback) == 'function') callback.call(domItem, isChecked);
        }

        return LoadSettingsCommand;
    }
);