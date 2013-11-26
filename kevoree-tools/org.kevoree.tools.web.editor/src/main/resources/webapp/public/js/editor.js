requirejs.config({
  //By default load any module IDs from js/lib
  baseUrl: 'js/lib',
  //except, if the module ID starts with "app",
  //load it from the js/app directory. paths
  //config is relative to the baseUrl, and
  //never includes a ".js" extension since
  //the paths config could be for a directory.
  paths: {
    bootstrap:      'bootstrap/src',
    app:            '../app',
    util:           '../app/util',
    abstraction:    '../app/editor/abstraction',
    visitor:        '../app/editor/visitor',
    control:        '../app/editor/control',
    resolver:       '../app/editor/resolver',
    presentation:   '../app/editor/presentation',
    factory:        '../app/editor/factory',
    command:        '../app/editor/command',
    templates:      '../app/templates'
  }
});


// Start the main app logic.
define(
  [
    'jquery',
    'kinetic',
    'factory/CFactory',
    'util/Config',
    'behave',
    'util/QueryString',

    'command/SaveCommand',
    'command/SaveAsKevsCommand',
    'command/SaveAsPNGCommand',
    'command/LoadCommand',
    'command/OpenKevsEditorCommand',
    'command/RunKevScriptCommand',
    'command/SettingsCommand',
    'command/LoadCoreLibrariesCommand',
    'command/MergeDefaultLibraryCommand',
    'command/ClearCommand',
    'command/InitCommand',
    'command/ClearInstancesCommand',
    'command/OpenFromNodeCommand',
    'command/ZoomInCommand',
    'command/ZoomDefaultCommand',
    'command/ZoomToCommand',
    'command/ZoomOutCommand',
    'command/ShowStatsCommand',
    'command/CheckModelCommand',
    'command/LoadSettingsCommand',
    'command/MergeCommand',
    'command/ListenToCommand',

    'bootstrap/tooltip',
    'bootstrap/modal',
    'bootstrap/collapse',
    'bootstrap/dropdown',
    'bootstrap/alert',
    'bootstrap/popover',
    'bootstrap/tab',

    'hammer',
    'touchpunch',
    'jqueryui/selectable'
  ],


  function ($, Kinetic, CFactory, Config, Behave, QueryString,
            SaveCommand, SaveAsKevsCommand, SaveAsPNGCommand, LoadCommand, OpenKevsEditorCommand, RunKevScriptCommand,
            SettingsCommand, LoadCoreLibrariesCommand, MergeDefaultLibraryCommand, ClearCommand, InitCommand, ClearInstancesCommand,
            OpenFromNodeCommand, ZoomInCommand, ZoomDefaultCommand, ZoomToCommand, ZoomOutCommand, ShowStatsCommand,
            CheckModelCommand, LoadSettingsCommand, MergeCommand, ListenToCommand,
            _bootstrap) {

    // init editor
    var editor = CFactory.getInstance().newEditor(Config.CONTAINER_ID);
    editor.getUI().create($('#'+Config.CONTAINER_ID).width(), $('#'+Config.CONTAINER_ID).height());

    // load editor's settings from Local Storage
    var loadSettingsCmd = new LoadSettingsCommand();
    loadSettingsCmd.execute(editor);

    // use Behave.js for KevScript Editor
    var kevsEditor = new Behave({
      textarea: document.getElementById('kev-script')
    });

    // create the controller that handles parameters in URL
    var qs = new QueryString();
    qs.process({
      zoom: function (scale) {
        // set editor zoom to the given scale if not wrong number
        var value = parseFloat(scale) || 1;
        if (value < 0) value = 0.1;
        var cmd = new ZoomToCommand();
        cmd.execute(editor, value);
      },
      menu: function (value) {
        var hide = (value == 'hide' || value == '0');
        if (hide) {
          editor.p2cHideLibTree();
        } else {
          editor.p2cShowLibTree();
        }
      },
      listen: function (uri) {
        var cmd = new ListenToCommand();
        cmd.execute(editor, uri);
      },
      open: {
        deps: ['protocol'],
        hasDeps: function (openVal, protocolVal) {
          // slight rewrite rules: allows 'ws' for 'ws://' and 'http' for 'http://'
          if      (protocolVal.indexOf('ws') == 0)    protocolVal = Config.WS;
          else if (protocolVal.indexOf('http') == 0)  protocolVal = Config.HTTP;

          var cmd = new OpenFromNodeCommand();
          cmd.execute(protocolVal, openVal, editor, false);
        },
        missDep: function (missField) {
          console.warn("Open from node impossible: '"+missField+"' field value required in URL with 'open' (http://example.com/?open=localhost:8000&protocol=tcp)");
        }
      }
    });

    $('.close').click(function () {
      // global behavior for alerts : close will remove 'in' class
      // in order for them to properly hide (with the CSS3 magic)
      $(this).parent().removeClass('in');
      $(this).parent().addClass('hide');
    });

    // safety check because one does not simply like when he loses
    // a 30 minutes work on a model by miss-pressing F5 button...if u no wat I mean
    $(window).bind('beforeunload', function() {
      var askBeforeLeaving = $('#ask-before-leaving').prop('checked');
      if (askBeforeLeaving) {
        return 'Leaving now will discard any changes you made.';
      }
    });

    // show zoom controls when mouse hovers #editor area
    $('#editor').on('mouseenter', function () {
      $('#zoom-controls').stop(true, true).delay(600).show('fast');
    });

    // hide zoom controls when mouse leaves #editor area
    $('#editor').on('mouseleave', function () {
      $('#zoom-controls').stop(true, true).delay(600).hide('fast');
    });

    // search core libraries keyup listener
    $('#search-corelib').on('keyup', function (e) {
      var keyword = $(this).val().toLowerCase();
      searchCoreLib(keyword);
      e.preventDefault();
      return false;
    });
    // search core libraries button click listener
    $('#search-corelib-button').on('click', function (e) {
      var keyword = $('#search-corelib').val().toLowerCase();
      searchCoreLib(keyword);
      e.preventDefault();
      return false;
    });
    function searchCoreLib(keyword) {
      // filter all corelib-item that matches the keyword, hide others
      $('.corelib-item-label').filter(function () {
        var libItem = $(this),
          itemName = libItem.text().toLowerCase();

        if (itemName.search(keyword) == -1) libItem.hide();
        else libItem.show();
      });
    }

    $('body').off('show.corelib-popup');
    $('body').on('show.corelib-popup', '#load-corelib-popup', function () {
      if ($('.corelib-item:checked').size() == 0) {
        $('#load-corelib').addClass('disabled');
      }
    });

    $('#open-node-uri').on('keyup', function (e) {
      if (e.which == 13) {
        var cmd = new OpenFromNodeCommand();
        var protocol = $('#open-node-protocol option:selected').val();
        var uri = $(this).val();

        cmd.execute(protocol, uri, editor, false);
      }
    });

    var initCmd = new InitCommand();
    initCmd.execute(editor);

    // ========================================
    // Listeners that trigger XXXCommand.execute(...)
    $('#load').click(function (e) {
      var cmd = new LoadCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#merge').click(function (e) {
      var cmd = new MergeCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#save-json').click(function (e) {
      var cmd = new SaveCommand();
      cmd.execute('json', editor);
      e.preventDefault();
    });

    $('#save-xmi').click(function (e) {
      var cmd = new SaveCommand();
      cmd.execute('xmi', editor);
      e.preventDefault();
    });

    $('#save-kevs').click(function (e) {
      var cmd = new SaveAsKevsCommand();
      cmd.execute();
      e.preventDefault();
    });

    $('#check-model').click(function (e) {
      var cmd = new CheckModelCommand();
      cmd.execute();
      e.preventDefault();
    });

    $('#save-png').click(function (e) {
      var cmd = new SaveAsPNGCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#open-from-node').click(function (e) {
      var cmd = new OpenFromNodeCommand();
      var protocol = $('#open-node-protocol option:selected').val();
      var uri = $('#open-node-uri').val();

      cmd.execute(protocol, uri, editor, true);
      e.preventDefault();
    });

    $('#open-kevs-editor').click(function (e) {
      var cmd = new OpenKevsEditorCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#show-stats').click(function (e) {
      var cmd = new ShowStatsCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#run-kevs').click(function (e) {
      var cmd = new RunKevScriptCommand();
      var script = $('#kev-script').val();
      cmd.execute(editor, script);
      e.preventDefault();
    });

    $('#settings').click(function (e) {
      var cmd = new SettingsCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#zoom-in').click(function (e) {
      var cmd = new ZoomInCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#editor').hammer().on("pinchout", function(e) {
      var cmd = new ZoomInCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#editor').hammer().on("pinchin", function(e) {
      var cmd = new ZoomOutCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#zoom-default').click(function (e) {
      var cmd = new ZoomDefaultCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#zoom-out').click(function (e) {
      var cmd = new ZoomOutCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#clear').click(function (e) {
      var cmd = new ClearCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#clear-instances').click(function (e) {
      var cmd = new ClearInstancesCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#model-load-corelib').click(function (e) {
      var cmd = new LoadCoreLibrariesCommand();
      cmd.execute(editor);
    });

    $('#load-corelib').click(function (e) {
      var cmd = new MergeDefaultLibraryCommand();
      cmd.execute(editor);
      e.preventDefault();
    });

    $('#listen-to').click(function () {
      var cmd = new ListenToCommand(),
        uri = $('#listen-to-uri').val();
      cmd.execute(editor, uri);
    });
    // END Listeners that trigger Cmd.execute()
    // ========================================

    return {};
  }
);
