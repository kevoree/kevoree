define(
  ['kevoree'],
  function (Kevoree) {

    function InitCommand() {}

    InitCommand.prototype.execute = function execute(editor) {
      $.ajax({
        type: 'GET',
        url: 'init',
        success: function (res) {
          try {
            var loader = new Kevoree.org.kevoree.loader.JSONModelLoader();
            var model = loader.loadModelFromString(JSON.stringify(res)).get(0);
            editor.setModel(model);
          } catch (err) {
            console.log("Failance init", err.stack);
          }
        },
        error: function (e) {
          console.log(e);
        }
      });
    }

    return InitCommand;
  }
);