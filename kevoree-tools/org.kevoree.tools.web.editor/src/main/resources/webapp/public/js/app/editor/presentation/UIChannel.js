define(
    [
        'util/Pooffs',
        "presentation/UIEntity",
        'presentation/property/UIChannelProps'
    ],

    function(Pooffs, UIEntity, UIChannelProps) {
        // GLOBAL CONSTANTS
        var STROKE = 3,
            RADIUS = 45,
            DEFAULT_STROKE_COLOR = 'white',
            KO_STROKE_COLOR = 'red',
            OK_STROKE_COLOR = 'green';

        Pooffs.extends(UIChannel, UIEntity);

        function UIChannel(ctrl) {
            UIEntity.prototype.constructor.call(this, ctrl);

            this._circle = new Kinetic.Circle({
                radius: RADIUS,
                fill: '#de7c37',
                stroke: DEFAULT_STROKE_COLOR,
                strokeWidth: STROKE,
                shadowColor: 'black',
                shadowBlur: 10,
                shadowOffset: [5, 5],
                shadowOpacity: 0.2,
                opacity: 0.6
            });

            var text = new Kinetic.Text({
                text: ctrl.getName() + '\n' + ctrl.getType(),
                fontSize: 12,
                fontFamily: 'Helvetica',
                fontWeight: 'bold',
                fill: '#FFF',
                align: 'center',
                width: this._circle.getWidth()-10,
                drawFunc: function (canvas) {
                    this.drawFunc(canvas);
                    text.setText(ctrl.getName() + '\n' + ctrl.getType());
                }
            });

            text.move(-text.getWidth()/2, -text.getHeight()/2);

            this._shape = new Kinetic.Group({
                draggable: true
            });

            this._shape.add(this._circle);
            this._shape.add(text);

            //===========================
            // Event handling
            //===========================
            var that = this;
            this._shape.on('mouseover touchmove', function() {
                that._ctrl.p2cMouseOver();
            });

            this._shape.on('mouseout touchend', function() {
                document.body.style.cursor = 'default';
                that._circle.setStrokeWidth(STROKE);
                that._circle.setStroke(DEFAULT_STROKE_COLOR);
                that._circle.getLayer().draw();
            });

            this._shape.on('mouseup touchend', function () {
                that._ctrl.p2cMouseUp();
            });

            this._shape.on('dragmove touchmove', function () {
                that._ctrl.p2cDragMove();
            });

            var props = new UIChannelProps(ctrl);
            this._shape.on('dblclick dbltap', function(e) {
                // prevent children from getting the event too
                e.cancelBubble = true;
                // display the properties popup
                props.show();
            });

            this._shape.on('dragend', function () {
                that._ctrl.p2cDragEnd();
            });
        }

        // Override UIEntity.getPosition()
        UIChannel.prototype.getPosition = function (origin) {
            var pos = this._circle.getAbsolutePosition(),
                scale = this._circle.getStage().getScale(),
                stagePos = this._circle.getStage().getPosition(),
                pos = {
                    x: (pos.x - stagePos.x) / scale.x,
                    y: (pos.y - stagePos.y) / scale.y
                };

            if (origin && (origin.y > pos.y)) {
                // if origin.y is greater than this channel position.y
                // then it is located 'under' graphically so give another plug point
                return {
                    x: pos.x,
                    y: pos.y + RADIUS - 10
                };
            }

            return {
                x: pos.x,
                y: pos.y - RADIUS + 10
            };
        }

        UIChannel.prototype.c2pDropPossible = function () {
            document.body.style.cursor = 'pointer';
            this._circle.setStrokeWidth(STROKE+1);
            this._circle.setStroke(OK_STROKE_COLOR);
            this._circle.getLayer().draw();
        }

        UIChannel.prototype.c2pDropImpossible = function () {
            document.body.style.cursor = 'pointer';
            this._circle.setStrokeWidth(STROKE+1);
            this._circle.setStroke(KO_STROKE_COLOR);
            this._circle.getLayer().draw();
        }

        UIChannel.prototype.c2pPointerOverShape = function () {
            document.body.style.cursor = 'pointer';
            this._circle.setStrokeWidth(STROKE+1);
            this._circle.getLayer().draw();
        }

        UIChannel.prototype.c2pWireCreated = function (wire) {
            wire.getCtrl().getOrigin().getComponent().getUI().setDraggable(true, true, true);
        }

        return UIChannel;
    }
);