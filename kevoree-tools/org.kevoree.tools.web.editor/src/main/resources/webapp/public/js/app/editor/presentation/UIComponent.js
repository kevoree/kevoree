define(
    [
        'presentation/UINestableEntity',
        'presentation/property/UIComponentProps',
        'util/Pooffs'
    ],

    function(UINestableEntity, UIComponentProps, Pooffs) {
            var STROKE = 2;

        UIComponent.PORT_PADDING = 10;

        Pooffs.extends(UIComponent, UINestableEntity);

        function UIComponent(ctrl) {
            UINestableEntity.prototype.constructor.call(this, ctrl);

            this._headerName.setText(
                ctrl.getName()
                + "\n"
                + ctrl.getType()
            );
            this._headerName.setPadding(8);

            this._rect.setAttrs({
                fill: 'black',
                opacity: 0.9,
                cornerRadius: 10
            });

            this._border.setAttrs({
                strokeWidth: STROKE
            });

            var inputs = ctrl.getInputs();
            for (var i=0; i < inputs.length; i++) {
                this._shape.add(inputs[i].getUI().getShape());
            }

            var outputs = ctrl.getOutputs();
            for (var i=0; i < outputs.length; i++) {
                this._shape.add(outputs[i].getUI().getShape());
            }

            this._computedWidth = computeWidth(this._ctrl.getInputs(), this._ctrl.getOutputs(), this._rect.getWidth())
            this._computedHeight = computeHeight(this._ctrl.getInputs(), this._ctrl.getOutputs(), this._rect.getHeight());

            var props = new UIComponentProps(ctrl);
            this._shape.on('dblclick dbltap', function(e) {
                // prevent children from getting the event too
                e.cancelBubble = true;
                // display the properties popup
                props.show();
            });
        }

        UIComponent.prototype._drawHeader = function () {
            this._headerName.setText(
                this._ctrl.getName() +
                "\n" +
                this._ctrl.getType());
        }

        // Override UINestableEntity._draw()
        UIComponent.prototype._draw = function () {
            if (this._ctrl.getParent()) {
                var parent = this._ctrl.getParent().getUI();
                var offset = parent.getChildOffset(this);
                this._shape.setOffset(-offset.x, -offset.y);

            } else {
                this._rect.setWidth(this._computedWidth);
            }

            this._rect.setHeight(this._computedHeight);

            var inputs = this._ctrl.getInputs();
            var dividedHeight = (inputs.length == 0) ? 0 : (this._computedHeight / inputs.length+1);
            for (var i=0; i < inputs.length; i++) {
                var port = inputs[i].getUI(),
                    portShape = port.getShape(),
                    y_off = dividedHeight*(i+1);
                portShape.setPosition(port.getRadius() + 10, y_off - port.getHeight()/2 - 12);
            }

            var outputs = this._ctrl.getOutputs();
            var dividedHeight = (outputs.length == 0) ? 0 : (this._computedHeight / outputs.length+1);
            for (var i=0; i < outputs.length; i++) {
                var port = outputs[i].getUI(),
                    portShape = port.getShape(),
                    y_off = dividedHeight*(i+1);
                portShape.setPosition(this._rect.getWidth() - port.getRadius() - 10, y_off - port.getHeight()/2 - 12);
            }

            this._headerName.setOffset(
                -(this.getWidth()/2 - this._headerName.getWidth()/2),
                -(this.getHeight()/2 - this._headerName.getHeight()/2));
        }

        // Override UINestableEntity.c2pMouseOut()
        UIComponent.prototype.c2pMouseOut = function () {
            document.body.style.cursor = 'default';
            this._border.setAttrs({
                strokeWidth: STROKE,
                stroke: 'white'
            });
            this._shape.getLayer().draw();
        }

        UIComponent.prototype.getHeight = function () {
            return this._rect.getHeight();
        }

        // private method
        function computeHeight(inputs, outputs, currentHeight) {
            var ret = currentHeight;

            if (inputs.length > 0 || outputs.length > 0) {
                var defaultPortHeight = 0;
                if (inputs.length > 0) defaultPortHeight = inputs[0].getUI().getHeight();
                else defaultPortHeight = outputs[0].getUI().getHeight();

                var max = (inputs.length >= outputs.length) ? inputs.length : outputs.length;
                ret = currentHeight + ((defaultPortHeight+10) * (max-1)) + 10;
            }

            return ret;
        }

        // private method
        function computeWidth(inputs, outputs, currentWidth) {
            var ret = currentWidth;

            if (inputs.length > 0)  ret += inputs[0].getUI().getRadius();
            if (outputs.length > 0) ret += outputs[0].getUI().getRadius();

            return ret;
        }

        return UIComponent;
    }
);