define(
    [
        'jquery',
        'util/AlertPopupHelper'
    ],
    function ($, AlertPopupHelper) {
        function SaveAsKevsCommand () {}

        SaveAsKevsCommand.prototype.execute = function () {
            AlertPopupHelper.setText("SaveAsKevs: not implemented yet");
            AlertPopupHelper.setType(AlertPopupHelper.WARN);
            AlertPopupHelper.show(5000);
        }

        return SaveAsKevsCommand;
    }
);