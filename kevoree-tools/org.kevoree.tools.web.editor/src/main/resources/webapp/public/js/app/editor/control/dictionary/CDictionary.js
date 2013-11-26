define(
    [
        'abstraction/dictionary/KDictionary',
        'control/AController',
        'presentation/dictionary/UIDictionary',
        'util/Pooffs'
    ],
    function (KDictionary, AController, UIDictionary, Pooffs) {

        Pooffs.extends(CDictionary, AController);
        Pooffs.extends(CDictionary, KDictionary);

        function CDictionary(entity) {
            KDictionary.prototype.constructor.call(this, entity);

            this._ui = new UIDictionary(this);
        }

        CDictionary.prototype.p2cSaveDictionary = function (attrs, fragDepAttrs) {
            for (var i=0; i < this._values.length; i++) {
                var name = this._values[i].getAttribute().getName();

                if (this._values[i].getAttribute().getFragmentDependant()) {
                    var nodeName = this._values[i].getTargetNode().getName();
                    this._values[i].setValue(fragDepAttrs[nodeName][name]);

                } else {
                    this._values[i].setValue(attrs[name]);
                }
            }
            this.getEntity().getEditor().updateModel(this);
        }

        return CDictionary;
    }
);