define(
    [
        'util/AlertPopupHelper'
    ],

    function (AlertPopupHelper) {

        function CheckModelCommand() {}

        CheckModelCommand.prototype.execute = function () {
            // TODO
            AlertPopupHelper.setText("CheckModel: not implemented yet");
            AlertPopupHelper.setType(AlertPopupHelper.WARN);
            AlertPopupHelper.show(5000);
        }

        return CheckModelCommand;
    }
);