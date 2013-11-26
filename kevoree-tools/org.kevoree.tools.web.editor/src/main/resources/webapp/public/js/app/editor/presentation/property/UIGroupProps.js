define(
    [
        'presentation/property/UIInstanceProps',
        'util/Pooffs'
    ],
    function (UIInstanceProps, Pooffs) {

        Pooffs.extends(UIGroupProps, UIInstanceProps);

        function UIGroupProps(ctrl) {
            UIInstanceProps.prototype.constructor.call(this, ctrl);
        }

        return UIGroupProps;
    }
);