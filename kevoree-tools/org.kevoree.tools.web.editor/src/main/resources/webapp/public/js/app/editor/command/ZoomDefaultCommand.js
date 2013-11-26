define(
    function () {
        function ZoomDefaultCommand() {}

        ZoomDefaultCommand.prototype.execute = function (editor) {
            editor.p2cZoomDefault();
        }

        return ZoomDefaultCommand;
    }
);