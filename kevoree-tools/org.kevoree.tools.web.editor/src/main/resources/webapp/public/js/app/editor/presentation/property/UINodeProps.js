define(
    [
        'jquery',
        'util/Pooffs',
        'util/AlertPopupHelper',
        'presentation/property/UIInstanceProps',
        'templates/node-properties',
        'bootstrap/multiselect'
    ],
    function ($, Pooffs, AlertPopupHelper, UIInstanceProps, nodePropsTemplate, _bootstrap) {

        var NAMESPACE           = ".ui-node-props",
            PUSH_ACTION         = "node-push-action",
            PULL_ACTION         = "node-pull-action",
            NODE_LINKS_TABS     = 'node-links-tabs',
            NODE_LINKS_CONTENTS = 'node-links-contents',
            INIT_BY_NODE        = 'initby-node',
            ADD_NODE_LINK       = 'node-link-add',
            DEL_NODE_LINK       = 'node-link-delete',
            HTML5_ATTR_TAG      = 'data-node-link-id';

        UINodeProps.INIT_BY_NODES   = "initby-nodes";
        UINodeProps.NODE_NETWORK_IP = "node-network-ip";
        UINodeProps.NODE_LINKS_PROP = "node-links-properties";

        Pooffs.extends(UINodeProps, UIInstanceProps);

        function UINodeProps(ctrl) {
            UIInstanceProps.prototype.constructor.call(this, ctrl);
        }

        // Override getHTML()
        UINodeProps.prototype.getHTML = function () {
            var html = UIInstanceProps.prototype.getHTML.call(this), // super.getHTML()
                model = this._ctrl.getEditor().getModel(),
                that = this;

            var templateParams = {
                initBy: getInitByNodes(),
                nodeLinks: getNodeLinks(),
                groups: getThisNodeGroups(this._ctrl.getName())
            }

            function getInitByNodes() {
                var initByNodes = [],
                    nodes = model.getNodes(),
                    nets = that._ctrl.getNodeNetworks();

                // check if ctrl already has nodeNetwork for those nodes
                for (var i=0; i < nodes.size(); i++) {
                    var hasNetwork = false;

                    for (var j=0; j < nets.length; j++) {
                        if (nets[j].getInitBy().getName() == nodes.get(i).getName()) {
                            hasNetwork = true;
                        }
                    }

                    initByNodes.push({
                        selected: hasNetwork,
                        name: nodes.get(i).getName()
                    });
                }

                return initByNodes;
            }

            function getNodeLinks() {
                var nodeLinks = [],
                    links = that._ctrl.getLinks(),
                    active = false;

                // activate 0-indexed tab if none active
                for (var i in links) if (links[i].getUI().isActive()) active = true;
                if (!active) links[0].getUI().setActive(true);

                for (var i in links) {
                    nodeLinks.push({
                        tabHTML: links[i].getUI().getTabHTML(),
                        contentHTML: links[i].getUI().getContentHTML()
                    });
                }

                return nodeLinks;
            }

            function getThisNodeGroups(name) {
                var grps = model.getGroups();
                var ret = [];
                for (var i=0; i < grps.size(); i++) {
                    var nodes = grps.get(i).getSubNodes();
                    for (var j=0; j < nodes.size(); j++) {
                        if (nodes.get(j).getName() == name) {
                            ret.push(grps.get(i).getName());
                        }
                    }
                }
                return ret;
            }

            return html + nodePropsTemplate(templateParams);
        }

        UINodeProps.prototype.onHTMLAppended = function () {
            UIInstanceProps.prototype.onHTMLAppended.call(this);

            var that = this;
            if (this._ctrl.getLinks().length > 1) this.c2pEnableDeleteNodeLinkButton();

            // initby nodes multiselect
            $('#'+UINodeProps.INIT_BY_NODES).multiselect({
                includeSelectAllOption: true,
                maxHeight: 200,
                onChange: function (element, checked) {
                    console.log("onChange", $(element).val());
                    if ($(element).hasClass('initby-node')) {
                        if (checked) {
                            // init by node selected
                            that._ctrl.p2cSelectedNodeNetwork($(element).val());
                        } else {
                            // init by node unselected
                            that._ctrl.p2cUnselectedNodeNetwork($(element).val());
                        }
                    }
                }
            });

            var pushBtn = $('#'+PUSH_ACTION),
                pullBtn = $('#'+PULL_ACTION);

            // push button click listener
            pushBtn.off(NAMESPACE);
            pushBtn.on('click'+NAMESPACE, function () {
                $('#node-push-pull-error').hide();
                that._ctrl.p2cPushModel($('#node-group-action').val());
            });

            // pull btn click listener
            pullBtn.off(NAMESPACE);
            pullBtn.on('click'+NAMESPACE, function () {
                $('#node-push-pull-error').hide();
                that._ctrl.p2cPullModel($('#node-group-action').val());
            });

            // add node link button click listener
            $('#'+ADD_NODE_LINK).off(NAMESPACE);
            $('#'+ADD_NODE_LINK).on('click'+NAMESPACE, function () {
                that._ctrl.p2cAddNodeLink();
            });

            // delete node link button click listener
            $('#'+DEL_NODE_LINK).off(NAMESPACE);
            $('#'+DEL_NODE_LINK).on('click'+NAMESPACE, function () {
                var tab = $('#'+NODE_LINKS_TABS+' li.active');
                that._ctrl.p2cDeleteNodeLink(parseInt(tab.attr(HTML5_ATTR_TAG)));
            });

            var links = this._ctrl.getLinks();
            registerListenersForTabs(links);

            // tell nodeLinks that they were added to DOM
            for (var i=0; i < links.length; i++) {
                links[i].getUI().onHTMLAppended();
            }
        }

        UINodeProps.prototype.onSaveProperties = function () {
            UIInstanceProps.prototype.onSaveProperties.call(this);
            // tell controller that user wants to save network properties too
            this._ctrl.p2cSaveNetworkProperties();
        }

        UINodeProps.prototype.getPropertiesValues = function () {
            var props = UIInstanceProps.prototype.getPropertiesValues.call(this);

            var nodes = [];
            $('#'+UINodeProps.INIT_BY_NODES+' option.'+INIT_BY_NODE+':selected').each(function () {
                nodes.push($(this).val());
            });
            props[UINodeProps.INIT_BY_NODES] =  nodes;

            return props;
        }

        UINodeProps.prototype.c2pSelectNodeNetwork = function (nodeName) {
            $('#'+UINodeProps.INIT_BY_NODES).multiselect('select', nodeName);
        }

        UINodeProps.prototype.c2pNodeLinkAdded = function (link) {
            // set last added tab to selected (active)
            var links = this._ctrl.getLinks();
            for (var i=0; i < links.length; i++) links[i].getUI().setActive(false);
            link.setActive(true);

            // add HTML to DOM
            $('#'+NODE_LINKS_TABS).append(link.getTabHTML());
            $('#'+NODE_LINKS_CONTENTS).append(link.getContentHTML());
            link.onHTMLAppended();
            registerListenersForTabs(links);

            if (links.length > 1) {
                $('#'+DEL_NODE_LINK).removeClass('disabled');
            } else {
                $('#'+DEL_NODE_LINK).addClass('disabled');
            }
        }

        UINodeProps.prototype.c2pNodeLinkRemoved = function (link) {
            $('#node-link-root-'+link._ctrl._id).remove();
            $('#node-link-'+link._ctrl._id).remove();
        }

        UINodeProps.prototype.c2pDisableDeleteNodeLinkButton = function () {
            $('#'+DEL_NODE_LINK).addClass('disabled');
        }

        UINodeProps.prototype.c2pEnableDeleteNodeLinkButton = function () {
            $('#'+DEL_NODE_LINK).removeClass('disabled');
        }


        UINodeProps.prototype.c2pPushModelStarted = function () {
            $('#node-progress-bar').addClass('progress-info progress-striped');
            $('#node-progress-bar').removeClass('bar-success bar-danger');
            $('#node-progress-bar').show();
        }

        UINodeProps.prototype.c2pPushModelEndedWell = function () {
            $('#node-progress-bar').removeClass('progress-info progress-striped');
            $('#node-progress-bar .bar').addClass('bar-success');
        }

        UINodeProps.prototype.c2pPullModelStarted = function () {
            $('#node-progress-bar').addClass('progress-info progress-striped');
            $('#node-progress-bar').removeClass('bar-success bar-danger');
            $('#node-progress-bar').show();
        }

        UINodeProps.prototype.c2pPullModelEndedWell = function () {
            $('#node-progress-bar').removeClass('progress-info progress-striped');
            $('#node-progress-bar .bar').addClass('bar-success');
            $('#prop-popup').modal('hide');

            AlertPopupHelper.setText("Model updated successfully");
            AlertPopupHelper.setType(AlertPopupHelper.SUCCESS);
            AlertPopupHelper.show(5000);
        }

        UINodeProps.prototype.c2pUnableToPush = function (msg) {
            $('#node-progress-bar').removeClass('progress-info progress-striped');
            $('#node-progress-bar .bar').addClass('bar-warning');
            $('#node-push-pull-error').html('Unable to <strong>push</strong> model. ('+msg+')');
            $('#node-push-pull-error').show('fast');
        }

        UINodeProps.prototype.c2pUnableToPull = function (msg) {
            $('#node-progress-bar').removeClass('progress-info progress-striped');
            $('#node-progress-bar .bar').addClass('bar-warning');
            $('#node-push-pull-error').html('Unable to <strong>pull</strong> model. ('+msg+')');
            $('#node-push-pull-error').show('fast');
        }

        function registerListenersForTabs(links) {
            $('#'+NODE_LINKS_TABS+' a[data-toggle="tab"]').off('shown'+NAMESPACE);
            $('#'+NODE_LINKS_TABS+' a[data-toggle="tab"]').on('shown'+NAMESPACE, function (e) {
                for (var i in links) {
                    links[i].getUI().setActive(links[i]._id == $(e.target).parent().attr(HTML5_ATTR_TAG));
                }
            })
        }

        return UINodeProps;
    }
);