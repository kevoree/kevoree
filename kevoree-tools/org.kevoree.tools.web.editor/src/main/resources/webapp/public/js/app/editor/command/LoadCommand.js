define(
  [
    'jquery',
    'util/ModelHelper',
    'util/AlertPopupHelper',
    'util/Config',
    'util/Util',
    'kevoree'
  ],
  function ($, ModelHelper, AlertPopupHelper, Config, Util, Kevoree) {
    var NAMESPACE = '.load-file-command';

    function LoadCommand () {}

    LoadCommand.prototype.execute = function (editor) {
      if (editor.getModel() != null && confirmOnLoadSettingEnabled()) {
        // current model is not null, meaning that it could be overwritten
        // so ask user if he really wants that to happen (if confirmOnLoad is enabled)
        AlertPopupHelper.setHTML(
          "<p>Do you want to overwrite current model ?" +
            "<br/>Any unsaved work will be lost.</p>" +
            "<div class='row-fluid'>" +
            "<button id='confirm-load-model' type='button' class='btn btn-mini btn-danger'>Load model</button>" +
            "<button id='keep-current-model' type='button' class='btn btn-mini btn-primary pull-right'>Keep model</button>" +
            "</div>" +
            "<small>You can disable this confirmation popup in <a href='#' id='disable-confirm-load'>settings</a></small>"
        );
        AlertPopupHelper.setType(AlertPopupHelper.WARN);
        AlertPopupHelper.show();

        $('#confirm-load-model').off('click');
        $('#confirm-load-model').on('click', function () {
          AlertPopupHelper.hide();
          loadProcess();
        });

        $('#keep-current-model').off('click');
        $('#keep-current-model').on('click', function () {
          AlertPopupHelper.hide();
        });

        $('#disable-confirm-load').off('click');
        $('#disable-confirm-load').on('click', function () {
          $('#settings-popup').modal('show');
        });
      } else {
        loadProcess();
      }

      function loadProcess() {
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
            //try {
            // retrieve data from selected file
            var jsonModel = JSON.parse(event.target.result),
              strModel = JSON.stringify(jsonModel),
              loader = new Kevoree.org.kevoree.loader.JSONModelLoader(),
              model = loader.loadModelFromString(strModel).get(0);


            editor.setModel(model);

            AlertPopupHelper.setText("Model \""+file.name+"\" loaded successfully");
            AlertPopupHelper.setType(AlertPopupHelper.SUCCESS);
            AlertPopupHelper.show(5000);

            //} catch (err) {
            //    AlertPopupHelper.setHTML("Unable to load model <strong>"+file.name+"</strong><br/><small>Error: "+err.message+"</small>");
            //    AlertPopupHelper.setType(AlertPopupHelper.ERROR);
            //    AlertPopupHelper.show(5000);
            //}
          }
          fReader.readAsText(file);

          // reset input field
          $(this).val('');

          $('#file').off(NAMESPACE);
        });
      }

      function confirmOnLoadSettingEnabled() {
        var ret = true;
        if (window.localStorage) {
          var storedVal = window.localStorage.getItem(Config.LS_CONFIRM_ON_LOAD);
          if (storedVal != undefined) {
            ret = Util.parseBoolean(storedVal);
          }
        }
        return ret;
      }
    }

    return LoadCommand;
  }
);