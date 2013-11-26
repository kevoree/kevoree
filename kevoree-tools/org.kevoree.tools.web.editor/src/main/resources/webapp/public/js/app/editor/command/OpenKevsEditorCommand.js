define(
    [
        'jquery'
    ],

    function ($) {

        function OpenKevsEditorCommand() {}

        OpenKevsEditorCommand.prototype.execute = function (editor) {
            $('#kevs-editor').modal({show: true});
        }

        return OpenKevsEditorCommand;
    }
);