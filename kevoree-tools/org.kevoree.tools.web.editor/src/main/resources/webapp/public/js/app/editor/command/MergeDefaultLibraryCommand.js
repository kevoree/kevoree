define(
  [
    'jquery',
    'util/ModelHelper',
    'util/AlertPopupHelper',
    'kevoree'
  ],

  function ($, ModelHelper, AlertPopupHelper, Kevoree) {
    var NAMESPACE = ".merge-default-library-command";

    function MergeDefaultLibraryCommand() {}

    MergeDefaultLibraryCommand.prototype.execute = function (editor) {
      // hide alert when popup is closed
      $('body').off(NAMESPACE)
      $('body').on('hidden'+NAMESPACE, '#load-corelib-popup', function () {
        $('#loading-corelib').hide();
        $('#load-corelib').show();
        $('#load-corelib-popup-error-content').html("");
        $('#load-corelib-popup-error').addClass('hide');
      });

      if ($('.corelib-item:checked').size() > 0) {
        $('#load-corelib').hide();
        $('#loading-corelib').show();

        // retrieve selected libraries from DOM (checked inputs)
        var libraries = {};

        $('.corelib-item:checked').each(function () {
          var corelib  = $(this),
              platform = corelib.attr('data-library-platform'),
              library  = editor.getLibraries(platform)[corelib.attr('data-library-id')];

          libraries[platform] = libraries[platform] || [];

          libraries[platform].push({
            artifactID: library.artifactID,
            groupID: library.groupID,
            version: library.version
          });
        });

        $.ajax({
          url: 'merge',
          data: { libz: libraries },
          dataType: 'json',
          success: function (data) {
            switch (data.result) {
              case 1:
                try {
                  var loader = new Kevoree.org.kevoree.loader.JSONModelLoader(),
                    receivedModel = loader.loadModelFromString(JSON.stringify(data.model)).get(0);
                  editor.mergeModel(receivedModel);
                  $('#loading-corelib').hide();
                  $('#load-corelib').show();

                  // close popup
                  $('#load-corelib-popup').modal('hide');
                } catch (err) {
                  console.log("MergeDefaultLib cmd caught error: ", err);
                  $('#loading-corelib').hide();
                  $('#load-corelib').show();

                  $('#load-corelib-popup-error-content').html("Unable to load received model.");
                  $('#load-corelib-popup-error').removeClass('hide');
                }
                break;

              default:
                $('#loading-corelib').hide();
                $('#load-corelib').show();

                $('#load-corelib-popup-error-content').html(data.message);
                $('#load-corelib-popup-error').removeClass('hide');
                break;
            }
          },
          error: function (err) {
            console.error(err);
            $('#loading-corelib').hide();
            $('#load-corelib').show();

            $('#load-corelib-popup-error-content').html(err.message);
            $('#load-corelib-popup-error').removeClass('hide');
          }
        });
      }
    }

    return MergeDefaultLibraryCommand;
  }
);
