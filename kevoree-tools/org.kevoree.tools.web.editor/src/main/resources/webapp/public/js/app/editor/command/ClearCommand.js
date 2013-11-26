define(
    function () {
        function ClearCommand() {}

        ClearCommand.prototype.execute = function (editor) {
            editor.clear();
        }

        return ClearCommand;
    }
);