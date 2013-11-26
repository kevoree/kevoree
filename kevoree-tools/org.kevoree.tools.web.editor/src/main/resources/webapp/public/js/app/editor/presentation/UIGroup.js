 define(
    [
        'util/Pooffs',
        "presentation/UIEntity",
        'presentation/property/UIGroupProps'
    ],

    function(Pooffs, UIEntity, UIGroupProps) {
        // GLOBAL CONSTANTS
        var STROKE = 4,
            RADIUS = 12;

        Pooffs.extends(UIGroup, UIEntity);

        function UIGroup(ctrl) {
            this._ctrl = ctrl;
            UIEntity.prototype.constructor.call(this, ctrl);

            var circle = new Kinetic.Circle({
                radius: 55,
                fill: 'green',
                stroke: 'black',
                strokeWidth: STROKE,
                shadowColor: 'black',
                shadowBlur: 10,
                shadowOffset: [5],
                shadowOpacity: 0.2,
                opacity: 0.6
            });

            this._plug = new Kinetic.Circle({
                y: (circle.getRadius() / 2) + RADIUS,
                radius: RADIUS,
                fill: '#f1c30f'
            });

            var text = new Kinetic.Text({
                text: ctrl.getName() + '\n' + ctrl.getType(),
                fontSize: 13,
                fontFamily: 'Helvetica',
                fill: '#FFF',
                align: 'center',
                width: circle.getWidth()-10,
                drawFunc: function (canvas) {
                    this.drawFunc(canvas);
                    text.setText(ctrl.getName() + '\n' + ctrl.getType());
                }
            });

            text.move(-text.getWidth()/2, -text.getHeight()/2);

            this._shape = new Kinetic.Group({
                x: 100,
                y: 100,
                draggable: true
            });
            this._shape.add(circle);
            this._shape.add(this._plug);
            this._shape.add(text);

            //===========================
            // Event handling
            //===========================
            var that = this;

            this._shape.on('mouseover touchstart', function() {
                document.body.style.cursor = 'pointer';
                circle.setStrokeWidth(STROKE+1);
                circle.getLayer().draw();
            });

            this._shape.on('mouseout touchend', function() {
                document.body.style.cursor = 'default';
                circle.setStrokeWidth(STROKE);
                circle.getLayer().draw();
            });

            this._shape.on('dragmove', function() {
                that._ctrl.p2cDragMove();
            });

            this._plug.on('mouseover touchmove touchstart', function() {
                that._plug.setRadius(RADIUS+1);
                that._plug.getLayer().draw();
            });

            this._plug.on('mouseout touchend', function() {
                that._plug.setRadius(RADIUS);
                that._plug.getLayer().draw();
            });

            this._shape.on('dragend', function () {
                that._ctrl.p2cDragEnd();
            });

            //===========================
            // Properties popup content
            //===========================
            var props = new UIGroupProps(ctrl);
            this._shape.on('dblclick dbltap', function(e) {
                // prevent children from getting the event too
                e.cancelBubble = true;
                // display the properties popup
                props.show();
            });
        }

        // Override UIEntity.c2pWireCreationStarted(UIWire)
        UIGroup.prototype.c2pWireCreationStarted = function (wire) {
            var wiresLayer = this._ctrl.getEditor().getUI().getWiresLayer();
            wire.setTargetPoint(this.getPosition());
            wiresLayer.draw();
        }


        UIGroup.prototype.ready = function () {
            if (!this._isReady) {
                var that = this;
                var stage = that._shape.getStage();

                // listens to 'mousedown' events to recognize
                // initiation of wire drawing
                this._plug.on('mousedown touchstart', function() {
                    // disable drag events on group during wire creation process
                    that._shape.setDraggable(false);
                    // dispatch user's mousedown event to controller
                    that._ctrl.p2cMouseDown(stage.getTouchPosition() || stage.getPointerPosition());
                });

                that._shape.getStage().on('mouseup touchend', function () {
                    that._ctrl.p2cMouseMove(stage.getTouchPosition() || stage.getPointerPosition());
                });

                this._isReady = true;
            }
        }

        UIGroup.prototype.getPosition = function () {
            var pos = this._plug.getAbsolutePosition(),
                scale = this._plug.getStage().getScale(),
                stagePos = this._plug.getStage().getPosition();
            return {
                x: (pos.x - stagePos.x) / scale.x,
                y: (pos.y - stagePos.y) / scale.y
            };
        }

        return UIGroup;
    }
);