define(
    [
        'kevoree'
    ],
    function (Kevoree) {
        var TIMEOUT = 10000;

        function PullFromCommand() {
            this._id = null;
        }

        PullFromCommand.prototype.execute = function (node, grp, model, callbacks) {
            var that = this;
            clearTimeout(this._id);
            this._id = setTimeout(function () {
                if (callbacks.error && typeof(callbacks.error) == "function") {
                    callbacks.error.call(this, 'Timeout: '+TIMEOUT+'ms');
                }
            }, TIMEOUT);

            var serializer = new Kevoree.org.kevoree.serializer.JSONModelSerializer();
            var jsonModel = JSON.parse(serializer.serialize(model));

            $.ajax({
                url: 'pull',
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
                                var loader = new Kevoree.org.kevoree.loader.JSONModelLoader(),
                                    pulledModel = loader.loadModelFromString(JSON.stringify(data.model)).get(0);

                                callbacks.success.call(this, pulledModel);
                            }
                            clearTimeout(that._id);
                            break;

                        case -1:
                        default:
                            if (callbacks.error && typeof(callbacks.error) == "function") {
                                callbacks.error.call(
                                    this,
                                    'Something went wrong while pulling model to '+node.getName()+' via '+grp.getName()
                                );
                            }
                            clearTimeout(that._id);
                            break;
                    }
                },
                error: function () {
                    if (callbacks.error && typeof(callbacks.error) == "function") {
                        callbacks.error.call(this, 'Internal Server Error');
                    }
                    clearTimeout(that._id);
                }
            });
        }

        return PullFromCommand;
    }
);