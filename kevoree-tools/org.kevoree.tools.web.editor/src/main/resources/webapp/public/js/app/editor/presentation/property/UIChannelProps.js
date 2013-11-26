define(
    [
        'presentation/property/UIInstanceProps',
        'util/Pooffs'
    ],
    function (UIInstanceProps, Pooffs) {
        Pooffs.extends(UIChannelProps, UIInstanceProps);

        function UIChannelProps(ctrl) {
            UIInstanceProps.prototype.constructor.call(this, ctrl);
        }

        return UIChannelProps;
    }
);