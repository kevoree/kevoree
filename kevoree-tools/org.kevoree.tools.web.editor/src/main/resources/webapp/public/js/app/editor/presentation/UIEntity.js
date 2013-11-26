/*
 * Super class for all UI Entities in Editor
 * Defines the global behavior for this type
 * This should be considered as an Abstract Class
 */
define(
    [    ],

    function() {

        /**
         * You shouldn't use this object directly, it should be considered
         * as an abstract object that facilitate the work in sub-classes
         * @param ctrl
         * @constructor
         */
        function UIEntity(ctrl) {
            this._ctrl = ctrl;
            this._isReady = false;
        }

        UIEntity.prototype.getShape = function() {
            return this._shape;
        }

        /**
         * Returns the absolute position of this entity shape in the stage
         * Sub-classes should override this method and give their own definition
         * of getPosition()
         * @returns {{x: number, y: number}}
         */
        UIEntity.prototype.getPosition = function () {
            return {x: 0, y: 0}; // default position
        }

        UIEntity.prototype.getCtrl = function () {
            return this._ctrl;
        }

        UIEntity.prototype.c2pRemoveEntity = function () {
            try {
                // retrieve this shape's layer
                var layer = this._shape.getLayer();
                // remove shape from layer
                this._shape.remove();
                // redraw the layer
                layer.draw();
            } catch (err) {
                console.warn("UIEntity.c2pRemoveEntity: "+err.message);
            }
        }

        UIEntity.prototype.setDOMItem = function (item) {
            this._DOMItem = item;
        }

        UIEntity.prototype.getDOMItem = function () {
            return this._DOMItem;
        }

        /**
         * Called when this entity's shape has been added to the stage
         * and so you can use KineticJS methods such as :
         * getLayer(), getStage() etc..
         *
         * TODO change that and give stage to each entity UI ?
         */
        UIEntity.prototype.ready = function() {}

        UIEntity.prototype.isReady = function() {
            return this._isReady;
        }

        UIEntity.prototype.c2pWireCreationStarted = function (wire) {}

        UIEntity.prototype.c2pRefreshWires = function (editor) {
            editor.getWiresLayer().draw();
        }

        UIEntity.prototype.c2pPropertiesUpdated = function () {
            this._shape.getLayer().draw();
        }

        /**
         * Do not rely on that, in KineticJS, groups do not have
         * width & height defined, so you MUST override this method
         * @returns {number} the width of this shape
         */
        UIEntity.prototype.getWidth = function () {
            return this._shape.getWidth();
        }

        /**
         * Do width & height defined, so you MUST override this method
         * @returns {number} the height of this shape
         */
        UIEntity.prototype.getHeight = function () {
            return this._shape.getHeight();
        }

        /**
         * Set this entity's shape draggable or not. If the 'parentsToo' given parameter is true,
         * then its parents draggable state will also be changed. Otherwise, just
         * this shape draggable state will be updated. Same for 'childrenToo', that will change
         * the draggable state of this entity's children if set to true
         * @param isDraggable {boolean}
         * @param parentsToo {boolean}
         * @param childrenToo {boolean}
         */
        UIEntity.prototype.setDraggable = function (isDraggable, parentsToo, childrenToo) {
            // set this entity's shape draggable state
            this._shape.setDraggable(isDraggable);

            // check for parents flag
            if (parentsToo && this._ctrl.getParent && this._ctrl.getParent()) {
                this._ctrl.getParent().getUI().setDraggable(isDraggable, true, false);
            }

            // check for children flag
            if (childrenToo && this._ctrl.getChildren) {
                var children = this._ctrl.getChildren();
                for (var i=0; i < children.length; i++) {
                    children[i].getUI().setDraggable(isDraggable, false, true);
                }
            }
        }

        return UIEntity;
    }
);