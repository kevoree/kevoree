define(
    [
        'jquery',
        'kevoree',
        'util/AlertPopupHelper'
    ],
    function ($, Kevoree, AlertPopupHelper) {
        var NAMESPACE = '.save-popup';

        function SaveCommand () {}

        SaveCommand.prototype.execute = function (type, editor) {
            // prevent listeners from being registered several times
            $('body').off(NAMESPACE)
            $('body').on('hidden'+NAMESPACE, '#save-popup', function () {
                // resets popup fields on hide
                $('#saved-model-link').hide();
            });

            $('#save').modal('show');

            if (editor.getModel()) {
                $('#save-popup').modal({show: true});

                try {
                    var serializer = new Kevoree.org.kevoree.serializer.JSONModelSerializer();
                    var jsonModel = JSON.parse(serializer.serialize(editor.getModel()));

                    $.ajax({
                        type: 'post',
                        url: 'save/'+type,
                        data: {model: jsonModel},
                        dataType: 'json',
                        success: function (data) {
                            $('#save-popup-text').html('Your model has been successfully uploaded to the server.');
                            $('#saved-model-link').attr('href', data.href);
                            $('#saved-model-link').show('fast');
                        },
                        error: function () {
                            $('#save-popup-text').html("Something went wrong while uploading your model.. :(");
                        }
                    });
                } catch (err) {
                    $('#save-popup-text').html("Something went wrong while uploading your model.. :(");
                    console.error("SaveCommand ERROR: "+err.message, editor.getModel());
                }
            } else {
                AlertPopupHelper.setType(AlertPopupHelper.WARN);
                AlertPopupHelper.setText("There is no model to save currently.");
                AlertPopupHelper.show(2000);
            }
        }

        return SaveCommand;
    }
);