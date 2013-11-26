define(
    [   // dependencies
        'util/ModelHelper',
        'util/AlertPopupHelper',
        'util/Config',
        'templates/lib-tree',
        'bootstrap/modal',
        'jquery',
        'jqueryui/droppable',
        'jqueryui/draggable',
        'jqueryui/effect-highlight',
        'tinysort',
        'touchpunch',
        'hammer'
    ],

    function (ModelHelper, AlertPopupHelper, Config, libTreeTemplate, _bootstrap, $) {
        var NAMESPACE = '.uieditor',
            libTreeFolded = false,
            displayableItems = [],
            displayableSubTrees = [];


        function UIEditor(ctrl, containerID) {
            this._ctrl = ctrl;
            this._id = containerID;
            this._currentWire = null;
            this._modelLayer = new Kinetic.Layer();
            this._wireLayer = new Kinetic.Layer();
            this._scale = 1;
        }

        UIEditor.prototype.create = function (width, height) {
            // init stage
            this._stage = new Kinetic.Stage({
                container: this._id,
                width: width,
                height: height
            });

            // init background layer
            var bgLayer = new Kinetic.Layer();
            var bgImg = new Image();
            bgImg.onload = function() {
                var background = new Kinetic.Image({
                    image: bgImg,
                    width: bgImg.width,
                    height: bgImg.height
                });
                bgLayer.add(background);
                bgLayer.setZIndex(0);
                bgLayer.draw();
            }

            bgImg.src = Config.BACKGROUND_IMG;
            this._stage.add(bgLayer);

            // add model layer to stage (layer for entities)
            this._stage.add(this._modelLayer);

            // add wire layer to stage
            this._stage.add(this._wireLayer);

            //===========================
            // Event handlers
            //===========================
            var that = this;

            this._stage.on('mousemove touchmove', function() {
                that._ctrl.p2cMouseMove(this.getTouchPosition() || this.getPointerPosition());
            });

            this._stage.on('dbltap', function () {
               that._ctrl.p2cDblTap();
            });

            this._stage.on('mouseup touchend', function() {
                that._ctrl.p2cMouseUp(this.getTouchPosition() || this.getPointerPosition());

                if (that._scale > 1) {
                    that._stage.setDraggable(true);
                } else {
                    that._stage.setDraggable(false);
                }

                that._wireLayer.draw();
            });

            this._registerCallbacks();
        }

        UIEditor.prototype._registerCallbacks = function () {
            var that = this;

            // unregister listeners before
            $(window).off(NAMESPACE);
            // refresh editor size on window resizing
            $(window).on('smartresize'+NAMESPACE, function() {
                that._stage.setSize($('#'+that._id).width(), $('#'+that._id).height());
                that._wireLayer.draw();
            });

            $('#toggle-lib-tree').off(NAMESPACE);
            $('#toggle-lib-tree').on('click'+NAMESPACE, function () {
                that._ctrl.p2cToggleLibTree();
            });

            $('#editor').off(NAMESPACE);
            $('#editor').on('dblclick'+NAMESPACE, function () {
                that._ctrl.p2cDblTap();
            });

            // foldable lib-tree
            $('#lib-tree-content .nav-header').off(NAMESPACE);
            $('#lib-tree-content .nav-header').on('click'+NAMESPACE, function() {
                var header = $(this),
                    icon = header.find('.lib-subtree-icon');

                if (icon.hasClass('icon-arrow-right')) {
                    // all items are showed, hide them
                    displayableSubTrees[header.text()] = false;
                    header.siblings().hide('fast');
                    icon.removeClass('icon-arrow-right');
                    icon.addClass('icon-arrow-down');
                } else {
                    // all items are hidden, reveal them
                    displayableSubTrees[header.text()] = true;
                    showLibTreeItems(header, icon);
                }
            });

            // search field
            $('#lib-tree-search').off(NAMESPACE);
            $('#lib-tree-search').on('keyup'+NAMESPACE, function () {
                $('.lib-item').filter(function () {
                    var libItem = $(this),
                        itemName = libItem.text().toLowerCase(),
                        searchVal = $('#lib-tree-search').val().toLowerCase();

                    if (itemName.search(searchVal) == -1) {
                        libItem.hide();
                    } else {
                        if (displayableItems[libItem.attr('data-entity')] && displayableSubTrees[libItem.siblings('.lib-tree-library').text()]) {
                            libItem.show();
                        }
                    }
                });
            });

            // convenient handler for checkbox checking while link are clicked
            // in lib-tree-settings menu
            $('[id^=lib-tree-settings-filter-]').off(NAMESPACE);
            $('[id^=lib-tree-settings-filter-]').on('click'+NAMESPACE, function () {
                var cb = $(this).children('.checkbox').first();
                cb.prop('checked', !cb.prop('checked'));
                cb.trigger('click');
                return false;
            });

            // filtering libtree items according to their types
            $('[id^=lib-tree-settings-filter-] .checkbox').off(NAMESPACE);
            $('[id^=lib-tree-settings-filter-] .checkbox').on('click'+NAMESPACE, function () {
                var isChecked = !$(this).prop('checked');
                var entity = $(this).val();
                if (isChecked) {
                    // show 'type'
                    displayableItems[entity] = true;
                    $('.lib-tree-library').each(function () {
                        var lib = $(this);
                        lib.siblings('.lib-item[data-entity='+entity+']').each(function () {
                            if (displayableSubTrees[lib.text()]) {
                                $(this).show('fast');
                            }
                        });
                    });
                } else {
                    // hide 'type'
                    displayableItems[entity] = false;
                    $('.lib-item[data-entity='+entity+']').each(function () {
                        $(this).hide('fast');
                    });
                }
            });

            $('.lib-item').off(NAMESPACE);
            $('.lib-item').on('mouseenter'+NAMESPACE, function () {
                    // hover in callback
                    $(this).find('.lib-item-count').stop(true, true).delay(600).fadeOut();
                    $(this).find('.lib-item-name').css('overflow', 'visible');
            });
            $('.lib-item').on('mouseleave'+NAMESPACE, function () {
                // hover out callback
                $(this).find('.lib-item-count').stop(true, true).delay(600).fadeIn();
                $(this).find('.lib-item-name').css('overflow', 'hidden');
            });

            $('#lib-tree-settings-toggle-fold').off(NAMESPACE);
            $('#lib-tree-settings-toggle-fold').on('click'+NAMESPACE, function () {
                that._ctrl.p2cFoldAllLibTree();
            });

            $('.lib-item').hammer().off('tap');
            $('.lib-item').hammer().on('tap', function () {
                $('.lib-item').removeClass('selected');
                $(this).addClass('selected');
            });

            $('.lib-item').hammer().off('doubletap');
            $('.lib-item').hammer().on('doubletap', function () {
                that._addEntity($(this));
            });

            // draggable item in lib-tree
            $('.lib-item').draggable({
                revert: 'invalid',
                helper: function() {
                    // the div dragged is a clone of the selected
                    // div for the drag without the badge if it exists
                    var clone = $(this).clone();
                    clone.children('.lib-item-count').remove();
                    clone.addClass('dragged');
                    return clone;
                },
                cursor: 'move',
                cursorAt: {
                    top: -5, // offset mouse cursor over the dragged item
                    right: -5 // dragged item will be place to the left of cursor (ergo++ on mobile devices)
                }
            });

            // drop behavior on #editor
            $('#editor').droppable({
                drop: function(event, ui) {
                    var canvas = $('#editor').offset(),
                        scale = that._stage.getScale(),
                        position = {
                            x: (event.pageX - canvas.left) / scale.x,
                            y: (event.pageY - canvas.top) / scale.y
                        };
                    that._ctrl.p2cEntityDropped(position);
                },
                over: function(event, ui) {
                    var entity = ui.draggable.attr('data-entity');
                    var name = ui.draggable.find('.lib-item-name').text();
                    that._ctrl.p2cEntityDraggedOver(entity, name);
                },
                out: function () {
                    that._ctrl.p2cEntityDraggedOut();
                }
            });

            var tooltipOnComponents = $('#component-tooltip').prop('checked');
            this.enableTooltips(tooltipOnComponents);
        }

        UIEditor.prototype.enableTooltips = function (enabled) {
            if (enabled) {
                // tooltip on ComponentType
                $(".lib-item[data-entity='ComponentType']").tooltip({
                    selector: $(this),
                    placement: 'bottom',
                    title: "You have to drop this element in a Node",
                    trigger: 'hover',
                    delay: {
                        show: 500,
                        hide: 0
                    }
                });
            } else {
                $(".lib-item[data-entity='ComponentType']").tooltip('destroy');
            }
        }

        UIEditor.prototype.enableAlertPopups = function (enabled) {
            AlertPopupHelper.setEnabled(enabled);
        }

        UIEditor.prototype.c2pEntityAdded = function(entity) {
            this.addShape(entity.getShape());
            entity.ready();

            // update instance counter in lib-tree
            var badgeCount = this._ctrl.getEntityCount(entity.getCtrl().getType());
            $('.lib-item-name').each(function () {
                if ($(this).text() == entity.getCtrl().getType()) {
                    if ($(this).siblings('.lib-item-count').size() > 0) {
                        $(this).siblings('.lib-item-count').children('.badge').text(badgeCount);
                    } else if (badgeCount != 0) {
                        $(this).parent().append(
                            "<div class='lib-item-count'>" +
                                "<span class='badge'>"+badgeCount+"</span>"+
                            "</div>"
                        );
                    }
                }
            });
        }

        UIEditor.prototype.c2pDropImpossible = function (entity) {
            $('.lib-item-name').each(function () {
                if ($(this).text() == entity.getCtrl().getType()) {
                    $(this).effect('highlight', {color: '#f00'}, 500);
                    $(this).parent().tooltip('show');
                }
            });
        }

        UIEditor.prototype.c2pZoomIn = function () {
            this._scale = this._scale + 0.1;
            if (this._scale > 1) this._stage.setDraggable(true);
            this._stage.setScale(this._scale);
            this._stage.draw();
        }

        UIEditor.prototype.c2pZoomTo = function (scale) {
            this._scale = scale;
            this._stage.setScale(this._scale);
            if (this._scale > 1) {
                this._stage.setDraggable(true);
            } else {
                this._stage.setDraggable(false);
                this._stage.setPosition(0, 0);
            }
            this._stage.draw();
        }

        UIEditor.prototype.c2pZoomDefault = function () {
            this._scale = 1;
            this._stage.setScale(this._scale);
            this._stage.setDraggable(false);
            this._stage.setPosition(0, 0);
            this._stage.draw();
        }

        UIEditor.prototype.c2pZoomOut = function () {
            this._scale = this._scale - 0.1;
            if (this._scale < 1) {
                this._stage.setPosition(0, 0);
                this._stage.setDraggable(false);
            }
            this._stage.setScale(this._scale);
            this._stage.draw();
        }

        UIEditor.prototype.c2pHideLibTree = function () {
            // hide lib tree
            $('#lib-tree').hide();
            $('#editor-panel').removeClass('span9');

            // resize editor accordingly
            this._stage.setSize($('#'+this._id).width(), $('#'+this._id).height());
        }

        UIEditor.prototype.c2pFoldAllLibTree = function () {
            $('#lib-tree-settings-toggle-fold').text('Unfold all');
            $('#lib-tree-content .nav-header').each(function () {
                var icon = $(this).children().first();
                if (icon.hasClass('icon-arrow-right')) {
                    displayableSubTrees[$(this).text()] = false;
                    $(this).siblings().hide('fast');
                    icon.removeClass('icon-arrow-right');
                    icon.addClass('icon-arrow-down');
                }
            });
            libTreeFolded = true;
        }

        UIEditor.prototype.c2pUnfoldAllLibTree = function () {
            $('#lib-tree-settings-toggle-fold').text('Fold all');
            $('#lib-tree-content .nav-header').each(function () {
                var icon = $(this).children().first();
                if (icon.hasClass('icon-arrow-down')) {
                    displayableSubTrees[$(this).text()] = true;
                    showLibTreeItems($(this), icon);
                }
            });
            libTreeFolded = false;
        }

        UIEditor.prototype.c2pShowLibTree = function () {
            // show lib tree
            $('#lib-tree').show();
            $('#editor-panel').addClass('span9');

            // resize editor accordingly
            this._stage.setSize($('#'+this._id).width(), $('#'+this._id).height());
            this._wireLayer.draw();
        }

        /**
         * Called by the controller of this UI when it has allowed
         * the entity (given in parameter) to be removed.
         * When this method is called, the editor does not contain
         * this entity anymore
         * @param entity the removed entity
         */
        UIEditor.prototype.c2pEntityRemoved = function(entity) {
            var badgeCount = this._ctrl.getEntityCount(entity.getCtrl().getType());
            $('.lib-item-name').each(function () {
                if ($(this).text() == entity.getCtrl().getType()) {
                    if (badgeCount == 0) {
                        $(this).siblings('.lib-item-count').remove();

                    } else {
                        $(this).siblings('.lib-item-count').children('.badge').text(badgeCount);
                    }
                }
            });
        }

        UIEditor.prototype.c2pUpdateWire = function (wire, position) {
            var scale = this._stage.getScale();
            position = {
                x: position.x / scale.x,
                y: position.y / scale.y
            };
            wire.setTargetPoint(position);
            this._wireLayer.draw();
        }

        UIEditor.prototype.getWiresLayer = function () {
            return this._wireLayer;
        }

        UIEditor.prototype.c2pEntityUpdated = function (entity) {
            this._stage.draw();
            this._wireLayer.draw();
        }

        UIEditor.prototype.c2pWireAdded = function (wire) {
            if (this._scale > 1) {
                // disable drag while wire creation is in progress
                this._stage.setDraggable(false);
            }
        }

        UIEditor.prototype.c2pModelUpdated = function () {
            $('.lib-tree-info').hide(); // hide info
            displayableSubTrees = []; // reset old filter
            $('#lib-tree-settings-toggle-fold').text('Fold all'); // reset fold status
            libTreeFolded = false; // reset fold status
            $('#lib-tree-content li, #lib-tree-content ul').remove(); // remove old content

            var libz = ModelHelper.getLibraries(this._ctrl.getModel());
            for (var i in libz) {
                displayableSubTrees[libz[i].name] = true;
                for (var j in libz[i].components) {
                    var tDef = libz[i].components[j];
                    if (displayableItems[tDef.type] == undefined) displayableItems[tDef.type] = true;
                }
            }
            $('#lib-tree-content').html(libTreeTemplate({ libz: libz}));
            $('.nav-list, .lib-item').tsort();

            this._registerCallbacks();

            // be sure to hide items if they were disable in lib-tree-settings
            $('.lib-item').each(function () {
                if (!displayableItems[$(this).attr('data-entity')]) {
                    $(this).hide();
                }
            });

            // redraw the whole stage
            this._stage.draw();
        }

        UIEditor.prototype.c2pClear = function () {
            $('#lib-tree-content li, #lib-tree-content ul').remove();
            $('.lib-tree-info').show(); // show info
        }

        /**
         * Add the given Shape to the
         * model layer in the stage and redraw the layer
         * @param shape
         */
        UIEditor.prototype.addShape = function (shape) {
            this._modelLayer.add(shape);
            this._modelLayer.draw();
        }

        UIEditor.prototype.getStage = function () {
            return this._stage;
        }

        UIEditor.prototype.draw = function () {
            this._stage.draw();
            this._wireLayer.draw();
        }

        UIEditor.prototype._addEntity = function (jqyItem) {
            var type = jqyItem.attr('data-entity');
            var tDef = jqyItem.find('.lib-item-name').text();
            this._ctrl.p2cEntityDraggedOver(type, tDef);
            this._ctrl.p2cEntityDropped({x: 100, y: 100});
        }

        return UIEditor;

        //==========================
        function showLibTreeItems(elem, icon) {
            elem.siblings().each(function () {
                if (displayableItems[$(this).attr('data-entity')] == true && displayableSubTrees[elem.text()] == true) {
                    $(this).show('fast');
                }
            });
            icon.addClass('icon-arrow-right');
            icon.removeClass('icon-arrow-down');
        }
    }
);