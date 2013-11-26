define(
    [
        'control/CEntity',
        'util/Pooffs'
    ],

    function (CEntity, Pooffs) {
        Pooffs.extends(CNestableEntity, CEntity);

        function CNestableEntity(editor, type) {
            // CEntity.super(editor, type)
            CEntity.prototype.constructor.call(this, editor, type);
        }

        CNestableEntity.prototype.p2cMouseOver = function () {}
        CNestableEntity.prototype.p2cMouseOut = function () {}

        CNestableEntity.prototype.p2cDragStart = function () {
            var parent = this.getParent();
            if (parent) {
                parent.removeChild(this);
                parent.getUI().c2pChildDragStarted(this.getUI());
            }
            this._isDragged = true;
            this.getEditor().setDraggedEntity(this);
        }

        CNestableEntity.prototype.p2cDragEnd = function () {
            // check if no one already consume dragged entity
            if (this.getEditor().getDraggedEntity()) {
                // consume entity (= I'll handle it from now on) + dragged entity is me (because p2cDragEnd)
                this.getEditor().consumeDraggedEntity();

                if (!this.getParent()) {
                    if (this.getEditor().hasEntity(this)) {
                    } else {
                        this._ui.c2pRemoveDraggedEntity();
                        this.getEditor().addEntity(this);
                    }
                }

            }
            this._isDragged = false;

            // super.p2cDragEnd();
            CEntity.prototype.p2cDragEnd.call(this);
        }

        return CNestableEntity;
    }
);