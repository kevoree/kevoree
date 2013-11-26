define(
    function () {

        function KValue(attr, targetNode) {
            this._attribute = attr;
            this._value = null;
            this._targetNode = targetNode || null;

            // initiate KValue with default values from model
            var entity          = attr.getDictionary().getEntity(),
                dicType         = entity.getEditor().getModel().findTypeDefinitionsByID(entity.getType()).getDictionaryType(),
                dicValues       = dicType.getDefaultValues(),
                instDic         = attr.getDictionary()._instance,
                valueUpdated    = false;

            if (instDic) {
                // we might have a value saved in model for this attribute value
                var instValues = instDic.getValues();
                for (var i=0; i < instValues.size(); i++) {
                    var val = instValues.get(i);
                    if (targetNode) {
                        if (val.getTargetNode() && val.getTargetNode().getName() == targetNode.getName()) {
                            // this KValue instance is fragment dependant
                            if (val.getAttribute().getName() == attr.getName()) {
                                // we found the perfect match for this KValue in the model
                                this._value = val.getValue();
                                valueUpdated = true;
                                break;
                            }
                        }

                    } else {
                        // this KValue instance is not fragment dependant
                        if (val.getAttribute().getName() == attr.getName()) {
                            // we found the value in model
                            this._value = val.getValue();
                            valueUpdated = true;
                            break;
                        }
                    }
                }
            }

            // if we did not found any value in instance dictionary => use default
            if (!valueUpdated) {
                for (var i=0; i < dicValues.size(); i++) {
                    if (dicValues.get(i).getAttribute().getName() == attr.getName()) {
                        this.setValue(dicValues.get(i).getValue());
                        return;
                    }
                }
            }
        }

        KValue.prototype.getAttribute = function () {
            return this._attribute;
        }

        KValue.prototype.getValue = function () {
            return this._value;
        }

        KValue.prototype.setValue = function (val) {
            this._value = val;
        }

        KValue.prototype.getTargetNode = function () {
            return this._targetNode;
        }

        KValue.prototype.setTargetNode = function (node) {
            this._targetNode = node;
        }

        KValue.prototype.accept = function (visitor) {
            visitor.visitValue(this);
        }

        return KValue;
    }
);