define(

    function () {

        function ZoomToCommand() {}

        ZoomToCommand.prototype.execute = function (editor, scale) {
            editor.p2cZoomTo(scale);
        }

        return ZoomToCommand;
    }
);