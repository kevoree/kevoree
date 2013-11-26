define(
    [
        'jquery',
        'util/ModelHelper',
        'util/AlertPopupHelper',
        'kevoree'
    ],
    function ($, ModelHelper, AlertPopupHelper, Kevoree) {
        var NAMESPACE = '.merge-file-command';

        function MergeCommand () {}

        MergeCommand.prototype.execute = function (editor) {
            // opens file selector
            $('#file').trigger('click');

            // called when a file is selected
            $('#file').off(NAMESPACE);
            $('#file').on('change'+NAMESPACE, function () {
                var file = $('#file').get(0).files[0]; // yeah, we do not want multiple file selection
                if ($('#file').get(0).files.length > 1) {
                    console.warn("You have selected multiple files ("
                        +$('#file').get(0).files[0].length
                        +") so I took the first one in the list ("
                        +$('#file').get(0).files[0].name
                        +")");
                }
                var fReader = new FileReader();
                fReader.onload = function (event) {
                    // retrieve data from selected file
                    var jsonModel = JSON.parse(event.target.result),
                        strModel = JSON.stringify(jsonModel);
                    try {
                        var loader = new Kevoree.org.kevoree.loader.JSONModelLoader();
                        var model = loader.loadModelFromString(strModel).get(0);
                        var currentModel = editor.getModel();

                        if (currentModel != null) {
                            // merge needed
                            var compare = new Kevoree.org.kevoree.compare.DefaultModelCompare(),
                                diffSeq = compare.merge(model, currentModel);
                            diffSeq.applyOn(model);
                        }

                        editor.setModel(model);

                        AlertPopupHelper.setText("Model \""+file.name+"\" merged successfully");
                        AlertPopupHelper.setType(AlertPopupHelper.SUCCESS);
                        AlertPopupHelper.show(5000);

                    } catch (err) {
                        AlertPopupHelper.setText(err.message);
                        AlertPopupHelper.setType(AlertPopupHelper.ERROR);
                        AlertPopupHelper.show(5000);
                    }
                }
                fReader.readAsText(file);

                // reset input field
                $(this).val('');

                $('#file').off(NAMESPACE);
            });
        }

        return MergeCommand;
    }
);