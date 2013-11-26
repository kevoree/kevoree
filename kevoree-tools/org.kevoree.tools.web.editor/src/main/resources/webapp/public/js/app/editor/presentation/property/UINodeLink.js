define(
    [
        'jquery',
        'templates/node-link-tab',
        'templates/node-link-content'
    ],

    function ($, tabTemplate, contentTemplate) {

        var NAMESPACE   = '.node-link',

            TYPE_VAL    = 'node-link-type-',
            TYPE_TAB    = 'node-link-tab-val-',
            RATE_VAL    = 'node-link-rate-',
            ADD_PROP    = 'network-property-add-',
            DEL_PROP    = 'network-property-delete-',
            PROP_LIST   = 'network-property-list-',
            SELECTORS   = [TYPE_VAL, TYPE_TAB, RATE_VAL, ADD_PROP, DEL_PROP, PROP_LIST],

            PROP_ROW    = 'network-property-row',
            PROP_ID     = 'data-net-prop-id';

        function UINodeLink(ctrl) {
            this._ctrl = ctrl;

            this._jqy = [];
            this._selectedIDs = [];
            this._isActive = false;
        }

        UINodeLink.prototype.getTabHTML = function () {
            var jadeParams = {
                id: this._ctrl._id,
                type: this._ctrl.getNetworkType(),
                isActive: this._isActive,
                minRate: 0,
                maxRate: 100,
                rate: this._ctrl.getEstimatedRate(),
                props: getNetworkProperties(this._ctrl)
            };

            return tabTemplate(jadeParams);
        }

        UINodeLink.prototype.setActive = function (isActive) {
            this._isActive = isActive;

            // if already added to DOM, this will update UI
            if (isActive) {
                $('#node-link-root-'+this._ctrl._id).addClass('active');
                $('#node-link-'+this._ctrl._id).addClass('active');
            } else {
                $('#node-link-root-'+this._ctrl._id).removeClass('active');
                $('#node-link-'+this._ctrl._id).removeClass('active');
            }
        }

        UINodeLink.prototype.isActive = function () {
            return this._isActive;
        }

        UINodeLink.prototype.getContentHTML = function () {
            var jadeParams = {
                id: this._ctrl._id,
                type: this._ctrl.getNetworkType(),
                isActive: this._isActive,
                minRate: 0,
                maxRate: 100,
                rate: this._ctrl.getEstimatedRate(),
                props: getNetworkProperties(this._ctrl)
            };

            return contentTemplate(jadeParams);
        }

        UINodeLink.prototype.onHTMLAppended = function () {
            var that = this,
                id = this._ctrl._id;

            this._selectedIDs.length = 0;

            for (var i=0; i < SELECTORS.length; i++) {
                this._jqy[SELECTORS[i]] = $('#'+SELECTORS[i]+id);
            }

            // set network properties selectable with jquery ui
            this._jqy[PROP_LIST].selectable({ filter: '.'+PROP_ROW });

            this._jqy[PROP_LIST].off("selectableselecting"+NAMESPACE);
            this._jqy[PROP_LIST].on("selectableselecting"+NAMESPACE, function() {
                that._jqy[DEL_PROP].removeClass('disabled');
            });

            this._jqy[PROP_LIST].off("selectableunselecting"+NAMESPACE);
            this._jqy[PROP_LIST].on("selectableunselecting"+NAMESPACE, function() {
                var size = that._jqy[PROP_LIST].find('.ui-selected').size();
                if (size == 0) {
                    // if there is no property selected ==> disable del button
                    that._jqy[DEL_PROP].addClass('disabled');
                }
            });

            // change tab name dynamically
            this._jqy[TYPE_VAL].off(NAMESPACE);
            this._jqy[TYPE_VAL].on('keyup'+NAMESPACE, function () {
                var value = $(this).val();
                var matcher = value.match(/\S+/g);
                if (matcher) {
                    that._ctrl.p2cChangeType(matcher[0]);
                }
            });

            // change rate listener
            this._jqy[RATE_VAL].off(NAMESPACE);
            this._jqy[RATE_VAL].on('change'+NAMESPACE, function () {
                that._ctrl.p2cChangeRate($(this).val());
            });

            // add network property button listener
            this._jqy[ADD_PROP].off(NAMESPACE);
            this._jqy[ADD_PROP].on('click'+NAMESPACE, function () {
                that._ctrl.p2cAddNetworkProperty();
            });

            // delete network property button listener
            this._jqy[DEL_PROP].off(NAMESPACE);
            this._jqy[DEL_PROP].on('click'+NAMESPACE, function () {
                var selectedItems = that._jqy[PROP_LIST].find('.ui-selected'),
                    propIDs = [];
                selectedItems.each(function () {
                    propIDs.push(parseInt($(this).attr(PROP_ID)));
                });
                that._ctrl.p2cDeleteNetworkProperties(propIDs);
            });

            // tell network properties that they were added to DOM
            var props = this._ctrl.getNetworkProperties();
            for (var i=0; i < props.length; i++) {
                props[i].getUI().onHTMLAppended();
            }
        }

        UINodeLink.prototype.c2pSetNetworkType = function (type) {
            if (this._jqy[TYPE_TAB]) this._jqy[TYPE_TAB].text(type);
        }

        UINodeLink.prototype.c2pAddNetworkProperty = function (prop) {
            if (this._jqy[PROP_LIST]) {
                this._jqy[PROP_LIST].append(prop.getUI().getHTML());
                prop.getUI().onHTMLAppended();
            }
        }

        UINodeLink.prototype.c2pDeleteNetworkProperty = function (prop) {
            if (this._jqy[PROP_LIST]) this._jqy[PROP_LIST].find('['+PROP_ID+'='+prop._id+']').remove();
        }

        function getNetworkProperties(link) {
            var ret = [],
                props = link.getNetworkProperties();

            for (var i=0; i < props.length; i++) {
                ret.push({
                    HTML: props[i].getUI().getHTML()
                });
            }

            return ret;
        }

        return UINodeLink;
    }
);