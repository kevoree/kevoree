define(
    [
        'util/Pooffs',
        'presentation/property/UIInstanceProps'
    ],
    function (Pooffs, UIInstanceProps) {
        Pooffs.extends(UIComponentProps, UIInstanceProps);

        function UIComponentProps(ui, ctrl) {
            UIInstanceProps.prototype.constructor.call(this, ui, ctrl);
        }

        return UIComponentProps;
    }
);