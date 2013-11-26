define(
    [
        'util/Util'
    ],
    function (Util) {

        var ENUM    = 'enum=';

        function KDictionary(entity) {
            this._entity = entity;
            this._attrs = [];
            this._values = [];

            // create attributes and default values
            var dicType = entity.getEditor().getModel().findTypeDefinitionsByID(entity.getType()).getDictionaryType();

            if (dicType != null && dicType != undefined) {
                var dicAttrs = dicType.getAttributes();

                for (var i=0; i < dicAttrs.size(); i++) {
                    var dicAttr = dicAttrs.get(i),
                        datatype = dicAttr.getDatatype() || '',
                        factory = require('factory/CFactory').getInstance(),
                        attr = factory.newAttribute(this);

                    // set attribute fields
                    attr.setName(dicAttr.getName());
                    // if ENUM set possible values into attribute's enum
                    if (datatype.substr(0, ENUM.length) == ENUM) { // attr.getDatatype() starts with "enum="
                        var str = datatype.substr(ENUM.length, datatype.length);
                        attr.setEnum(str.split(','));
                    }
                    attr.setFragmentDependant(Util.parseBoolean(dicAttr.getFragmentDependant()));
                    attr.setOptional(Util.parseBoolean(dicAttr.getOptional()));

                    // actually add attribute to this dictionary
                    this.addAttribute(attr);

                    if (!attr.getFragmentDependant()) {
                        // add default attribute value to this dictionary if !fragmentDependant
                        this.addValue(factory.newValue(attr));
                    }
                }
            }
        }

        KDictionary.prototype.getEntity = function () {
            return this._entity;
        }

        KDictionary.prototype.getAttributes = function () {
            return this._attrs;
        }

        KDictionary.prototype.getValues = function () {
            return this._values;
        }

        KDictionary.prototype.getValue = function (attrName, nodeName) {
            for (var i=0; i < this._values.length; i++) {
                if (nodeName != undefined && nodeName != null) {
                    if (this._values[i].getAttribute().getName() == attrName
                        && this._values[i].getTargetNode()
                        && this._values[i].getTargetNode().getName() == nodeName) {
                        return this._values[i];
                    }
                } else {
                    if (this._values[i].getAttribute().getName() == attrName) return this._values[i];
                }
            }
            return null;
        }

        KDictionary.prototype.getAttribute = function (name) {
            for (var i=0; i < this._attrs.length; i++) {
                if (this._attrs[i].getName() == name) return this._attrs[i];
            }
            return null;
        }

        KDictionary.prototype.addAttribute = function (attr) {
            this._attrs.push(attr);
        }

        KDictionary.prototype.addValue = function (val) {
            var index = this._values.indexOf(val);
            if (index == -1) {
                // this value has not been added to this._values yet
                if (val.getAttribute().getFragmentDependant()) {
                    // this value is fragment dependant
                    var kVal = this.getValue(val.getAttribute().getName(), val.getTargetNode().getName());
                    if (kVal) {
                        // there is already a value for this attr name & targetNode => update it
                        kVal.setValue(val.getValue());
                    } else {
                        this._values.push(val);
                    }
                } else {
                    // this value is not fragment dependant
                    var kVal = this.getValue(val.getAttribute().getName());
                    if (kVal) {
                        // there is already a value for this attr name => update it
                        kVal.setValue(val.getValue());
                    } else {
                        this._values.push(val);
                    }
                }
                // update model
                this._entity.getEditor().update(this);
            }
        }

        KDictionary.prototype.removeValue = function (val) {
            var index = this._values.indexOf(val);
            if (index != -1) {
                this._values.splice(index, 1);
                // update model
                this._entity.getEditor().update(this);
            }
        }

        KDictionary.prototype.accept = function (visitor) {
            visitor.visitDictionary(this);
        }

        return KDictionary;
    }
);