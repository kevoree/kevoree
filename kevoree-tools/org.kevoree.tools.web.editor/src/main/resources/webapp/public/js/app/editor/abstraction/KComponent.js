define(
    [
        'abstraction/KEntity',
        'util/Pooffs',
        'require'
    ],

    function(KEntity, Pooffs, require) {
        var COUNT = 0;

        KComponent.ENTITY_TYPE = 'ComponentType';

        Pooffs.extends(KComponent, KEntity);

        function KComponent(editor, type) {
            KEntity.prototype.constructor.call(this, editor, type);

            this._parent = null;
            this._name = 'comp' + (COUNT++);
            this._inputs = [];
            this._outputs = [];

            var compTDef = editor.getModel().findTypeDefinitionsByID(this._type),
                inputs = compTDef.getProvided(),
                outputs = compTDef.getRequired(),
                factory = require('factory/CFactory').getInstance();

            for (var i=0; i < inputs.size(); i++) {
                this._inputs.push(factory.newInputPort(inputs.get(i).getName()));
            }

            for (var i=0; i < outputs.size(); i++) {
                this._outputs.push(factory.newOutputPort(outputs.get(i).getName()));
            }

            for (var i=0; i < this._inputs.length; i++) this._inputs[i].setComponent(this);
            for (var i=0; i < this._outputs.length; i++) this._outputs[i].setComponent(this);
        }

        KComponent.prototype.getEntityType = function () {
            return KComponent.ENTITY_TYPE;
        }

        KComponent.prototype.getParent = function () {
            return this._parent;
        }

        KComponent.prototype.setParent = function (node) {
            this._parent = node;
        }

        // Override KEntity.remove()
        KComponent.prototype.remove = function () {
            KEntity.prototype.remove.call(this);

            // tell my parent that I'm gone *sob*
            if (this._parent) {
                this._parent.removeChild(this);
            }
        }

        KComponent.prototype.hasChildren = function () {
            return false;
        }

        KComponent.prototype.getChildren = function () {
            // components do not have children, but they need this method (TODO: refactor, need superclass for Node and component!)
            return [];
        }

        KComponent.prototype.getPort = function (name) {
            for (var i=0; i < this._inputs.length; i++) {
                if (this._inputs[i].getName() == name) return this._inputs[i];
            }
            for (var i=0; i < this._outputs.length; i++) {
                if (this._outputs[i].getName() == name) return this._outputs[i];
            }
            return null;
        }

        KComponent.prototype.getInputs = function () {
            return this._inputs;
        }

        KComponent.prototype.getOutputs = function () {
            return this._outputs;
        }

        KComponent.prototype.accept = function (visitor) {
            visitor.visitComponent(this);
        }

        return KComponent;
    }
);