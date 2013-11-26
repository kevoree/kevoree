define(
    function () {
        function ClearInstancesCommand() {}

        ClearInstancesCommand.prototype.execute = function (editor) {
            editor.clearInstances();
        }

        return ClearInstancesCommand;
    }
);