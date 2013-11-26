define(
    [
        'util/ModelHelper',
        'visitor/UpdateModelVisitor',
        'visitor/RemoveModelVisitor',
        'visitor/InstanceModelVisitor',
        'kevoree'
    ],

    function(ModelHelper, UpdateModelVisitor, RemoveModelVisitor, InstanceModelVisitor, Kevoree) {

        function KEditor() {
            this._entities = [];
            this._typeCounter = [];
            this._model = null;
            this._updateVisitor = new UpdateModelVisitor();
            this._removeVisitor = new RemoveModelVisitor();
            this._instanceVisitor = new InstanceModelVisitor();
            this._factory = new Kevoree.org.kevoree.impl.DefaultKevoreeFactory();
            this._lockedModel = false;
            this._libraries = {};
        }

        KEditor.prototype.addEntity = function(entity) {
            this._entities.push(entity);
            console.log("Editor.addEntity ", entity);

            // update typeCounter
            if (!this._typeCounter[entity.getType()]) this._typeCounter[entity.getType()] = 0;
            this._typeCounter[entity.getType()]++;

            // update model
            if (!this._lockedModel) {
                entity.accept(this._updateVisitor);
            }
        }

        KEditor.prototype.removeEntity = function(entity) {
            var index = this._entities.indexOf(entity);
            if (index != -1) {
                this._entities.splice(index, 1);
                console.log("Editor.removedEntity (index: "+index+")", entity);

                // update typeCounter
                this._typeCounter[entity.getType()]--;

                // update model
                if (!this._lockedModel) {
                    entity.accept(this._removeVisitor);
                }
            }
        }

        KEditor.prototype.getEntity = function (name) {
            for (var i=0; i < this._entities.length; i++) {
                if (this._entities[i].getName() == name) return this._entities[i];
                else {
                    if (this._entities[i].getEntity && typeof(this._entities[i].getEntity) == "function") {
                        var entity = this._entities[i].getEntity(name);
                        if (entity != null) return entity;
                    }
                }
            }
            return null;
        }

        KEditor.prototype.addWire = function (wire) {
            wire.accept(this._updateVisitor);
            console.log("Editor.addWire ", wire);
        }

        KEditor.prototype.removeWire = function (wire) {
            wire.accept(this._removeVisitor);
            console.log("Editor.removedWire ", wire);
        }

        KEditor.prototype.addNestableEntity = function (entity) {
            // update typeCounter
            if (!this._typeCounter[entity.getType()]) this._typeCounter[entity.getType()] = 0;
            this._typeCounter[entity.getType()]++;

            // update model
            if (!this._lockedModel) {
                entity.accept(this._updateVisitor);
            }

            console.log("Editor.addNestableEntity ", entity);
        }

        KEditor.prototype.removeNestableEntity = function (entity) {
            // update typeCounter
            this._typeCounter[entity.getType()]--;

            // update model
            if (!this._lockedModel) {
                entity.accept(this._removeVisitor);
            }

            console.log("Editor.removeNestableEntity ", entity);
        }

        KEditor.prototype.clear = function () {
            this.clearInstances();
            this._model = null; // and forget model
        }

        KEditor.prototype.clearInstances = function () {
            var entities = this._entities.slice(0); // clone entities array

            for (var i=0; i < entities.length; i++) {
                entities[i].remove();
            }

            this._entities.length = 0;
            this._typeCounter.length = 0;
        }

        KEditor.prototype.hasEntity = function (entity) {
            for (var i=0; i < this._entities.length; i++) {
                if (this._entities[i].getName() == entity.getName()) return true;
                else {
                    if (this._entities[i].hasChild && typeof(this._entities[i].hasChild) == "function") {
                        if (this._entities[i].hasChild(entity)) return true;
                    }
                }
            }
            return false;
        }

        KEditor.prototype.setModel = function (model) {
            this.clear(); // clear editor before a new model is set
            this._model = model;
            this._updateVisitor.setModel(model);
            this._removeVisitor.setModel(model);

            this._lockedModel = true;
            this._instanceVisitor.visitEditor(this);
            this._lockedModel = false;

            // add model checker
            this._model.addModelTreeListener({
                elementChanged : function (event) {
                    // TODO check model integrity
                }
            });
        }

        KEditor.prototype.mergeModel = function (model) {
            if (this._model != null) {
                // merge needed because there is a model currently set in the editor
                var compare = new Kevoree.org.kevoree.compare.DefaultModelCompare(),
                    diffSeq = compare.merge(model, this._model);
                diffSeq.applyOn(model);
            }

            this.setModel(model);
        }

        KEditor.prototype.updateModel = function (entity) {
            if (!this._lockedModel) entity.accept(this._updateVisitor);
        }

        KEditor.prototype.addToModel = function (entity) {
            if (!this._lockedModel) entity.accept(this._updateVisitor);
        }

        KEditor.prototype.removeFromModel = function (entity) {
            if (!this._lockedModel) entity.accept(this._removeVisitor);
        }

        KEditor.prototype.getModel = function () {
            return this._model;
        }

        /**
         * Returns the current count of instances of the given type
         * @param type specific type of entity
         */
        KEditor.prototype.getEntityCount = function(type) {
            return this._typeCounter[type] || 0;
        }

        KEditor.prototype.addLibraries = function (platform, libraries) {
            this._libraries[platform] = libraries;
        }

        KEditor.prototype.getLibraries = function (platform) {
            return this._libraries[platform];
        }

        return KEditor;
    }
);