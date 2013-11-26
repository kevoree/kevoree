define(
    [
        'kevoree'
    ],
    function (Kevoree) {
        var TIMEOUT = 10000;

        function PushToCommand() {
            this._id = null;
        }

        PushToCommand.prototype.execute = function (node, grp, model, callbacks) {
            var that = this;
            clearTimeout(this._id);
            this._id = setTimeout(function () {
                if (callbacks.error && typeof(callbacks.error) == "function") {
                    callbacks.error('Timeout: '+TIMEOUT+'ms');
                }
            }, TIMEOUT);

            try {
                var serializer = new Kevoree.org.kevoree.serializer.JSONModelSerializer();
                var jsonModel = JSON.parse(serializer.serialize(model));

              $.ajax({
                url: 'push',
                type: 'POST',
                timeout: 60000, // 1 minute timeout
                data: {
                  destNodeName: node.getName(),
                  grpName: grp.getName(),
                  model: jsonModel
                },
                dataType: 'json',
                success: function (data) {
                  switch (data.result) {
                    case 1:
                      if (callbacks.success && typeof(callbacks.success) == "function") {
                        callbacks.success(this);
                      }
                      clearTimeout(that._id);
                      break;

                    case -1:
                    default:
                      if (callbacks.error && typeof(callbacks.error) == "function") {
                        callbacks.error(data.message);
                      }
                      clearTimeout(that._id);
                      break;
                  }
                },
                error: function () {
                  if (callbacks.error && typeof(callbacks.error) == "function") {
                    callbacks.error('Internal Server Error');
                  }
                  clearTimeout(that._id);
                }
              });
            } catch (err) {
                if (callbacks.error && typeof(callbacks.error) == "function") {
                    callbacks.error(err.message);
                }
                clearTimeout(that._id);
            }
        }

        return PushToCommand;
    }
);