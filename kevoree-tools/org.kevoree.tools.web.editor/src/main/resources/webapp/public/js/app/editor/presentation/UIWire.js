define(
    [
        'util/Pooffs',
        'abstraction/KInputPort',
        'abstraction/KOutputPort',
        'presentation/UIEntity',
        'presentation/property/UIWireProps'
    ],

    function(Pooffs, KInputPort, KOutputPort, UIEntity, UIWireProps) {
        Pooffs.extends(UIWire, UIEntity);

        // GLOBAL CONSTANTS
        var DEFAULT_COLOR = '#5aa564',
            OUTPUT_WIRE = '#C60808',
            INPUT_WIRE = '#ECCA40';

        function UIWire(ctrl, layer) {
            // UIEntity.super(ctrl)
            UIEntity.prototype.constructor.call(this, ctrl);

            this._origin = ctrl.getOrigin().getUI();
            this._target = null;

            var color = (function () {
                switch (ctrl.getOrigin().getEntityType()) {
                    case KOutputPort.ENTITY_TYPE:
                        return OUTPUT_WIRE;

                    case KInputPort.ENTITY_TYPE:
                        return INPUT_WIRE;

                    default:
                        return DEFAULT_COLOR;
                }
            })();

            var that = this;
            this._shape = new Kinetic.Shape({
                stroke: color,
                strokeWidth: 5,
                lineCap: 'round',
                lineJoin: 'round',
                opacity: 0.6,
                drawFunc: function(canvas) {
                    drawLine.call(this, canvas);
                },
                drawHitFunc: function(canvas) {
                    if (that._handlersEnabled) {
                        drawLine.call(this, canvas);
                    }
                }
            });

            function drawLine(canvas) {
                var pts = getPoints(that);
                var context = canvas.getContext();
                context.beginPath();
                context.moveTo(pts.origin.x, pts.origin.y);
                context.quadraticCurveTo(pts.middle.x, pts.middle.y, pts.target.x, pts.target.y);
                canvas.fillStroke(this);
                canvas.fill(this);
                canvas.stroke(this);
                context.closePath();
            }

            layer.add(this._shape);

            // ================
            // Event handlers
            // ================
            this._shape.on('mouseenter', function () {
                this.setStrokeWidth(8);
                this.getLayer().draw();
            });

            this._shape.on('mouseout', function () {
                this.setStrokeWidth(5);
                this.getLayer().draw();
            });

            // ================
            // Properties popup
            // ================
            var props = new UIWireProps(ctrl);
            this._shape.on('dblclick dbltap', function(e) {
                // prevent children from getting the event too
                e.cancelBubble = true;
                // display the properties popup
                props.show();
            });
        }

        UIWire.prototype.setOrigin = function(entityUI) {
            this._origin = entityUI;
        }

        UIWire.prototype.setTarget = function(entityUI) {
            this._target = entityUI;
            this._handlersEnabled = true;
        }

        UIWire.prototype.draw = function() {
            this._shape.draw();
        }

        UIWire.prototype._computeMiddlePoint = function(origin, target) {
            var middleX, middleY;

            if (origin.x > target.x) middleX = target.x + (origin.x - target.x)/2;
            else middleX = origin.x + (target.x - origin.x)/2;

            middleY = ((origin.y >= target.y) ? origin.y : target.y) + 30;

            return { x: middleX, y: middleY };
        }

        UIWire.prototype.remove = function () {
            if (this._shape) {
                var layer = this._shape.getLayer();
                this._shape.destroy();
                layer.draw();
            }
        }

        UIWire.prototype.setTargetPoint = function (point) {
            this._target = {
                getPosition: function () {
                    return point;
                }
            };
        }

        UIWire.prototype.setColor = function (color) {
            this._shape.setAttrs({stroke: color});
        }

        function getPoints(wire) {
            var origin = (wire._origin) ? wire._origin.getPosition() : {x: 0, y: 0},
                target = (wire._target) ? wire._target.getPosition(origin) : {x: 0, y: 0};

            return {
                origin: origin,
                target: target,
                middle: wire._computeMiddlePoint(origin, target)
            };
        }

        return UIWire;
    }
);