define(
    [
        'util/Logger',
        'jquery',
        'jqueryui/effect-highlight',
        'bootstrap/tab'
    ],

    function (Logger, $) {
        function RuntimeUI(ctrl) {
            this._ctrl = ctrl;
            this._header = $('#header');
            this._tabsContainer = $('#tabs-container');
            this._tabs = $('#tabs');
            this._tabsPane = $('#tabs-container .tab-pane');
            this._tabCounter = this._tabsPane.size();
            this._startNodeBtn = $('#node-start');
            this._stopNodeBtn = $('#node-stop');
            this._nodeName = $('#node-name');
            this._grpSelect = $('#node-grp');
            this._serverUrl = $('#server-ip');
            this._actionClearLog = $('#action-clear-log');
            this._tabsMap = [];

            configUI(this);
            registerCallbacks(this);
        }

        // private method
        function configUI(ui) {
            // refresh tabs related variables
            ui._tabsPane = $('#tabs-container .tab-pane');
            ui._tabs = $('#tabs');
            ui._tabCounter = ui._tabsPane.size();

            function resize() {
                var fullHeight = $('body').outerHeight(true),
                    headerHeight = ui._header.outerHeight(true),
                    tabsContainerPaddingTop = ui._tabsContainer.css('padding-top'),
                    tabsHeight = ui._tabs.outerHeight(true);

                var computedPadding = parseInt(tabsContainerPaddingTop.substr(0, tabsContainerPaddingTop.length-2));
                tabsContainerPaddingTop = (computedPadding == 0) ? 50 : computedPadding;
                ui._tabsPane.each(function () {
                    $(this).height((fullHeight - headerHeight - tabsHeight - tabsContainerPaddingTop)+'px');
                });
            }
            $(document).ready(resize);
            $(window).resize(resize);
        }

        // private method
        function registerCallbacks(ui) {
            ui._nodeName.on('keyup', function (e) {
                if(e.keyCode == 13) {
                    ctrlStartNode(ui);
                }
            });

            ui._startNodeBtn.on('click', function () {
                ctrlStartNode(ui);
            });

            ui._stopNodeBtn.on('click', function () {
                ui._ctrl.p2cStopNode();
            });

            ui._actionClearLog.on('click', function (e) {
                Logger.clear();
                e.preventDefault();
            });

            ui._serverUrl.on('click', function (e) {
                e.preventDefault();
                return false;
            });
        }

        RuntimeUI.prototype.addTab = function (name, content) {
            this._tabCounter += 1;
            var tabID = 'appended-tab-' + (this._tabCounter),
                contentID = 'tab-content-' + (this._tabCounter);

            $('#tabs').append(
                '<li id="'+tabID+'">' +
                    '<a href="#'+contentID+'" data-toggle="tab">'+name+'</a>' +
                '</li>');

            $('#tabs-content').append(
                '<div id="'+contentID+'" class="tab-pane in">' +
                    '<p>Your browser does not support Shadow DOM. You should consider using a real one (like Google Chrome or Firefox)</p>' +
                '</div>'
            );

            // using Shadow DOM to encapsulate component's view (own scope for CSS and script)
            var tabRoot = document.getElementById(contentID).createShadowRoot();
            tabRoot.innerHTML = content;

            $("#"+tabID).effect('highlight', {color: '#fff'}, 500);
            configUI(this);
            this._tabsMap.push({
                tab_id: tabID,
                content_id: contentID,
                name: name
            });
        }

        RuntimeUI.prototype.removeTab = function (name) {
            for (var i=0; i < this._tabsMap.length; i++) {
                if (this._tabsMap[i].name == name) {
                    $('#'+this._tabsMap[i].tab_id).empty();
                    $('#'+this._tabsMap[i].content_id).empty();
                    this._tabsMap.splice(i, 1);
                    return;
                }
            }
        }

        RuntimeUI.prototype.c2pNodeStarted = function (params) {
            this._nodeName.val(params.nodeName);
            this._startNodeBtn.addClass('disabled');
            this._stopNodeBtn.removeClass('disabled');
            Logger.log("Starting "+params.nodeName+" with "+this._grpSelect.find('option[value="'+params.groupName+'"]').text());
        }

        RuntimeUI.prototype.c2pNodeStartFailed = function () {
            this._startNodeBtn.effect('highlight', {color: '#f00'});
        }

        RuntimeUI.prototype.c2pSetNodeName = function (name) {
            this._nodeName.val(name);
        }

        RuntimeUI.prototype.c2pSetServerIP = function (ip) {
            this._serverUrl.val(ip);
        }

        RuntimeUI.prototype.c2pNodeStopped = function () {
            this._startNodeBtn.removeClass('disabled');
            this._stopNodeBtn.addClass('disabled');
            Logger.log("Node stopped");
        }

        RuntimeUI.prototype.inflateGroupSelector = function (groups) {
            for (var i=0; i < groups.length; i++) {
                this._grpSelect.append('<option value="'+groups[i]+'">'+groups[i]+'</option>');
            }
        }

        // private method
        function ctrlStartNode(ui) {
            ui._ctrl.p2cStartNode({
                nodeName: ui._nodeName.val(),
                groupName: ui._grpSelect.find('option:selected').val(),
                serverUrl: ui._serverUrl.val()
            });
        }

        return RuntimeUI;
    }
);