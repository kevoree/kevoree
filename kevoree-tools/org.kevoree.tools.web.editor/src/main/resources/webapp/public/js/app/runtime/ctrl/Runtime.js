define(
    [
        'ui/RuntimeUI',
        'app_util/QueryString',
        'core/KevoreeJSBootstrap',
        'util/Logger'
    ],

    function (RuntimeUI, QueryString, KevoreeJSBootstrap, Logger) {
        var DEFAULT_SERVER_URL = "kevoree.org:9042/default";

        function RuntimeController() {
            this._isStarted = false;
            this._tabs = [];

            this._bootstrapper = new KevoreeJSBootstrap();

            this._ui = new RuntimeUI(this);
            var groups = doARealModelParsingToGetGroups();
            this._ui.inflateGroupSelector(groups);
            this._queryString = new QueryString();
            var runtime = this;
            this._queryString.process({
                name: function (name) {
                    runtime.setNodeName(name);
                },
                server: function (ip) {
                    runtime.setServerIP(ip);
                }
            });
        }

        RuntimeController.prototype.p2cStartNode = function (params) {
            // never trust user inputs
            params.nodeName = (params.nodeName && params.nodeName.length > 0) ? params.nodeName : 'node'+randomChar(4);
            params.serverUrl = (params.serverUrl && params.serverUrl.length > 0) ? params.serverUrl : DEFAULT_SERVER_URL;

            if (!this._isStarted) {
                // TODO real node start

                var runtime = this;
                this._bootstrapper.start(params.nodeName, params.groupName, params.serverUrl, function (started) {
                    runtime._started = started;
                    if (started) {
                        runtime._ui.c2pNodeStarted(params);
                    } else {
                        runtime._ui.c2pNodeStartFailed();
                    }
                });
            }
        }

        RuntimeController.prototype.p2cStopNode = function () {
            // TODO real stop node
            if (this._isStarted) {
                this._isStarted = false;
                this._ui.c2pNodeStopped();
            }
        }

        RuntimeController.prototype.addTab = function (name, content) {
            // ensure no tab has the same name
            for (var i=0; i < this._tabs.length; i++) {
                if (this._tabs[i].name == name) {
                    // someone already has this name
                    name = 'Tab'+randomChar(3); // new random name
                    break; // we can break the loop
                }
            }

            // keep ref of the tab
            this._tabs.push({
                name: name,
                content: content
            });

            // update ui accordingly
            this._ui.addTab(name, content);
        }

        RuntimeController.prototype.removeTab = function (name) {
            if (name) {
                for (var i=0; i < this._tabs.length; i++) {
                    if (this._tabs[i].name == name) {
                        this._ui.removeTab(name);
                        this._tabs.slice(i, 1);
                        return;
                    }
                }
            } else {
                var lastElem = this._tabs.pop();
                if (lastElem) this._ui.removeTab(lastElem.name);
            }
        }

        RuntimeController.prototype.setNodeName = function (name) {
            this._ui.c2pSetNodeName(name);
        }

        RuntimeController.prototype.setServerIP = function (ip) {
            this._ui.c2pSetServerIP(ip);
        }

        function randomChar(length) {
            var text = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

            for(var i=0; i < length; i++)
                text += possible.charAt(Math.floor(Math.random() * possible.length));

            return text;
        }

        function doARealModelParsingToGetGroups() {
            // TODO fake values, read that from model
            return ['WebRTCGroup'];
        }

        return RuntimeController;
    }
);