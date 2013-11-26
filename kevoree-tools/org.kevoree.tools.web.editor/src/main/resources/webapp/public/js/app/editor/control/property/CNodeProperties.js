define(
    [
        'abstraction/property/KNodeProperties',
        'presentation/property/UINodeProps',
        'control/AController',
        'command/PushToCommand',
        'command/PullFromCommand',
        'util/Pooffs'
    ],
    function (KNodeProperties, UINodeProps, AController, PushToCommand, PullFromCommand, Pooffs) {

        Pooffs.extends(CNodeProperties, AController);
        Pooffs.extends(CNodeProperties, KNodeProperties);

        function CNodeProperties(node) {
            KNodeProperties.prototype.constructor.call(this, node);

            this._ui = new UINodeProps(this);
            this._pushCmd = new PushToCommand();
            this._pullCmd = new PullFromCommand();
        }

        CNodeProperties.prototype.p2cPushModel = function (grpName) {
            if (grpName != undefined) {
                var editor = this.getNode().getEditor(),
                    grp = editor.getEntity(grpName),
                    that = this;

                this._pushCmd.execute(this.getNode(), grp, editor.getModel(), {
                    success: function () {
                        that._ui.c2pPushModelEndedWell();
                    },
                    error: function (msg) {
                        that._ui.c2pUnableToPush(msg);
                    }
                });

                this._ui.c2pPushModelStarted();
            }
        }

        CNodeProperties.prototype.p2cPullModel = function (grpName) {
            if (grpName != undefined) {
                var editor = this.getNode().getEditor(),
                    grp = editor.getEntity(grpName),
                    that = this;

                this._pullCmd.execute(this.getNode(), grp, editor.getModel(), {
                    success: function (model) {
                        editor.setModel(model);
                        that._ui.c2pPullModelEndedWell();
                    },
                    error: function (msg) {
                        that._ui.c2pUnableToPull(msg);
                    }
                });

                this._ui.c2pPullModelStarted();
            }
        }

        CNodeProperties.prototype.p2cSelectedNodeNetwork = function (nodeName) {
            var net = this.getNodeNetworkByInitByName(nodeName);
            if (net == null) {
                var initByNode = this.getNode().getEditor().getEntity(nodeName),
                    nodeNetwork = require('factory/CFactory').getInstance().newNodeNetwork(initByNode, this);
                this.addNodeNetwork(nodeNetwork);
            }
        }

        CNodeProperties.prototype.p2cUnselectedNodeNetwork = function (nodeName) {
            var nets = this.getNodeNetworks(),
                removed = false;

            if (nets.length > 1) {
                for (var i=0; i < nets.length; i++) {
                    if (nets[i].getInitBy().getName() == nodeName) {
                        this.removeNodeNetwork(nets[i]);
                        removed = true;
                    }
                }
            }

            if (!removed) this._ui.c2pSelectNodeNetwork(nodeName);
        }

        // Override KNodeProperties.addLink(link)
        CNodeProperties.prototype.addLink = function (link) {
            KNodeProperties.prototype.addLink.call(this, link);
            this._ui.c2pNodeLinkAdded(link.getUI());
        }

        CNodeProperties.prototype.p2cAddNodeLink = function () {
            var link = require('factory/CFactory').getInstance().newNodeLink(this);
            this.addLink(link);
        }

        // Override KNodeProperties.removeLink(link)
        CNodeProperties.prototype.removeLink = function (link) {
            KNodeProperties.prototype.removeLink.call(this, link);

            var links = this.getLinks();
            // set default node link to active (index: 0)
            for (var i in links) {
                if (i > 0) links[i].getUI().setActive(false);
                else links[i].getUI().setActive(true);
            }

            // check nodeLinks.length in order to tell to the UI to enable/disable delete button
            if (links.length == 1) {
                this._ui.c2pDisableDeleteNodeLinkButton();
            } else {
                this._ui.c2pEnableDeleteNodeLinkButton();
            }

            this._ui.c2pNodeLinkRemoved(link.getUI());
        }

        CNodeProperties.prototype.p2cDeleteNodeLink = function (id) {
            // always keep at least one node link
            var links = this.getLinks();
            if (links.length > 1) {
                for (var i in links) {
                    if (links[i]._id == id) {
                        this.removeLink(links[i]);
                        break;
                    }
                }
            }
        }

        CNodeProperties.prototype.p2cSaveProperties = function (props) {
            // node's proxy
            this.getNode().p2cSaveProperties(props);
        }

        CNodeProperties.prototype.p2cRemoveEntity = function () {
            // node's proxy
            this.getNode().p2cRemoveEntity();
        }

        CNodeProperties.prototype.p2cSaveNetworkProperties = function () {
            this.getNode().getEditor().updateModel(this);
        }

        function checkIpAndPort(options) {
            var ip, port;
            for (var i in options.values) {
                if (options.values[i].getTargetNode()) {
                    if (options.values[i].getTargetNode().getName() == options.nodeName) {
                        if (options.values[i].getAttribute().getName() == 'ip') {
                            ip = options.values[i].getValue();
                        }
                        if (options.values[i].getAttribute().getName() == 'port') {
                            port = options.values[i].getValue();
                        }
                    }
                }
                if (ip && port) break;
            }

            if (ip && port) options.found.call(this, ip, port);
            else options.none.call();
        }

        return CNodeProperties;
    }
);