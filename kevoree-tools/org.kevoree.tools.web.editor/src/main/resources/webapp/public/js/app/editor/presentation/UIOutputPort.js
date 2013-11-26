define(
    [
        'presentation/UIPort',
        'util/Pooffs'
    ],

    function (UIPort, Pooffs) {
        var STROKE = '#C60808';

        UIOutputPort.NAME = 'output_port';

        Pooffs.extends(UIOutputPort, UIPort);

        function UIOutputPort (ctrl) {
            UIPort.prototype.constructor.call(this, ctrl);

            this._circle.setStroke(STROKE);
            this._shape.setName(UIOutputPort.NAME);
        }

        return UIOutputPort;
    }
);