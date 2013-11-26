define(
  [
    'jquery',
    'templates/corelib-items'
  ],

  function ($, CorelibItemsTemplate) {
    function LoadCoreLibrariesCommand() {}

    LoadCoreLibrariesCommand.prototype.execute = function (editor) {
      // reinitializing popup content
      function loadingHTML(platform) {
        return '<div class="well"><p><img src="img/ajax-loader-small.gif" alt="Loading"/> Please wait while '+platform+' libraries are loading...</p></div>';
      }
      $('#corelib-javase').html(loadingHTML('JavaSE'));
      $('#corelib-android').html(loadingHTML('Android'));
      $('#corelib-javascript').html(loadingHTML('Javascript'));
      $('#corelib-sky').html(loadingHTML('SKY'));

      // loading libraries
      loadLibs('javase', editor);
      loadLibs('android', editor);
      loadLibs('javascript', editor);
      loadLibs('sky', editor);
    }

    function loadLibs(platform, editor) {
      $.ajax({
        url: 'load',
        //timeout: 10000, // 10 seconds timeout
        data: {platform: platform},
        dataType: 'json',
        success: function (data) {
          switch (data.result) {
            case 1:
              // load libraries
              editor.addLibraries(platform, data.libraries);
              $('#corelib-'+platform).html(
                CorelibItemsTemplate({
                  platform: platform,
                  items: data.libraries
                })
              );
              // register listener to enable/disable Load button
              $('.corelib-item').off('click');
              $('.corelib-item').on('click', function () {
                if ($(this).prop('checked') == true) {
                  // if there is at least one item selected = enable button
                  $('#load-corelib').removeClass('disabled');
                } else {
                  if ($('.corelib-item:checked').size() == 0) {
                    // no item are checked = disable button
                    $('#load-corelib').addClass('disabled');
                  }
                }
              });

              // register listener for 'select all' checkbox
              $('#corelib-selectall-'+platform).off('click');
              $('#corelib-selectall-'+platform).on('click', function () {
                if ($(this).prop('checked')) {
                  $('.corelib-item[data-library-platform='+platform+']').prop('checked', true);
                  $('#load-corelib').removeClass('disabled');
                } else {
                  $('.corelib-item[data-library-platform='+platform+']').prop('checked', false);
                  if ($('.corelib-item:checked').size() == 0) {
                    $('#load-corelib').addClass('disabled');
                  }
                }
              });
              break;

            default:
              console.log(platform+" libraries load error", data.message);
              $('#corelib-'+platform).html('<div class="well"><p>Something went wrong while loading libraries :-(<br/><strong>Error:</strong> '+data.message+'</p></div>');
              break;
          }
        },
        error: function (err) {
          // fail
          console.log(platform+" libraries load error", err);
          $('#corelib-'+platform).html('<p>Something went wrong while loading libraries :-(</p>');
        }
      });
    }

    return LoadCoreLibrariesCommand;
  }
);