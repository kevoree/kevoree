define(
    [
        'templates/attribute'
    ],
    function (attributeTemplate) {

        function UIValue(ctrl) {
            this._ctrl = ctrl;
        }

        UIValue.prototype.getHTML = function () {
            var nodeName = (this._ctrl.getTargetNode()) ? this._ctrl.getTargetNode().getName() : null;

            return attributeTemplate({
                node: nodeName,
                name: this._ctrl.getAttribute().getName(),
                value: this._ctrl.getValue(),
                type: (this._ctrl.getAttribute().getEnum().length > 0) ? 'enum' : 'raw',
                possibleValues: this._ctrl.getAttribute().getEnum(),
                selected: this._ctrl.getAttribute().getEnum().indexOf(this._ctrl.getValue()),
                optional: this._ctrl.getAttribute().getOptional()
            });
        }

        UIValue.prototype.onHTMLAppended = function () {}

        return UIValue;
    }
);