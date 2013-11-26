define(
    function () {
        function KNodeProperties(node) {
            this._node = node;
            this._nets = [];
            this._links = [];

            // defaults
            this._nets.push(require('factory/CFactory').getInstance().newNodeNetwork(node, node));
            this._links.push(require('factory/CFactory').getInstance().newNodeLink(this));
        }

        KNodeProperties.prototype.addNodeNetwork = function (net) {
            var index = this._nets.indexOf(net);
            if (index == -1) {
                this._nets.push(net);
                this._node.getEditor().updateModel(this);
            }
        }

        KNodeProperties.prototype.removeNodeNetwork = function (net) {
            var index = this._nets.indexOf(net);
            if (index != -1) {
                this._nets.splice(index, 1);
                net.remove();
            }
        }

        KNodeProperties.prototype.getNodeNetworkByInitByName = function (name) {
            for (var i in this._nets) {
                if (this._nets[i].getInitBy().getName() == name) return this._nets[i];
            }
            return null;
        }

        KNodeProperties.prototype.addLink = function (link) {
            var index = this._links.indexOf(link);
            if (index == -1) {
                this._links.push(link);
            }
        }

        KNodeProperties.prototype.removeLink = function (link) {
            var index = this._links.indexOf(link);
            if (index != -1) {
                this._links.splice(index, 1);
                link.remove();
            }
        }

        KNodeProperties.prototype.removeAllLinks = function () {
            var links = this._links.slice(0); // clone links
            for (var i in links) this.removeLink(links[i]);
            this._links.length = 0;
        }

        KNodeProperties.prototype.getLinks = function () {
            return this._links;
        }

        KNodeProperties.prototype.getNodeNetworks = function () {
            return this._nets;
        }

        KNodeProperties.prototype.getEditor = function () {
            // node's proxy
            return this._node.getEditor();
        }

        KNodeProperties.prototype.getType = function () {
            // node's proxy
            return this._node.getType();
        }

        KNodeProperties.prototype.getEntityType = function () {
            // node's proxy
            return this._node.getEntityType();
        }

        KNodeProperties.prototype.getName = function () {
            // node's proxy
            return this._node.getName();
        }

        KNodeProperties.prototype.getConnectedFragments = function () {
            // node's proxy
            return this._node.getConnectedFragments();
        }

        KNodeProperties.prototype.getDictionary = function () {
            // node's proxy
            return this._node.getDictionary();
        }

        KNodeProperties.prototype.getNodeProperties = function () {
            // node's proxy
            return this._node.getNodeProperties();
        }

        KNodeProperties.prototype.getNode = function () {
            return this._node;
        }

        KNodeProperties.prototype.remove = function () {
            this._node.getEditor().removeFromModel(this);
        }

        KNodeProperties.prototype.accept = function (visitor) {
            visitor.visitNodeProperties(this);
        }

        return KNodeProperties;
    }
);