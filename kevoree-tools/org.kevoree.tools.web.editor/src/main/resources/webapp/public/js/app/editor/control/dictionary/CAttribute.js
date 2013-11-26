define(
    [
        'abstraction/dictionary/KAttribute',
        'control/AController',
        'presentation/dictionary/UIAttribute',
        'util/Pooffs'
    ],
    function (KAttribute, AController, UIAttribute, Pooffs) {

        Pooffs.extends(CAttribute, AController);
        Pooffs.extends(CAttribute, KAttribute);

        function CAttribute(dict) {
            KAttribute.prototype.constructor.call(this, dict);

            this._ui = new UIAttribute(this);
        }

        return CAttribute;
    }
);