define(
    function () {
        function ZoomInCommand() {}

        ZoomInCommand.prototype.execute = function (editor) {
            editor.p2cZoomIn();
        }

        return ZoomInCommand;
    }
);