define(
    function () {
        function ZoomOutCommand() {}

        ZoomOutCommand.prototype.execute = function (editor) {
            editor.p2cZoomOut();
        }

        return ZoomOutCommand;
    }
);