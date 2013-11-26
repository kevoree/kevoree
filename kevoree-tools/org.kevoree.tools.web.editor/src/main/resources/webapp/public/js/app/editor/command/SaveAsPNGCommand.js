define(
    [
        'jquery'
    ],
    function ($) {
        function SaveAsPNGCommand () {}

        SaveAsPNGCommand.prototype.execute = function (editor) {
            editor.getUI().getStage().toDataURL({
                callback: function(dataUrl) {
                    window.open(dataUrl, '_blank');
                }
            });
        }

        return SaveAsPNGCommand;
    }
);