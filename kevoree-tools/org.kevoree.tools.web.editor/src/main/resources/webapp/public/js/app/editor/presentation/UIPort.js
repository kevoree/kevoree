define(
    [
        'util/AlertPopupHelper',
        'templates/select-channel-list'
    ],

    function (AlertPopupHelper, channelListTemplate) {
        var RADIUS = 12;

        function UIPort (ctrl) {
            this._ctrl = ctrl;

            this._circle = new Kinetic.Circle({
                radius: RADIUS,
                fillLinearGradientStartPoint: [0, 0],
                fillLinearGradientEndPoint: [0, RADIUS*2],
                fillLinearGradientColorStops: [0, '#bc7645', 1, '#8e7361'],
                strokeWidth: 2
            });

            this._text = new Kinetic.Text({
                text: ctrl.getName().substr(0, 5),
                fontSize: 9,
                fontStyle: 'bold',
                fontFamily: 'Helvetica',
                fill: '#FFF',
                align: 'center'
            });

            this._text.move(
                -this._text.getWidth()/2,
                (-this._text.getHeight()/2) + (RADIUS+5)
            );

            this._shape = new Kinetic.Group({
                x: 100,
                y: 100
            });

            this._shape.add(this._circle);
            this._shape.add(this._text);

            //==========================
            // Event handling
            //==========================
            this._shape.on('mousedown touchstart', function () {
                ctrl.p2cMouseDown();
            });

            this._shape.on('mouseup touchend', function () {
                ctrl.p2cMouseUp();
            });
        }

        UIPort.prototype.getPosition = function () {
            var pos = this._shape.getAbsolutePosition(),
                scale = this._shape.getStage().getScale(),
                stagePos = this._shape.getStage().getPosition();

            return {
                x: (pos.x - stagePos.x) / scale.x,
                y: (pos.y - stagePos.y) / scale.y
            };
        }

        UIPort.prototype.getShape = function () {
            return this._shape;
        }

        UIPort.prototype.c2pWireCreationStarted = function (wire) {
            var wiresLayer = this._ctrl.getComponent().getEditor().getUI().getWiresLayer();
            wire.setTargetPoint(this.getPosition());
            wiresLayer.draw();
            this._ctrl.getComponent().getUI().setDraggable(false, true, true);
        }

        UIPort.prototype.c2pWireCreationPossible = function (originPort, channels) {
            // if we endup here, it means that the user is trying to bind directly a port with another,
            // so we need to display a list of channels and ask him to choose one
            $('#select-channel-popup').modal('show');

            var that = this;
            $('#select-channel-list').html(channelListTemplate({channels: channels}));
            $('#selected-channel').off('click');
            $('#selected-channel').on('click', function () {
                var checkedChannel = $('.channel-type-item:checked'),
                    id = checkedChannel.attr('data-channel-id');
                that._ctrl.p2cChannelSelectedForWireCreation(originPort, channels[id]);
                $('#select-channel-popup').modal('hide');
            });
        }

        UIPort.prototype.c2pWireCreationImpossibleNoChannel = function () {
            AlertPopupHelper.setHTML("<p>There is no channel in the current model.<br/>Binding port is not possible.<br/></p>");
            AlertPopupHelper.setType(AlertPopupHelper.WARN);
            AlertPopupHelper.show(5000);
        }

        UIPort.prototype.setDraggable = function (isDraggable, parentsToo, childrenToo) {
            this._ctrl.getComponent().getUI().setDraggable(isDraggable, parentsToo, childrenToo);
        }

        UIPort.prototype.getRadius = function () {
            return RADIUS;
        }

        UIPort.prototype.getHeight = function () {
            return RADIUS*2 + this._text.getHeight();
        }

        return UIPort;
    }
);