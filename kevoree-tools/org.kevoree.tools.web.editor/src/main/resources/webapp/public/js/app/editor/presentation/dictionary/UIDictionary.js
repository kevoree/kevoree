define(
    [
        'templates/dictionary'
    ],

    function (dictionaryTemplate) {
        var NAMESPACE = '.entity-dictionary';

        function UIDictionary(ctrl) {
            this._ctrl = ctrl;
        }

        UIDictionary.prototype.getHTML = function () {
            var values = this._ctrl.getValues();

            return dictionaryTemplate({
                name: this._ctrl.getEntity().getName(),
                attrsHTML: getAttributesHTML(values),
                nodes: getFragDepAttributesHTML(values)
            });
        }

        UIDictionary.prototype.onHTMLAppended = function () {
            var that = this;
            $('#prop-popup-save').off('click'+NAMESPACE);
            $('#prop-popup-save').on('click'+NAMESPACE, function () {
                // when user wants to save properties, tell dictionary to update its content
                var attrs = [],
                    fragDepAttrs = [],
                    dicValues = that._ctrl.getValues();

                for (var i=0; i < dicValues.length; i++) {
                    var name = dicValues[i].getAttribute().getName(),
                        fragDep = dicValues[i].getAttribute().getFragmentDependant();

                    if (fragDep) {
                        var nodeName = dicValues[i].getTargetNode().getName();
                        if (fragDepAttrs[nodeName] == undefined) {
                            fragDepAttrs[nodeName] = [];
                            fragDepAttrs.length += 1;
                        }
                        fragDepAttrs[nodeName][name] = $('#instance-attr-'+nodeName+'-'+name).val();
                        fragDepAttrs[nodeName].length += 1;
                    } else {
                        attrs[name] = $('#instance-attr-'+name).val();
                        attrs.length += 1;
                    }
                }

                that._ctrl.p2cSaveDictionary(attrs, fragDepAttrs);
            });

            for (var i=0; i < this._ctrl.getValues().length; i++) this._ctrl.getValues()[i].getUI().onHTMLAppended();
        }

        function getAttributesHTML(values) {
            var html = '';

            for (var i=0; i < values.length; i++) {
                if (!values[i].getAttribute().getFragmentDependant()) {
                    html += values[i].getUI().getHTML();
                }
            }

            return html;
        }

        function getFragDepAttributesHTML(values) {
            var nodes = [],
                tmpNodes = {};

            for (var i=0; i < values.length; i++) {
                if (values[i].getAttribute().getFragmentDependant()) {
                    var html = tmpNodes[values[i].getTargetNode().getName()] || '';
                    html += values[i].getUI().getHTML();
                    tmpNodes[values[i].getTargetNode().getName()] = html;
                }
            }

            for (var name in tmpNodes) {
                nodes.push({
                    name: name,
                    fragDepAttrsHTML: tmpNodes[name]
                });
            }

            return nodes;
        }

        return UIDictionary;
    }
);