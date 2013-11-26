define(
  [
    'kevoree-commons',
    'kevoree'
  ],

  function (KevoreeCommons, Kevoree) {
    return KevoreeCommons.Resolver.extend({
      toString: 'NPMResolver',

      resolve: function (deployUnit, callback) {
        $.ajax({
          url: 'resolve',
          type: 'POST',
          timeout: 60000,
          data: {
            name: deployUnit.name,
            version: deployUnit.version
          },
          success: function (res) {
            switch (res.result) {
              case 1:
                console.log("success", res);
                var loader = new Kevoree.org.kevoree.loader.JSONModelLoader();
                var model = loader.loadModelFromString(res.model).get(0);
                callback(null, null, model);
                break;

              default:
                console.log("error", res.message);
                break;
            }
          },
          error: function (res) {
            console.log("error", res);
          }
        });
      },

      uninstall: function (deployUnit, callback) {
        // we dont really install thing client-side, so there not much
        // to do in this uninstall function.
        callback();
      }
    });
  }
);