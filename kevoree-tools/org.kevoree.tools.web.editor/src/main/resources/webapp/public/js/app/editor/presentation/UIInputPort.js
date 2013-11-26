define(
    [
        'presentation/UIPort',
        'util/Pooffs'
    ],

    function (UIPort, Pooffs) {
        var STROKE = '#ECCA40';

        UIInputPort.NAME = 'input_port';

        Pooffs.extends(UIInputPort, UIPort);

        function UIInputPort (ctrl) {
            UIPort.prototype.constructor.call(this, ctrl);

            this._circle.setStroke(STROKE);
            this._shape.setName(UIInputPort.NAME);
        }

        return UIInputPort;
    }
);