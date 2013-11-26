/**
 * KineticJS JavaScript Framework v4.5.1
 * http://www.kineticjs.com/
 * Copyright 2013, Eric Rowell
 * Licensed under the MIT or GPL Version 2 licenses.
 * Date: May 12 2013
 *
 * Copyright (C) 2011 - 2013 by Eric Rowell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/** 
 * @namespace 
 */
var Kinetic = {}; 
(function() {
    Kinetic.version = '4.5.1';
    
    /** 
     * @namespace 
     */
    Kinetic.Filters = {};

    /**
     * Node constructor. Nodes are entities that can be transformed, layered,
     * and have bound events. The stage, layers, groups, and shapes all extend Node.
     * @constructor
     * @param {Object} config
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Node = function(config) {
        this._nodeInit(config);
    };

    /**
     * Shape constructor.  Shapes are primitive objects such as rectangles,
     *  circles, text, lines, etc.
     * @constructor
     * @augments Kinetic.Node
     * @param {Object} config
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Shape = function(config) {
        this._initShape(config);
    }; 

    /**
     * Container constructor.&nbsp; Containers are used to contain nodes or other containers
     * @constructor
     * @augments Kinetic.Node
     * @param {Object} config
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     * @param {Function} [config.clipFunc] clipping function

     */
    Kinetic.Container = function(config) {
        this._containerInit(config);
    };

    /**
     * Stage constructor.  A stage is used to contain multiple layers
     * @constructor
     * @augments Kinetic.Container
     * @param {Object} config
     * @param {String|DomElement} config.container Container id or DOM element
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     * @param {Function} [config.clipFunc] clipping function

     */
    Kinetic.Stage = function(config) {
        this._initStage(config);
    };

    /**
     * Layer constructor.  Layers are tied to their own canvas element and are used
     * to contain groups or shapes
     * @constructor
     * @augments Kinetic.Container
     * @param {Object} config
     * @param {Boolean} [config.clearBeforeDraw] set this property to false if you don't want
     * to clear the canvas before each layer draw.  The default value is true.
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     * @param {Function} [config.clipFunc] clipping function

     */
    Kinetic.Layer = function(config) {
        this._initLayer(config);
    };

    /**
     * Group constructor.  Groups are used to contain shapes or other groups.
     * @constructor
     * @augments Kinetic.Container
     * @param {Object} config
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     * @param {Function} [config.clipFunc] clipping function

     */
    Kinetic.Group = function(config) {
        this._initGroup(config);
    }; 

    /** 
     * @namespace 
     */
    Kinetic.Global = {
        stages: [],
        idCounter: 0,
        ids: {},
        names: {},
        //shapes hash.  rgb keys and shape values
        shapes: {},

        /**
         * @method isDragging returns whether or not drag and drop
         *  is currently active
         * @methodOf Kinetic.Global
         */
        isDragging: function() {
            var dd = Kinetic.DD;  

            // if DD is not included with the build, then
            // drag and drop is not even possible
            if (!dd) {
                return false;
            } 
            // if DD is included with the build
            else {
                return dd.isDragging;
            }
        },
        /**
        * @method isDragReady returns whether or not a drag and drop operation is ready, but may
        *  not necessarily have started
        * @methodOf Kinetic.Global
        */
        isDragReady: function() {
            var dd = Kinetic.DD;  

            // if DD is not included with the build, then
            // drag and drop is not even possible
            if (!dd) {
                return false;
            } 
            // if DD is included with the build
            else {
                return !!dd.node;
            }
        },
        _addId: function(node, id) {
            if(id !== undefined) {
                this.ids[id] = node;
            }
        },
        _removeId: function(id) {
            if(id !== undefined) {
                delete this.ids[id];
            }
        },
        _addName: function(node, name) {
            if(name !== undefined) {
                if(this.names[name] === undefined) {
                    this.names[name] = [];
                }
                this.names[name].push(node);
            }
        },
        _removeName: function(name, _id) {
            if(name !== undefined) {
                var nodes = this.names[name];
                if(nodes !== undefined) {
                    for(var n = 0; n < nodes.length; n++) {
                        var no = nodes[n];
                        if(no._id === _id) {
                            nodes.splice(n, 1);
                        }
                    }
                    if(nodes.length === 0) {
                        delete this.names[name];
                    }
                }
            }
        }
    };
})();

// Uses Node, AMD or browser globals to create a module.

// If you want something that will work in other stricter CommonJS environments,
// or if you need to create a circular dependency, see commonJsStrict.js

// Defines a module "returnExports" that depends another module called "b".
// Note that the name of the module is implied by the file name. It is best
// if the file name and the exported global have matching names.

// If the 'b' module also uses this type of boilerplate, then
// in the browser, it will create a global .b that is used below.

// If you do not want to support the browser global path, then you
// can remove the `root` use and the passing `this` as the first arg to
// the top function.

// if the module has no dependencies, the above pattern can be simplified to
( function(root, factory) {
    if( typeof exports === 'object') {
        // Node. Does not work with strict CommonJS, but
        // only CommonJS-like enviroments that support module.exports,
        // like Node.
        module.exports = factory();
    }
    else if( typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define(factory);
    }
    else {
        // Browser globals (root is window)
        root.returnExports = factory();
    }
}(this, function() {

    // Just return a value to define the module export.
    // This example returns an object, but the module
    // can return a function as the exported value.
    return Kinetic;
}));

(function() {
    /**
     * Collection constructor.  Collection extends
     *  Array.  This class is used in conjunction with get()
     * @constructor
     */
    Kinetic.Collection = function() {
        var args = [].slice.call(arguments), length = args.length, i = 0;

        this.length = length;
        for(; i < length; i++) {
            this[i] = args[i];
        }
        return this;
    }
    Kinetic.Collection.prototype = new Array();
    /**
     * iterate through node array
     * @name each
     * @methodOf Kinetic.Collection.prototype
     * @param {Function} func
     */
    Kinetic.Collection.prototype.each = function(func) {
        for(var n = 0; n < this.length; n++) {
            func(this[n], n);
        }
    };

    Kinetic.Collection.mapMethods = function(arr) {
        var leng = arr.length,
            n;
            
        for(n = 0; n < leng; n++) {
            // induce scope
            (function(i) {
                var method = arr[i];
                Kinetic.Collection.prototype[method] = function() {
                    var len = this.length,
                        i;
                        
                    args = [].slice.call(arguments);
                    for(i = 0; i < len; i++) {
                        this[i][method].apply(this[i], args);
                    }        
                };
            })(n);
        }
    };
})();


(function() {
    /*
    * Last updated November 2011
    * By Simon Sarris
    * www.simonsarris.com
    * sarris@acm.org
    *
    * Free to use and distribute at will
    * So long as you are nice to people, etc
    */

    /*
    * The usage of this class was inspired by some of the work done by a forked
    * project, KineticJS-Ext by Wappworks, which is based on Simon's Transform
    * class.
    */

    /**
     * Transform constructor
     * @constructor
     */
    Kinetic.Transform = function() {
        this.m = [1, 0, 0, 1, 0, 0];
    }

    Kinetic.Transform.prototype = {
        /**
         * Apply translation
         * @param {Number} x
         * @param {Number} y
         */
        translate: function(x, y) {
            this.m[4] += this.m[0] * x + this.m[2] * y;
            this.m[5] += this.m[1] * x + this.m[3] * y;
        },
        /**
         * Apply scale
         * @param {Number} sx
         * @param {Number} sy
         */
        scale: function(sx, sy) {
            this.m[0] *= sx;
            this.m[1] *= sx;
            this.m[2] *= sy;
            this.m[3] *= sy;
        },
        /**
         * Apply rotation
         * @param {Number} rad  Angle in radians
         */
        rotate: function(rad) {
            var c = Math.cos(rad);
            var s = Math.sin(rad);
            var m11 = this.m[0] * c + this.m[2] * s;
            var m12 = this.m[1] * c + this.m[3] * s;
            var m21 = this.m[0] * -s + this.m[2] * c;
            var m22 = this.m[1] * -s + this.m[3] * c;
            this.m[0] = m11;
            this.m[1] = m12;
            this.m[2] = m21;
            this.m[3] = m22;
        },
        /**
         * Returns the translation
         * @returns {Object} 2D point(x, y)
         */
        getTranslation: function() {
            return {
                x: this.m[4],
                y: this.m[5]
            };
        },
        /**
         * Apply skew 
         * @param {Number} sx
         * @param {Number} sy
         */
        skew: function(sx, sy) {
            var m11 = this.m[0] + this.m[2] * sy;
            var m12 = this.m[1] + this.m[3] * sy;
            var m21 = this.m[2] + this.m[0] * sx;
            var m22 = this.m[3] + this.m[1] * sx;
            this.m[0] = m11;
            this.m[1] = m12;
            this.m[2] = m21;
            this.m[3] = m22;
         },
        /**
         * Transform multiplication
         * @param {Kinetic.Transform} matrix
         */
        multiply: function(matrix) {
            var m11 = this.m[0] * matrix.m[0] + this.m[2] * matrix.m[1];
            var m12 = this.m[1] * matrix.m[0] + this.m[3] * matrix.m[1];

            var m21 = this.m[0] * matrix.m[2] + this.m[2] * matrix.m[3];
            var m22 = this.m[1] * matrix.m[2] + this.m[3] * matrix.m[3];

            var dx = this.m[0] * matrix.m[4] + this.m[2] * matrix.m[5] + this.m[4];
            var dy = this.m[1] * matrix.m[4] + this.m[3] * matrix.m[5] + this.m[5];

            this.m[0] = m11;
            this.m[1] = m12;
            this.m[2] = m21;
            this.m[3] = m22;
            this.m[4] = dx;
            this.m[5] = dy;
        },
        /**
         * Invert the matrix
         */
        invert: function() {
            var d = 1 / (this.m[0] * this.m[3] - this.m[1] * this.m[2]);
            var m0 = this.m[3] * d;
            var m1 = -this.m[1] * d;
            var m2 = -this.m[2] * d;
            var m3 = this.m[0] * d;
            var m4 = d * (this.m[2] * this.m[5] - this.m[3] * this.m[4]);
            var m5 = d * (this.m[1] * this.m[4] - this.m[0] * this.m[5]);
            this.m[0] = m0;
            this.m[1] = m1;
            this.m[2] = m2;
            this.m[3] = m3;
            this.m[4] = m4;
            this.m[5] = m5;
        },
        /**
         * return matrix
         */
        getMatrix: function() {
            return this.m;
        }
    };
})();


(function() {
    // CONSTANTS
    var CANVAS = 'canvas',
        CONTEXT_2D = '2d',
        OBJECT_ARRAY = '[object Array]',
        OBJECT_NUMBER = '[object Number]',
        OBJECT_STRING = '[object String]',
        PI_OVER_DEG180 = Math.PI / 180,
        DEG180_OVER_PI = 180 / Math.PI,
        HASH = '#',
        RGB_PAREN = 'rgb(',
        COLORS = {
            aqua: [0,255,255],
            lime: [0,255,0],
            silver: [192,192,192],
            black: [0,0,0],
            maroon: [128,0,0],
            teal: [0,128,128],
            blue: [0,0,255],
            navy: [0,0,128],
            white: [255,255,255],
            fuchsia: [255,0,255],
            olive:[128,128,0],
            yellow: [255,255,0],
            orange: [255,165,0],
            gray: [128,128,128],
            purple: [128,0,128],
            green: [0,128,0],
            red: [255,0,0],
            pink: [255,192,203],
            cyan: [0,255,255],
            transparent: [255,255,255,0]
        },

        RGB_REGEX = /rgb\((\d{1,3}),(\d{1,3}),(\d{1,3})\)/;

    /** 
     * @namespace 
     */
    Kinetic.Util = {
        /*
         * cherry-picked utilities from underscore.js
         */
        _isElement: function(obj) {
            return !!(obj && obj.nodeType == 1);
        },
        _isFunction: function(obj) {
            return !!(obj && obj.constructor && obj.call && obj.apply);
        },
        _isObject: function(obj) {
            return (!!obj && obj.constructor == Object);
        },
        _isArray: function(obj) {
            return Object.prototype.toString.call(obj) == OBJECT_ARRAY;
        },
        _isNumber: function(obj) {
            return Object.prototype.toString.call(obj) == OBJECT_NUMBER;
        },
        _isString: function(obj) {
            return Object.prototype.toString.call(obj) == OBJECT_STRING;
        },
        /*
         * other utils
         */
        _hasMethods: function(obj) {
            var names = [],
                key;
                
            for(key in obj) {
                if(this._isFunction(obj[key])) {
                    names.push(key);
                }
            }
            return names.length > 0;
        },
        _isInDocument: function(el) {
            while(el = el.parentNode) {
                if(el == document) {
                    return true;
                }
            }
            return false;
        },
        /*
         * The argument can be:
         * - an integer (will be applied to both x and y)
         * - an array of one integer (will be applied to both x and y)
         * - an array of two integers (contains x and y)
         * - an array of four integers (contains x, y, width, and height)
         * - an object with x and y properties
         * - an array of one element which is an array of integers
         * - an array of one element of an object
         */
        _getXY: function(arg) {
            if(this._isNumber(arg)) {
                return {
                    x: arg,
                    y: arg
                };
            }
            else if(this._isArray(arg)) {
                // if arg is an array of one element
                if(arg.length === 1) {
                    var val = arg[0];
                    // if arg is an array of one element which is a number
                    if(this._isNumber(val)) {
                        return {
                            x: val,
                            y: val
                        };
                    }
                    // if arg is an array of one element which is an array
                    else if(this._isArray(val)) {
                        return {
                            x: val[0],
                            y: val[1]
                        };
                    }
                    // if arg is an array of one element which is an object
                    else if(this._isObject(val)) {
                        return val;
                    }
                }
                // if arg is an array of two or more elements
                else if(arg.length >= 2) {
                    return {
                        x: arg[0],
                        y: arg[1]
                    };
                }
            }
            // if arg is an object return the object
            else if(this._isObject(arg)) {
                return arg;
            }

            // default
            return null;
        },
        /*
         * The argument can be:
         * - an integer (will be applied to both width and height)
         * - an array of one integer (will be applied to both width and height)
         * - an array of two integers (contains width and height)
         * - an array of four integers (contains x, y, width, and height)
         * - an object with width and height properties
         * - an array of one element which is an array of integers
         * - an array of one element of an object
         */
        _getSize: function(arg) {
            if(this._isNumber(arg)) {
                return {
                    width: arg,
                    height: arg
                };
            }
            else if(this._isArray(arg)) {
                // if arg is an array of one element
                if(arg.length === 1) {
                    var val = arg[0];
                    // if arg is an array of one element which is a number
                    if(this._isNumber(val)) {
                        return {
                            width: val,
                            height: val
                        };
                    }
                    // if arg is an array of one element which is an array
                    else if(this._isArray(val)) {
                        /*
                         * if arg is an array of one element which is an
                         * array of four elements
                         */
                        if(val.length >= 4) {
                            return {
                                width: val[2],
                                height: val[3]
                            };
                        }
                        /*
                         * if arg is an array of one element which is an
                         * array of two elements
                         */
                        else if(val.length >= 2) {
                            return {
                                width: val[0],
                                height: val[1]
                            };
                        }
                    }
                    // if arg is an array of one element which is an object
                    else if(this._isObject(val)) {
                        return val;
                    }
                }
                // if arg is an array of four elements
                else if(arg.length >= 4) {
                    return {
                        width: arg[2],
                        height: arg[3]
                    };
                }
                // if arg is an array of two elements
                else if(arg.length >= 2) {
                    return {
                        width: arg[0],
                        height: arg[1]
                    };
                }
            }
            // if arg is an object return the object
            else if(this._isObject(arg)) {
                return arg;
            }

            // default
            return null;
        },
        /*
         * arg will be an array of numbers or
         *  an array of point arrays or
         *  an array of point objects
         */
        _getPoints: function(arg) {
            if(arg === undefined) {
                return [];
            }

            // an array of arrays
            if(this._isArray(arg[0])) {
                /*
                 * convert array of arrays into an array
                 * of objects containing x, y
                 */
                var arr = [];
                for(var n = 0; n < arg.length; n++) {
                    arr.push({
                        x: arg[n][0],
                        y: arg[n][1]
                    });
                }

                return arr;
            }
            // an array of objects
            if(this._isObject(arg[0])) {
                return arg;
            }
            // an array of integers
            else {
                /*
                 * convert array of numbers into an array
                 * of objects containing x, y
                 */
                var arr = [];
                for(var n = 0; n < arg.length; n += 2) {
                    arr.push({
                        x: arg[n],
                        y: arg[n + 1]
                    });
                }

                return arr;
            }
        },
        /*
         * arg can be an image object or image data
         */
        _getImage: function(arg, callback) {
            var imageObj, canvas, context, dataUrl;
            
            // if arg is null or undefined
            if(!arg) {
                callback(null);
            }

            // if arg is already an image object
            else if(this._isElement(arg)) {
                callback(arg);
            }

            // if arg is a string, then it's a data url
            else if(this._isString(arg)) {
                imageObj = new Image();
                imageObj.onload = function() {
                    callback(imageObj);
                }
                imageObj.src = arg;
            }

            //if arg is an object that contains the data property, it's an image object
            else if(arg.data) {
                canvas = document.createElement(CANVAS);
                canvas.width = arg.width;
                canvas.height = arg.height;
                context = canvas.getContext(CONTEXT_2D);
                context.putImageData(arg, 0, 0);
                dataUrl = canvas.toDataURL();
                imageObj = new Image();
                imageObj.onload = function() {
                    callback(imageObj);
                }
                imageObj.src = dataUrl;
            }
            else {
                callback(null);
            }
        },
        _rgbToHex: function(r, g, b) {
            return ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
        },
        _hexToRgb: function(hex) {
            var bigint = parseInt(hex, 16);
            return {
                r: (bigint >> 16) & 255,
                g: (bigint >> 8) & 255,
                b: bigint & 255
            };
        },
        getRandomColor: function() {
            var randColor = (Math.random() * 0xFFFFFF << 0).toString(16);
            while (randColor.length < 6) {
              randColor = '0' + randColor;
            }
            return randColor;
        },
        getRGB: function(color) {
          var rgb;
          // color string
          if (color in COLORS) {
            rgb = COLORS[color];
            return {
              r: rgb[0],
              g: rgb[1],
              b: rgb[2]
            };
          }
          // hex
          else if (color[0] === HASH) {
            return this._hexToRgb(color.substring(1));
          }
          // rgb string
          else if (color.substr(0, 4) === RGB_PAREN) {
            rgb = RGB_REGEX.exec(color.replace(/ /g,'')); 
            return {
                r: parseInt(rgb[1]),
                g: parseInt(rgb[2]),
                b: parseInt(rgb[3])
            };
          }
          // default
          else {
            return {
                r: 0,
                g: 0,
                b: 0
            };
          }
        },
        // o1 takes precedence over o2
        _merge: function(o1, o2) {
            var retObj = this._clone(o2);
            for(var key in o1) {
                if(this._isObject(o1[key])) {
                    retObj[key] = this._merge(o1[key], retObj[key]);
                }
                else {
                    retObj[key] = o1[key];
                }
            }
            return retObj;
        },
        // deep clone
        _clone: function(obj) {
            var retObj = {};
            for(var key in obj) {
                if(this._isObject(obj[key])) {
                    retObj[key] = this._clone(obj[key]);
                }
                else {
                    retObj[key] = obj[key];
                }
            }
            return retObj;
        },
        _degToRad: function(deg) {
            return deg * PI_OVER_DEG180;
        },
        _radToDeg: function(rad) {
            return rad * DEG180_OVER_PI;
        },
        _capitalize: function(str) {
            return str.charAt(0).toUpperCase() + str.slice(1);
        },
        warn: function(str) {
            /*
             * IE9 on Windows7 64bit will throw a JS error
             * if we don't use window.console in the conditional
             */
            if(window.console && console.warn) {
                console.warn('Kinetic warning: ' + str);
            }
        },
        extend: function(c1, c2) {
            for(var key in c2.prototype) {
                if(!( key in c1.prototype)) {
                    c1.prototype[key] = c2.prototype[key];
                }
            }
        },
        /**
         * @method addMethods adds methods to a constructor prototype
         * @methodOf Kinetic.Util
         * @param {Function} constructor
         * @param {Object} methods
         */
        addMethods: function(constructor, methods) {
          var key;

          for (key in methods) {
            constructor.prototype[key] = methods[key];
          }
        },
    };
})();

(function() {    
    // calculate pixel ratio
    var canvas = document.createElement('canvas'), 
        context = canvas.getContext('2d'), 
        devicePixelRatio = window.devicePixelRatio || 1, 
        backingStoreRatio = context.webkitBackingStorePixelRatio 
            || context.mozBackingStorePixelRatio 
            || context.msBackingStorePixelRatio 
            || context.oBackingStorePixelRatio 
            || context.backingStorePixelRatio || 1, 
        _pixelRatio = devicePixelRatio / backingStoreRatio;
        
    /**
     * Canvas Renderer constructor
     * @constructor
     * @param {Number} width
     * @param {Number} height
     */
    Kinetic.Canvas = function(config) {
        this.init(config);
    };

    Kinetic.Canvas.prototype = {
        init: function(config) {
            var config = config || {},
                width = config.width || 0,
                height = config.height || 0,
                pixelRatio = config.pixelRatio || _pixelRatio,
                contextType = config.contextType || '2d'; 

            this.pixelRatio = pixelRatio;
            this.element = document.createElement('canvas');
            this.element.style.padding = 0;
            this.element.style.margin = 0;
            this.element.style.border = 0;
            this.element.style.background = 'transparent';
            this.context = this.element.getContext(contextType);
            this.setSize(width, height);   
        },        
        /**
         * get canvas element
         * @name getElement
         * @methodOf Kinetic.Canvas.prototype
         */
        getElement: function() {
            return this.element;
        },
        /**
         * get canvas context
         * @name getContext
         * @methodOf Kinetic.Canvas.prototype
         */
        getContext: function() {
            return this.context;
        },
        /**
         * set width
         * @name setWidth
         * @methodOf Kinetic.Canvas.prototype
         * @param {Number} width
         */
        setWidth: function(width) {
            // take into account pixel ratio
            this.width = this.element.width = width * this.pixelRatio;
            this.element.style.width = width + 'px';
        },
        /**
         * set height
         * @name setHeight
         * @methodOf Kinetic.Canvas.prototype
         * @param {Number} height
         */
        setHeight: function(height) {
            // take into account pixel ratio
            this.height = this.element.height = height * this.pixelRatio;
            this.element.style.height = height + 'px';
        },
        /**
         * get width
         * @name getWidth
         * @methodOf Kinetic.Canvas.prototype
         */
        getWidth: function() {
            return this.width;
        },
        /**
         * get height
         * @name getHeight
         * @methodOf Kinetic.Canvas.prototype
         */
        getHeight: function() {
            return this.height;
        },
        /**
         * set size
         * @name setSize
         * @methodOf Kinetic.Canvas.prototype
         * @param {Number} width
         * @param {Number} height
         */
        setSize: function(width, height) {
            this.setWidth(width);
            this.setHeight(height);
        }
    };

    /**
     * Canvas 2D Renderer constructor
     * @constructor
     * @param {Number} width
     * @param {Number} height
     */
    Kinetic.Canvas2D = function(config) {
        Kinetic.Canvas.call(this, config);
    };

    Kinetic.Canvas2D.prototype = {
        /**
         * clear canvas
         * @name clear
         * @methodOf Kinetic.Canvas.prototype
         */
        clear: function() {
            var context = this.getContext();
            var el = this.getElement();
            context.clearRect(0, 0, this.getWidth(), this.getHeight());
        },
        /**
         * to data url
         * @name toDataURL
         * @methodOf Kinetic.Canvas2D.prototype
         * @param {String} mimeType
         * @param {Number} quality between 0 and 1 for jpg mime types
         */
        toDataURL: function(mimeType, quality) {
            try {
                // If this call fails (due to browser bug, like in Firefox 3.6),
                // then revert to previous no-parameter image/png behavior
                return this.element.toDataURL(mimeType, quality);
            }
            catch(e) {
                try {
                    return this.element.toDataURL();
                }
                catch(e) {
                    Kinetic.Util.warn('Unable to get data URL. ' + e.message)
                    return '';
                }
            }
        },
        /**
         * fill shape
         * @name fill
         * @methodOf Kinetic.Canvas2D.prototype
         * @param {Kinetic.Shape} shape
         */
        fill: function(shape) {
            if(shape.getFillEnabled()) {
                this._fill(shape);
            }
        },
        /**
         * stroke shape
         * @name stroke
         * @methodOf Kinetic.Canvas2D.prototype
         * @param {Kinetic.Shape} shape
         */
        stroke: function(shape) {
            if(shape.getStrokeEnabled()) {
                this._stroke(shape);
            }
        },
        /**
         * fill, stroke, and apply shadows
         *  will only be applied to either the fill or stroke.&nbsp; Fill
         *  is given priority over stroke.
         * @name fillStroke
         * @methodOf Kinetic.Canvas2D.prototype
         * @param {Kinetic.Shape} shape
         */
        fillStroke: function(shape) {
            var fillEnabled = shape.getFillEnabled();
            if(fillEnabled) {
                this._fill(shape);
            }

            if(shape.getStrokeEnabled()) {
                this._stroke(shape, shape.hasShadow() && shape.hasFill() && fillEnabled);
            }
        },
        /**
         * apply shadow
         * @name applyShadow
         * @methodOf Kinetic.Canvas2D.prototype
         * @param {Kinetic.Shape} shape
         * @param {Function} drawFunc
         */
        applyShadow: function(shape, drawFunc) {
            var context = this.context;
            context.save();
            this._applyShadow(shape);
            drawFunc();
            context.restore();
            drawFunc();
        },
        _applyLineCap: function(shape) {
            var lineCap = shape.getLineCap();
            if(lineCap) {
                this.context.lineCap = lineCap;
            }
        },
        _applyOpacity: function(shape) {
            var absOpacity = shape.getAbsoluteOpacity();
            if(absOpacity !== 1) {
                this.context.globalAlpha = absOpacity;
            }
        },
        _applyLineJoin: function(shape) {
            var lineJoin = shape.getLineJoin();
            if(lineJoin) {
                this.context.lineJoin = lineJoin;
            }
        },
        _applyAncestorTransforms: function(node) {
            var context = this.context,
                t, m;

            node._eachAncestorReverse(function(no) {
                t = no.getTransform(true); 
                m = t.getMatrix();
                context.transform(m[0], m[1], m[2], m[3], m[4], m[5]);
            }, true);
        },
        _clip: function(container) {
            var context = this.getContext(); 
            context.save();
            this._applyAncestorTransforms(container);
            context.beginPath(); 
            container.getClipFunc()(this);
            context.clip();
            context.setTransform(1, 0, 0, 1, 0, 0);
        }
    };

    Kinetic.Util.extend(Kinetic.Canvas2D, Kinetic.Canvas);

    Kinetic.SceneCanvas = function(config) {
        Kinetic.Canvas2D.call(this, config);
    };

    Kinetic.SceneCanvas.prototype = {
        setWidth: function(width) {  
            var pixelRatio = this.pixelRatio;           
            Kinetic.Canvas.prototype.setWidth.call(this, width);
            this.context.scale(pixelRatio, pixelRatio);
        },
        setHeight: function(height) { 
            var pixelRatio = this.pixelRatio; 
            Kinetic.Canvas.prototype.setHeight.call(this, height);
            this.context.scale(pixelRatio, pixelRatio);
        },
        _fillColor: function(shape) {
            var context = this.context, fill = shape.getFill();
            context.fillStyle = fill;
            shape._fillFunc(context);
        },
        _fillPattern: function(shape) {
            var context = this.context, 
                fillPatternImage = shape.getFillPatternImage(), 
                fillPatternX = shape.getFillPatternX(), 
                fillPatternY = shape.getFillPatternY(), 
                fillPatternScale = shape.getFillPatternScale(), 
                fillPatternRotation = shape.getFillPatternRotation(), 
                fillPatternOffset = shape.getFillPatternOffset(), 
                fillPatternRepeat = shape.getFillPatternRepeat();

            if(fillPatternX || fillPatternY) {
                context.translate(fillPatternX || 0, fillPatternY || 0);
            }
            if(fillPatternRotation) {
                context.rotate(fillPatternRotation);
            }
            if(fillPatternScale) {
                context.scale(fillPatternScale.x, fillPatternScale.y);
            }
            if(fillPatternOffset) {
                context.translate(-1 * fillPatternOffset.x, -1 * fillPatternOffset.y);
            }

            context.fillStyle = context.createPattern(fillPatternImage, fillPatternRepeat || 'repeat');
            context.fill();
        },
        _fillLinearGradient: function(shape) {
            var context = this.context, 
                start = shape.getFillLinearGradientStartPoint(), 
                end = shape.getFillLinearGradientEndPoint(), 
                colorStops = shape.getFillLinearGradientColorStops(), 
                grd = context.createLinearGradient(start.x, start.y, end.x, end.y);

            if (colorStops) {
                // build color stops
                for(var n = 0; n < colorStops.length; n += 2) {
                    grd.addColorStop(colorStops[n], colorStops[n + 1]);
                }
                context.fillStyle = grd;
                context.fill();  
            }
        },
        _fillRadialGradient: function(shape) {
            var context = this.context, 
            start = shape.getFillRadialGradientStartPoint(), 
            end = shape.getFillRadialGradientEndPoint(), 
            startRadius = shape.getFillRadialGradientStartRadius(), 
            endRadius = shape.getFillRadialGradientEndRadius(), 
            colorStops = shape.getFillRadialGradientColorStops(), 
            grd = context.createRadialGradient(start.x, start.y, startRadius, end.x, end.y, endRadius);

            // build color stops
            for(var n = 0; n < colorStops.length; n += 2) {
                grd.addColorStop(colorStops[n], colorStops[n + 1]);
            }
            context.fillStyle = grd;
            context.fill();
        },
        _fill: function(shape, skipShadow) {
            var context = this.context, 
                hasColor = shape.getFill(), 
                hasPattern = shape.getFillPatternImage(), 
                hasLinearGradient = shape.getFillLinearGradientColorStops(), 
                hasRadialGradient = shape.getFillRadialGradientColorStops(), 
                fillPriority = shape.getFillPriority();

            context.save();

            if(!skipShadow && shape.hasShadow()) {
                this._applyShadow(shape);
            }

            // priority fills
            if(hasColor && fillPriority === 'color') {
                this._fillColor(shape);
            }
            else if(hasPattern && fillPriority === 'pattern') {
                this._fillPattern(shape);
            }
            else if(hasLinearGradient && fillPriority === 'linear-gradient') {
                this._fillLinearGradient(shape);
            }
            else if(hasRadialGradient && fillPriority === 'radial-gradient') {
                this._fillRadialGradient(shape);
            }
            // now just try and fill with whatever is available
            else if(hasColor) {
                this._fillColor(shape);
            }
            else if(hasPattern) {
                this._fillPattern(shape);
            }
            else if(hasLinearGradient) {
                this._fillLinearGradient(shape);
            }
            else if(hasRadialGradient) {
                this._fillRadialGradient(shape);
            }
            context.restore();

            if(!skipShadow && shape.hasShadow()) {
                this._fill(shape, true);
            }
        },
        _stroke: function(shape, skipShadow) {
            var context = this.context, 
                stroke = shape.getStroke(), 
                strokeWidth = shape.getStrokeWidth(), 
                dashArray = shape.getDashArray();

            if(stroke || strokeWidth) {
                context.save();
                if (!shape.getStrokeScaleEnabled()) {
                  
                    context.setTransform(1, 0, 0, 1, 0, 0);
                }
                this._applyLineCap(shape);
                if(dashArray && shape.getDashArrayEnabled()) {
                    if(context.setLineDash) {
                        context.setLineDash(dashArray);
                    }
                    else if('mozDash' in context) {
                        context.mozDash = dashArray;
                    }
                    else if('webkitLineDash' in context) {
                        context.webkitLineDash = dashArray;
                    }
                }
                if(!skipShadow && shape.hasShadow()) {
                    this._applyShadow(shape);
                }
                context.lineWidth = strokeWidth || 2;
                context.strokeStyle = stroke || 'black';
                shape._strokeFunc(context);
                context.restore();

                if(!skipShadow && shape.hasShadow()) {
                    this._stroke(shape, true);
                }
            }
        },
        _applyShadow: function(shape) {
            var context = this.context;
            if(shape.hasShadow() && shape.getShadowEnabled()) {
                var aa = shape.getAbsoluteOpacity();
                // defaults
                var color = shape.getShadowColor() || 'black';
                var blur = shape.getShadowBlur() || 5;
                var offset = shape.getShadowOffset() || {
                    x: 0,
                    y: 0
                };

                if(shape.getShadowOpacity()) {
                    context.globalAlpha = shape.getShadowOpacity() * aa;
                }
                context.shadowColor = color;
                context.shadowBlur = blur;
                context.shadowOffsetX = offset.x;
                context.shadowOffsetY = offset.y;
            }
        }
    };
    Kinetic.Util.extend(Kinetic.SceneCanvas, Kinetic.Canvas2D);

    Kinetic.HitCanvas = function(config) {
        Kinetic.Canvas2D.call(this, config);
    };

    Kinetic.HitCanvas.prototype = {
        _fill: function(shape) {
            var context = this.context;
            context.save();
            context.fillStyle = '#' + shape.colorKey;
            shape._fillFuncHit(context);
            context.restore();
        },
        _stroke: function(shape) {
            var context = this.context, 
                stroke = shape.getStroke(), 
                strokeWidth = shape.getStrokeWidth();

            if(stroke || strokeWidth) {
                this._applyLineCap(shape);
                context.save();
                context.lineWidth = strokeWidth || 2;
                context.strokeStyle = '#' + shape.colorKey;
                shape._strokeFuncHit(context);
                context.restore();
            }
        }
    };
    Kinetic.Util.extend(Kinetic.HitCanvas, Kinetic.Canvas2D);

})();

(function() {
    // CONSTANTS
    var SPACE = ' ',
        EMPTY_STRING = '',
        DOT = '.',
        GET = 'get',
        SET = 'set',
        SHAPE = 'Shape',
        STAGE = 'Stage',
        X = 'x',
        Y = 'y',
        UPPER_X = 'X',
        UPPER_Y = 'Y',
        KINETIC = 'kinetic',
        BEFORE = 'before',
        CHANGE = 'Change',
        ID = 'id',
        NAME = 'name',
        MOUSEENTER = 'mouseenter',
        MOUSELEAVE = 'mouseleave',
        DEG = 'Deg',
        ON = 'on',
        OFF = 'off',
        BEFORE_DRAW = 'beforeDraw',
        DRAW = 'draw',
        BLACK = 'black',
        RGB = 'RGB',
        R = 'r',
        G = 'g',
        B = 'b',
        UPPER_R = 'R',
        UPPER_G = 'G',
        UPPER_B = 'B',
        HASH = '#';
        
    Kinetic.Util.addMethods(Kinetic.Node, {
        _nodeInit: function(config) {
            this._id = Kinetic.Global.idCounter++;
            this.eventListeners = {};
            this.setAttrs(config);
        },
        /**
         * bind events to the node. KineticJS supports mouseover, mousemove,
         *  mouseout, mouseenter, mouseleave, mousedown, mouseup, click, dblclick, touchstart, touchmove,
         *  touchend, tap, dbltap, dragstart, dragmove, and dragend events. Pass in a string
         *  of events delimmited by a space to bind multiple events at once
         *  such as 'mousedown mouseup mousemove'. Include a namespace to bind an
         *  event by name such as 'click.foobar'.
         * @name on
         * @methodOf Kinetic.Node.prototype
         * @param {String} typesStr e.g. 'click', 'mousedown touchstart', 'mousedown.foo touchstart.foo'
         * @param {Function} handler The handler function is passed an event object
         */
        on: function(typesStr, handler) {
            var types = typesStr.split(SPACE),
                len = types.length,
                n, type, event, parts, baseEvent, name;
            
             /*
             * loop through types and attach event listeners to
             * each one.  eg. 'click mouseover.namespace mouseout'
             * will create three event bindings
             */
            for(n = 0; n < len; n++) {
                type = types[n];
                event = type;
                parts = event.split(DOT);
                baseEvent = parts[0];
                name = parts.length > 1 ? parts[1] : EMPTY_STRING;

                if(!this.eventListeners[baseEvent]) {
                    this.eventListeners[baseEvent] = [];
                }

                this.eventListeners[baseEvent].push({
                    name: name,
                    handler: handler
                });
            }
            return this;
        },
        /**
         * remove event bindings from the node. Pass in a string of
         *  event types delimmited by a space to remove multiple event
         *  bindings at once such as 'mousedown mouseup mousemove'.
         *  include a namespace to remove an event binding by name
         *  such as 'click.foobar'. If you only give a name like '.foobar',
         *  all events in that namespace will be removed.
         * @name off
         * @methodOf Kinetic.Node.prototype
         * @param {String} typesStr e.g. 'click', 'mousedown touchstart', '.foobar'
         */
        off: function(typesStr) {
            var types = typesStr.split(SPACE),
                len = types.length,
                n, type, event, parts, baseEvent;
                
            for(n = 0; n < len; n++) {
                type = types[n];
                event = type;
                parts = event.split(DOT);
                baseEvent = parts[0];

                if(parts.length > 1) {
                    if(baseEvent) {
                        if(this.eventListeners[baseEvent]) {
                            this._off(baseEvent, parts[1]);
                        }
                    }
                    else {
                        for(var type in this.eventListeners) {
                            this._off(type, parts[1]);
                        }
                    }
                }
                else {
                    delete this.eventListeners[baseEvent];
                }
            }
            return this;
        },
        /**
         * remove child from container, but don't destroy it
         * @name remove
         * @methodOf Kinetic.Node.prototype
         */
        remove: function() {
            var parent = this.getParent();
            
            if(parent && parent.children) {
                parent.children.splice(this.index, 1);
                parent._setChildrenIndices();
            }
            delete this.parent;
        },
        /**
         * remove and destroy node
         * @name destroy
         * @methodOf Kinetic.Node.prototype
         */
        destroy: function() {
            var parent = this.getParent(), 
                stage = this.getStage(), 
                dd = Kinetic.DD, 
                go = Kinetic.Global;

            // destroy children
            while(this.children && this.children.length > 0) {
                this.children[0].destroy();
            }

            // remove from ids and names hashes
            go._removeId(this.getId());
            go._removeName(this.getName(), this._id);

            // TODO: stop transitions
 
            this.remove();
        },
        /**
         * get attr
         * @name getAttr
         * @methodOf Kinetic.Node.prototype
         * @param {String} attr  
         */
        getAttr: function(attr) {
            var method = GET + Kinetic.Util._capitalize(attr);
            if(Kinetic.Util._isFunction(this[method])) {
                return this[method]();
            }
            // otherwise get directly
            else {
                return this.attrs[attr];
            }
        },
        /**
         * get attrs
         * @name getAttrs
         * @methodOf Kinetic.Node.prototype
         */
        getAttrs: function() {
            return this.attrs || {};
        },
        /**
         * @name createAttrs
         * @methodOf Kinetic.Node.prototype
         */
        createAttrs: function() {
            if(this.attrs === undefined) {
                this.attrs = {};
            }
        },
        
        /**
         * set attrs
         * @name setAttrs
         * @methodOf Kinetic.Node.prototype
         * @param {Object} config object containing key value pairs
         */
        setAttrs: function(config) {
            var key, method;
            
            if(config) {
                for(key in config) {
                    method = SET + Kinetic.Util._capitalize(key);
                    // use setter if available
                    if(Kinetic.Util._isFunction(this[method])) {
                        this[method](config[key]);
                    }
                    // otherwise set directly
                    else {
                        this.setAttr(key, config[key]);
                    }
                }
            }
        },
        /**
         * determine if node is visible or not.  Node is visible only
         *  if it's visible and all of its ancestors are visible.  If an ancestor
         *  is invisible, this means that the node is also invisible
         * @name getVisible
         * @methodOf Kinetic.Node.prototype
         */
        getVisible: function() {
            var visible = this.attrs.visible, 
                parent = this.getParent();
              
            // default  
            if (visible === undefined) {
                visible = true;  
            }
            
            if(visible && parent && !parent.getVisible()) {
                return false;
            }
            return visible;
        },
        /**
         * determine if node is listening or not.  Node is listening only
         *  if it's listening and all of its ancestors are listening.  If an ancestor
         *  is not listening, this means that the node is also not listening
         * @name getListening
         * @methodOf Kinetic.Node.prototype
         */
        getListening: function() {
            var listening = this.attrs.listening, 
                parent = this.getParent();
                
            // default  
            if (listening === undefined) {
                listening = true;  
            }
            
            if(listening && parent && !parent.getListening()) {
                return false;
            }
            return listening;
        },
        /**
         * show node
         * @name show
         * @methodOf Kinetic.Node.prototype
         */
        show: function() {
            this.setVisible(true);
        },
        /**
         * hide node.  Hidden nodes are no longer detectable
         * @name hide
         * @methodOf Kinetic.Node.prototype
         */
        hide: function() {
            this.setVisible(false);
        },
        /**
         * get zIndex relative to the node's siblings who share the same parent
         * @name getZIndex
         * @methodOf Kinetic.Node.prototype
         */
        getZIndex: function() {
            return this.index || 0;
        },
        /**
         * get absolute z-index which takes into account sibling
         *  and ancestor indices
         * @name getAbsoluteZIndex
         * @methodOf Kinetic.Node.prototype
         */
        getAbsoluteZIndex: function() {
            var level = this.getLevel(),
                stage = this.getStage(),
                that = this,
                index = 0,
                nodes, len, n, child;
                
            function addChildren(children) {
                nodes = [];
                len = children.length;
                for(n = 0; n < len; n++) {
                    child = children[n];
                    index++;

                    if(child.nodeType !== SHAPE) {
                        nodes = nodes.concat(child.getChildren());
                    }

                    if(child._id === that._id) {
                        n = len;
                    }
                }

                if(nodes.length > 0 && nodes[0].getLevel() <= level) {
                    addChildren(nodes);
                }
            }
            if(that.nodeType !== STAGE) {
                addChildren(that.getStage().getChildren());
            }

            return index;
        },
        /**
         * get node level in node tree.  Returns an integer.<br><br>
         *  e.g. Stage level will always be 0.  Layers will always be 1.  Groups and Shapes will always
         *  be >= 2
         * @name getLevel
         * @methodOf Kinetic.Node.prototype
         */
        getLevel: function() {
            var level = 0,
                parent = this.parent;
                
            while(parent) {
                level++;
                parent = parent.parent;
            }
            return level;
        },
        /**
         * set node position relative to parent
         * @name setPosition
         * @methodOf Kinetic.Node.prototype
         * @param {Number} x
         * @param {Number} y
         */
        setPosition: function() {
            var pos = Kinetic.Util._getXY([].slice.call(arguments));
            this.setX(pos.x);
            this.setY(pos.y);
        },
        /**
         * get node position relative to parent
         * @name getPosition
         * @methodOf Kinetic.Node.prototype
         */
        getPosition: function() {
            return {
                x: this.getX(),
                y: this.getY()
            };
        },
        /**
         * get absolute position relative to the top left corner of the stage container div
         * @name getAbsolutePosition
         * @methodOf Kinetic.Node.prototype
         */
        getAbsolutePosition: function() {
            var trans = this.getAbsoluteTransform(),
                o = this.getOffset();
                
            trans.translate(o.x, o.y);
            return trans.getTranslation();
        },
        /**
         * set absolute position
         * @name setAbsolutePosition
         * @methodOf Kinetic.Node.prototype
         * @param {Number} x
         * @param {Number} y
         */
        setAbsolutePosition: function() {
            var pos = Kinetic.Util._getXY([].slice.call(arguments)),
                trans = this._clearTransform(),
                it;
                
            // don't clear translation
            this.attrs.x = trans.x;
            this.attrs.y = trans.y;
            delete trans.x;
            delete trans.y;

            // unravel transform
            it = this.getAbsoluteTransform();

            it.invert();
            it.translate(pos.x, pos.y);
            pos = {
                x: this.attrs.x + it.getTranslation().x,
                y: this.attrs.y + it.getTranslation().y
            };

            this.setPosition(pos.x, pos.y);
            this._setTransform(trans);
        },
        /**
         * move node by an amount relative to its current position
         * @name move
         * @methodOf Kinetic.Node.prototype
         * @param {Number} x
         * @param {Number} y
         */
        move: function() {
            var pos = Kinetic.Util._getXY([].slice.call(arguments)),
                x = this.getX(),
                y = this.getY();

            if(pos.x !== undefined) {
                x += pos.x;
            }

            if(pos.y !== undefined) {
                y += pos.y;
            }

            this.setPosition(x, y);
        },
        _eachAncestorReverse: function(func, includeSelf) {
            var family = [], 
                parent = this.getParent(),
                len, n;

            // build family by traversing ancestors
            if(includeSelf) {
                family.unshift(this);
            }
            while(parent) {
                family.unshift(parent);
                parent = parent.parent;
            }

            len = family.length;
            for(n = 0; n < len; n++) {
                func(family[n]);
            }
        },
        /**
         * rotate node by an amount in radians relative to its current rotation
         * @name rotate
         * @methodOf Kinetic.Node.prototype
         * @param {Number} theta
         */
        rotate: function(theta) {
            this.setRotation(this.getRotation() + theta);
        },
        /**
         * rotate node by an amount in degrees relative to its current rotation
         * @name rotateDeg
         * @methodOf Kinetic.Node.prototype
         * @param {Number} deg
         */
        rotateDeg: function(deg) {
            this.setRotation(this.getRotation() + Kinetic.Util._degToRad(deg));
        },
        /**
         * move node to the top of its siblings
         * @name moveToTop
         * @methodOf Kinetic.Node.prototype
         */
        moveToTop: function() {
            var index = this.index;
            this.parent.children.splice(index, 1);
            this.parent.children.push(this);
            this.parent._setChildrenIndices();
            return true;
        },
        /**
         * move node up
         * @name moveUp
         * @methodOf Kinetic.Node.prototype
         */
        moveUp: function() {
            var index = this.index,
                len = this.parent.getChildren().length;
            if(index < len - 1) {
                this.parent.children.splice(index, 1);
                this.parent.children.splice(index + 1, 0, this);
                this.parent._setChildrenIndices();
                return true;
            }
        },
        /**
         * move node down
         * @name moveDown
         * @methodOf Kinetic.Node.prototype
         */
        moveDown: function() {
            var index = this.index;
            if(index > 0) {
                this.parent.children.splice(index, 1);
                this.parent.children.splice(index - 1, 0, this);
                this.parent._setChildrenIndices();
                return true;
            }
        },
        /**
         * move node to the bottom of its siblings
         * @name moveToBottom
         * @methodOf Kinetic.Node.prototype
         */
        moveToBottom: function() {
            var index = this.index;
            if(index > 0) {
                this.parent.children.splice(index, 1);
                this.parent.children.unshift(this);
                this.parent._setChildrenIndices();
                return true;
            }
        },
        /**
         * set zIndex relative to siblings
         * @name setZIndex
         * @methodOf Kinetic.Node.prototype
         * @param {Integer} zIndex
         */
        setZIndex: function(zIndex) {
            var index = this.index;
            this.parent.children.splice(index, 1);
            this.parent.children.splice(zIndex, 0, this);
            this.parent._setChildrenIndices();
        },
        /**
         * get absolute opacity
         * @name getAbsoluteOpacity
         * @methodOf Kinetic.Node.prototype
         */
        getAbsoluteOpacity: function() {
            var absOpacity = this.getOpacity();
            if(this.getParent()) {
                absOpacity *= this.getParent().getAbsoluteOpacity();
            }
            return absOpacity;
        },
        /**
         * move node to another container
         * @name moveTo
         * @methodOf Kinetic.Node.prototype
         * @param {Container} newContainer
         */
        moveTo: function(newContainer) {
            Kinetic.Node.prototype.remove.call(this);
            newContainer.add(this);
        },
        /**
         * convert Node into an object for serialization.  Returns an object.
         * @name toObject
         * @methodOf Kinetic.Node.prototype
         */
        toObject: function() {
            var type = Kinetic.Util, 
                obj = {}, 
                attrs = this.getAttrs(),
                key, val;

            obj.attrs = {};

            // serialize only attributes that are not function, image, DOM, or objects with methods
            for(key in attrs) {
                val = attrs[key];
                if(!type._isFunction(val) && !type._isElement(val) && !(type._isObject(val) && type._hasMethods(val))) {
                    obj.attrs[key] = val;
                }
            }

            obj.nodeType = this.nodeType;
            obj.shapeType = this.shapeType;

            return obj;
        },
        /**
         * convert Node into a JSON string.  Returns a JSON string.
         * @name toJSON
         * @methodOf Kinetic.Node.prototype
         */
        toJSON: function() {
            return JSON.stringify(this.toObject());
        },
        /**
         * get parent container
         * @name getParent
         * @methodOf Kinetic.Node.prototype
         */
        getParent: function() {
            return this.parent;
        },
        /**
         * get layer ancestor
         * @name getLayer
         * @methodOf Kinetic.Node.prototype
         */
        getLayer: function() {
            return this.getParent().getLayer();
        },
        /**
         * get stage ancestor
         * @name getStage
         * @methodOf Kinetic.Node.prototype
         */
        getStage: function() {
            if(this.getParent()) {
                return this.getParent().getStage();
            }
            else {
                return undefined;
            }
        },
        /**
         * fire event
         * @name fire
         * @methodOf Kinetic.Node.prototype
         * @param {String} eventType event type.  can be a regular event, like click, mouseover, or mouseout, or it can be a custom event, like myCustomEvent
         * @param {EventObject} evt event object
         * @param {Boolean} preventBubble setting the value to false, or leaving it undefined, will result in the event bubbling.  Setting the value to true will result in the event not bubbling.
         */
        fire: function(eventType, evt, preventBubble) {
            // no bubble
            if (preventBubble) {
                this._executeHandlers(eventType, evt || {});
            }
            // bubble
            else {
                this._handleEvent(eventType, evt || {});
            }
        },
        /**
         * get absolute transform of the node which takes into
         *  account its ancestor transforms
         * @name getAbsoluteTransform
         * @methodOf Kinetic.Node.prototype
         */
        getAbsoluteTransform: function() {
            // absolute transform
            var am = new Kinetic.Transform(),
                m;

            this._eachAncestorReverse(function(node) {
                m = node.getTransform();
                am.multiply(m);
            }, true);
            return am;
        },
        _getAndCacheTransform: function() {
            var m = new Kinetic.Transform(), 
                x = this.getX(), 
                y = this.getY(), 
                rotation = this.getRotation(),
                scaleX = this.getScaleX(), 
                scaleY = this.getScaleY(), 
                skewX = this.getSkewX(), 
                skewY = this.getSkewY(), 
                offsetX = this.getOffsetX(), 
                offsetY = this.getOffsetY();
                
            if(x !== 0 || y !== 0) {
                m.translate(x, y);
            }
            if(rotation !== 0) {
                m.rotate(rotation);
            }
            if(skewX !== 0 || skewY !== 0) {
                m.skew(skewX, skewY);
            }
            if(scaleX !== 1 || scaleY !== 1) {
                m.scale(scaleX, scaleY);
            }
            if(offsetX !== 0 || offsetY !== 0) {
                m.translate(-1 * offsetX, -1 * offsetY);
            }
             
            // cache result
            this.cachedTransform = m;
            return m;
        },
        /**
         * get transform of the node
         * @name getTransform
         * @methodOf Kinetic.Node.prototype
         */
        getTransform: function(useCache) {
            var cachedTransform = this.cachedTransform;
            if (useCache && cachedTransform) {
                return cachedTransform;
            }
            else {
                return this._getAndCacheTransform();
            }
        },
        /**
         * clone node.  Returns a new Node instance with identical attributes
         * @name clone
         * @methodOf Kinetic.Node.prototype
         * @param {Object} attrs override attrs
         */
        clone: function(obj) {
            // instantiate new node
            var classType = this.shapeType || this.nodeType,
                node = new Kinetic[classType](this.attrs),
                key, allListeners, len, n, listener;

            // copy over listeners
            for(key in this.eventListeners) {
                allListeners = this.eventListeners[key];
                len = allListeners.length;
                for(n = 0; n < len; n++) {
                    listener = allListeners[n];
                    /*
                     * don't include kinetic namespaced listeners because
                     *  these are generated by the constructors
                     */
                    if(listener.name.indexOf(KINETIC) < 0) {
                        // if listeners array doesn't exist, then create it
                        if(!node.eventListeners[key]) {
                            node.eventListeners[key] = [];
                        }
                        node.eventListeners[key].push(listener);
                    }
                }
            }

            // apply attr overrides
            node.setAttrs(obj);
            return node;
        },
        /**
         * Creates a composite data URL. If MIME type is not
         * specified, then "image/png" will result. For "image/jpeg", specify a quality
         * level as quality (range 0.0 - 1.0)
         * @name toDataURL
         * @methodOf Kinetic.Node.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toDataURL: function(config) {
            var config = config || {},
                mimeType = config.mimeType || null, 
                quality = config.quality || null,
                stage = this.getStage(),
                x = config.x || 0, 
                y = config.y || 0,
                canvas = new Kinetic.SceneCanvas({
                    width: config.width || stage.getWidth(), 
                    height: config.height || stage.getHeight(),
                    pixelRatio: 1
                }),
                context = canvas.getContext();
            
            context.save();

            if(x || y) {
                context.translate(-1 * x, -1 * y);
            }

            this.drawScene(canvas);
            context.restore();

            return canvas.toDataURL(mimeType, quality);
        },
        /**
         * converts node into an image.  Since the toImage
         *  method is asynchronous, a callback is required.  toImage is most commonly used
         *  to cache complex drawings as an image so that they don't have to constantly be redrawn
         * @name toImage
         * @methodOf Kinetic.Node.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toImage: function(config) {
            Kinetic.Util._getImage(this.toDataURL(config), function(img) {
                config.callback(img);
            });
        },
        /**
         * set size
         * @name setSize
         * @methodOf Kinetic.Node.prototype
         * @param {Number} width
         * @param {Number} height
         */
        setSize: function() {
            // set stage dimensions
            var size = Kinetic.Util._getSize(Array.prototype.slice.call(arguments));
            this.setWidth(size.width);
            this.setHeight(size.height);
        },
        /**
         * get size
         * @name getSize
         * @methodOf Kinetic.Node.prototype
         */
        getSize: function() {
            return {
                width: this.getWidth(),
                height: this.getHeight()
            };
        },
        /**
         * get width
         * @name getWidth
         * @methodOf Kinetic.Node.prototype
         */
        getWidth: function() {
            return this.attrs.width || 0;
        },
        /**
         * get height
         * @name getHeight
         * @methodOf Kinetic.Node.prototype
         */
        getHeight: function() {
            return this.attrs.height || 0;
        },
        _get: function(selector) {
            return this.nodeType === selector ? [this] : [];
        },
        _off: function(type, name) {
            var evtListeners = this.eventListeners[type],
                i;
                
            for(i = 0; i < evtListeners.length; i++) {
                if(evtListeners[i].name === name) {
                    evtListeners.splice(i, 1);
                    if(evtListeners.length === 0) {
                        delete this.eventListeners[type];
                        break;
                    }
                    i--;
                }
            }
        },
        _clearTransform: function() {

            var trans = {
                x: this.getX(),
                y: this.getY(),
                rotation: this.getRotation(),
                scaleX: this.getScaleX(),
                scaleY: this.getScaleY(),
                offsetX: this.getOffsetX(),
                offsetY: this.getOffsetY(),
                skewX: this.getSkewX(),
                skewY: this.getSkewY()
            };

            this.attrs.x = 0;
            this.attrs.y = 0;
            this.attrs.rotation = 0;
            this.attrs.scaleX = 1;
            this.attrs.scaleY = 1;
            this.attrs.offsetX = 0;
            this.attrs.offsetY = 0;
            this.attrs.skewX = 0;
            this.attrs.skewY = 0;

            return trans;
        },
        _setTransform: function(trans) {
            var key;
            
            for(key in trans) {
                this.attrs[key] = trans[key];
            }

            this.cachedTransform = null;
        },
        _fireBeforeChangeEvent: function(attr, oldVal, newVal) {
            this._handleEvent(BEFORE + Kinetic.Util._capitalize(attr) + CHANGE, {
                oldVal: oldVal,
                newVal: newVal
            });
        },
        _fireChangeEvent: function(attr, oldVal, newVal) {
            this._handleEvent(attr + CHANGE, {
                oldVal: oldVal,
                newVal: newVal
            });
        },
        /**
         * set id
         * @name setId
         * @methodOf Kinetic.Node.prototype
         * @param {String} id
         */
        setId: function(id) {
            var oldId = this.getId(), 
                stage = this.getStage(), 
                go = Kinetic.Global;
                
            go._removeId(oldId);
            go._addId(this, id);
            this.setAttr(ID, id);
        },
        /**
         * set name
         * @name setName
         * @methodOf Kinetic.Node.prototype
         * @param {String} name
         */
        setName: function(name) {
            var oldName = this.getName(), 
                stage = this.getStage(), 
                go = Kinetic.Global;
                
            go._removeName(oldName, this._id);
            go._addName(this, name);
            this.setAttr(NAME, name);
        },
        /**
         * get node type.  Returns 'Stage', 'Layer', 'Group', or 'Shape'
         * @name getNodeType
         * @methodOf Kinetic.Node.prototype
         */
        getNodeType: function() {
            return this.nodeType;
        },
        setAttr: function(key, val) {
            var oldVal;
            if(val !== undefined) {
                oldVal = this.attrs[key];
                this._fireBeforeChangeEvent(key, oldVal, val);
                this.attrs[key] = val;
                this._fireChangeEvent(key, oldVal, val);
            }
        },
        _handleEvent: function(eventType, evt, compareShape) {
            if(evt && this.nodeType === SHAPE) {
                evt.targetNode = this;
            }
            var stage = this.getStage();
            var el = this.eventListeners;
            var okayToRun = true;

            if(eventType === MOUSEENTER && compareShape && this._id === compareShape._id) {
                okayToRun = false;
            }
            else if(eventType === MOUSELEAVE && compareShape && this._id === compareShape._id) {
                okayToRun = false;
            }

            if(okayToRun) {                
                this._executeHandlers(eventType, evt);

                // simulate event bubbling
                if(evt && !evt.cancelBubble && this.parent) {
                    if(compareShape && compareShape.parent) {
                        this._handleEvent.call(this.parent, eventType, evt, compareShape.parent);
                    }
                    else {
                        this._handleEvent.call(this.parent, eventType, evt);
                    }
                }
            }
        },
        _executeHandlers: function(eventType, evt) {
            var events = this.eventListeners[eventType],
                len, i;
                
            if (events) {
                len = events.length;
                for(i = 0; i < len; i++) {
                    events[i].handler.apply(this, [evt]);
                }
            }
        },
        /*
         * draw both scene and hit graphs.  If the node being drawn is the stage, all of the layers will be cleared and redra
         * @name draw
         * @methodOf Kinetic.Node.prototype
         *  the scene renderer
         */
        draw: function() {
            var evt = {
                node: this
            };
            
            this.fire(BEFORE_DRAW, evt);
            this.drawScene();
            this.drawHit();
            this.fire(DRAW, evt);
        },
        shouldDrawHit: function() { 
            return this.isVisible() && this.isListening() && !Kinetic.Global.isDragging(); 
        }
    });

    // add getter and setter methods
    Kinetic.Node.addGetterSetter = function(constructor, attr, def, isTransform) {
        this.addGetter(constructor, attr, def);
        this.addSetter(constructor, attr, isTransform);
    };
    Kinetic.Node.addPointGetterSetter = function(constructor, attr, def, isTransform) {
        this.addPointGetter(constructor, attr);
        this.addPointSetter(constructor, attr);  

        // add invdividual component getters and setters
        this.addGetter(constructor, attr + UPPER_X, def);
        this.addGetter(constructor, attr + UPPER_Y, def);
        this.addSetter(constructor, attr + UPPER_X, isTransform);
        this.addSetter(constructor, attr + UPPER_Y, isTransform);
    };
    Kinetic.Node.addRotationGetterSetter = function(constructor, attr, def, isTransform) {
        this.addRotationGetter(constructor, attr, def);
        this.addRotationSetter(constructor, attr, isTransform);    
    };
    Kinetic.Node.addColorGetterSetter = function(constructor, attr) {
        this.addGetter(constructor, attr);
        this.addSetter(constructor, attr); 
  
        // component getters 
        this.addColorRGBGetter(constructor, attr);
        this.addColorComponentGetter(constructor, attr, R);
        this.addColorComponentGetter(constructor, attr, G);
        this.addColorComponentGetter(constructor, attr, B);

        // component setters
        this.addColorRGBSetter(constructor, attr);
        this.addColorComponentSetter(constructor, attr, R);
        this.addColorComponentSetter(constructor, attr, G);
        this.addColorComponentSetter(constructor, attr, B);
    };
    Kinetic.Node.addColorRGBGetter = function(constructor, attr) {
        var method = GET + Kinetic.Util._capitalize(attr) + RGB;
        constructor.prototype[method] = function() {
            return Kinetic.Util.getRGB(this.attrs[attr]);
        };
    };
    Kinetic.Node.addColorRGBSetter = function(constructor, attr) {
        var method = SET + Kinetic.Util._capitalize(attr) + RGB;

        constructor.prototype[method] = function(obj) {
            var r = obj && obj.r !== undefined ? obj.r | 0 : this.getAttr(attr + UPPER_R),
                g = obj && obj.g !== undefined ? obj.g | 0 : this.getAttr(attr + UPPER_G),
                b = obj && obj.b !== undefined ? obj.b | 0 : this.getAttr(attr + UPPER_B);

            this.setAttr(attr, HASH + Kinetic.Util._rgbToHex(r, g, b));
        };
    };
    Kinetic.Node.addColorComponentGetter = function(constructor, attr, c) {
        var prefix = GET + Kinetic.Util._capitalize(attr),
            method = prefix + Kinetic.Util._capitalize(c);
        constructor.prototype[method] = function() {
            return this[prefix + RGB]()[c];
        };
    };
    Kinetic.Node.addColorComponentSetter = function(constructor, attr, c) {
        var prefix = SET + Kinetic.Util._capitalize(attr),
            method = prefix + Kinetic.Util._capitalize(c);
        constructor.prototype[method] = function(val) {
            var obj = {};
            obj[c] = val;
            this[prefix + RGB](obj);
        };
    };
    Kinetic.Node.addSetter = function(constructor, attr, isTransform) {
        var that = this,
            method = SET + Kinetic.Util._capitalize(attr);
            
        constructor.prototype[method] = function(val) {
            this.setAttr(attr, val);
            if (isTransform) {
                this.cachedTransform = null;
            }
        };
    };
    Kinetic.Node.addPointSetter = function(constructor, attr) {
        var that = this,
            baseMethod = SET + Kinetic.Util._capitalize(attr);
            
        constructor.prototype[baseMethod] = function() {
            var pos = Kinetic.Util._getXY([].slice.call(arguments)),
                oldVal = this.attrs[attr]; 
                x = 0,
                y = 0;

            if (pos) {
              x = pos.x;
              y = pos.y;

              this._fireBeforeChangeEvent(attr, oldVal, pos);
              if (x !== undefined) {
                this[baseMethod + UPPER_X](x);
              }
              if (y !== undefined) {
                this[baseMethod + UPPER_Y](y);
              }
              this._fireChangeEvent(attr, oldVal, pos);
            }    
        };
    };
    Kinetic.Node.addRotationSetter = function(constructor, attr, isTransform) {
        var that = this,
            method = SET + Kinetic.Util._capitalize(attr);
            
        // radians
        constructor.prototype[method] = function(val) {
            this.setAttr(attr, val);
            if (isTransform) {
                this.cachedTransform = null;
            }
        };
        // degrees
        constructor.prototype[method + DEG] = function(deg) {
            this.setAttr(attr, Kinetic.Util._degToRad(deg));
            if (isTransform) {
                this.cachedTransform = null;
            }
        };
    };
    Kinetic.Node.addGetter = function(constructor, attr, def) {
        var that = this,
            method = GET + Kinetic.Util._capitalize(attr);
           
        constructor.prototype[method] = function(arg) {
            var val = this.attrs[attr];
            if (val === undefined) {
                val = def; 
            }

            return val;    
        };
    };
    Kinetic.Node.addPointGetter = function(constructor, attr) {
        var that = this,
            baseMethod = GET + Kinetic.Util._capitalize(attr);
            
        constructor.prototype[baseMethod] = function(arg) {
            var that = this;
            return {
                x: that[baseMethod + UPPER_X](),
                y: that[baseMethod + UPPER_Y]()
            };  
        };
    };
    Kinetic.Node.addRotationGetter = function(constructor, attr, def) {
        var that = this,
            method = GET + Kinetic.Util._capitalize(attr);
            
        // radians
        constructor.prototype[method] = function() {
            var val = this.attrs[attr];
            if (val === undefined) {
                val = def; 
            }
            return val;
        };
        // degrees
        constructor.prototype[method + DEG] = function() {
            var val = this.attrs[attr];
            if (val === undefined) {
                val = def; 
            }
            return Kinetic.Util._radToDeg(val);
        };
    };
    /**
     * create node with JSON string.  De-serializtion does not generate custom
     *  shape drawing functions, images, or event handlers (this would make the
     * 	serialized object huge).  If your app uses custom shapes, images, and
     *  event handlers (it probably does), then you need to select the appropriate
     *  shapes after loading the stage and set these properties via on(), setDrawFunc(),
     *  and setImage() methods
     * @name create
     * @methodOf Kinetic.Node
     * @param {String} JSON string
     * @param {DomElement} [container] optional container dom element used only if you're
     *  creating a stage node
     */
    Kinetic.Node.create = function(json, container) {
        return this._createNode(JSON.parse(json), container);
    };
    Kinetic.Node._createNode = function(obj, container) {
        var type, no, len, n;

        // determine type
        if(obj.nodeType === SHAPE) {
            // add custom shape
            if(obj.shapeType === undefined) {
                type = SHAPE;
            }
            // add standard shape
            else {
                type = obj.shapeType;
            }
        }
        else {
            type = obj.nodeType;
        }

        // if container was passed in, add it to attrs
        if(container) {
            obj.attrs.container = container;
        }

        no = new Kinetic[type](obj.attrs);
        if(obj.children) {
            len = obj.children.length;
            for(n = 0; n < len; n++) {
                no.add(this._createNode(obj.children[n]));
            }
        }

        return no;
    };
    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Node, 'x', 0, true);

    /**
     * set x position
     * @name setX
     * @methodOf Kinetic.Node.prototype
     * @param {Number} x
     */

    /**
     * get x position
     * @name getX
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Node, 'y', 0, true);

    /**
     * set y position
     * @name setY
     * @methodOf Kinetic.Node.prototype
     * @param {Number} y
     */

    /**
     * get y position
     * @name getY
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Node, 'opacity', 1);

    /**
     * set opacity.  Opacity values range from 0 to 1.
     *  A node with an opacity of 0 is fully transparent, and a node
     *  with an opacity of 1 is fully opaque
     * @name setOpacity
     * @methodOf Kinetic.Node.prototype
     * @param {Object} opacity
     */

    /**
     * get opacity.
     * @name getOpacity
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addGetter(Kinetic.Node, 'name');

     /**
     * get name
     * @name getName
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addGetter(Kinetic.Node, 'id');

    /**
     * get id
     * @name getId
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addRotationGetterSetter(Kinetic.Node, 'rotation', 0, true);

    /**
     * set rotation in radians
     * @name setRotation
     * @methodOf Kinetic.Node.prototype
     * @param {Number} theta
     */

    /**
     * set rotation in degrees
     * @name setRotationDeg
     * @methodOf Kinetic.Node.prototype
     * @param {Number} deg
     */

    /**
     * get rotation in degrees
     * @name getRotationDeg
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get rotation in radians
     * @name getRotation
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Node, 'scale', 1, true);

    /**
     * set scale
     * @name setScale
     * @param {Number} x
     * @param {Number} y
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * set scale x
     * @name setScaleX
     * @param {Number} x
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * set scale y
     * @name setScaleY
     * @param {Number} y
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get scale
     * @name getScale
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * get scale x
     * @name getScaleX
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * get scale y
     * @name getScaleY
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Node, 'skew', 0, true);

    /**
     * set skew
     * @name setSkew
     * @param {Number} x
     * @param {Number} y
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * set skew x
     * @name setSkewX
     * @param {Number} x
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * set skew y
     * @name setSkewY
     * @param {Number} y
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get skew 
     * @name getSkew
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * get skew x
     * @name getSkewX
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * get skew y
     * @name getSkewY
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Node, 'offset', 0, true);

    /**
     * set offset.  A node's offset defines the position and rotation point
     * @name setOffset
     * @methodOf Kinetic.Node.prototype
     * @param {Number} x
     * @param {Number} y
     */

     /**
     * set offset x
     * @name setOffsetX
     * @methodOf Kinetic.Node.prototype
     * @param {Number} x
     */

     /**
     * set offset y
     * @name setOffsetY
     * @methodOf Kinetic.Node.prototype
     * @param {Number} y
     */

    /**
     * get offset
     * @name getOffset
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * get offset x
     * @name getOffsetX
     * @methodOf Kinetic.Node.prototype
     */

     /**
     * get offset y
     * @name getOffsetY
     * @methodOf Kinetic.Node.prototype
     */

    Kinetic.Node.addSetter(Kinetic.Node, 'width');

    /**
     * set width
     * @name setWidth
     * @methodOf Kinetic.Node.prototype
     * @param {Number} width
     */

    Kinetic.Node.addSetter(Kinetic.Node, 'height');

    /**
     * set height
     * @name setHeight
     * @methodOf Kinetic.Node.prototype
     * @param {Number} height
     */

    Kinetic.Node.addSetter(Kinetic.Node, 'listening');

    /**
     * listen or don't listen to events
     * @name setListening
     * @methodOf Kinetic.Node.prototype
     * @param {Boolean} listening
     */

    Kinetic.Node.addSetter(Kinetic.Node, 'visible');

    /**
     * set visible
     * @name setVisible
     * @methodOf Kinetic.Node.prototype
     * @param {Boolean} visible
     */

    // aliases
    /**
     * Alias of getListening()
     * @name isListening
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isListening = Kinetic.Node.prototype.getListening;
    /**
     * Alias of getVisible()
     * @name isVisible
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isVisible = Kinetic.Node.prototype.getVisible;
    
    Kinetic.Collection.mapMethods(['on', 'off']);
})();

(function() {
    /**
     * Animation constructor.  A stage is used to contain multiple layers and handle
     * @constructor
     * @param {Function} func function executed on each animation frame
     * @param {Kinetic.Layer|Array} [layers] layer(s) to be redrawn.&nbsp; Can be a layer, an array of layers, or null.  Not specifying a node will result in no redraw.
     */
    Kinetic.Animation = function(func, layers) {
        this.func = func;
        this.setLayers(layers);
        this.id = Kinetic.Animation.animIdCounter++;
        this.frame = {
            time: 0,
            timeDiff: 0,
            lastTime: new Date().getTime()
        };
    };
    /*
     * Animation methods
     */
    Kinetic.Animation.prototype = {
        /**
         * set layers to be redrawn on each animation frame
         * @name setLayers
         * @methodOf Kinetic.Animation.prototype
         * @param {Kinetic.Layer|Array} [layers] layer(s) to be redrawn.&nbsp; Can be a layer, an array of layers, or null.  Not specifying a node will result in no redraw.
         */
        setLayers: function(layers) {
            var lays = []; 
            // if passing in no layers
            if (!layers) {
                lays = [];
            }
            // if passing in an array of Layers
            else if (Kinetic.Util._isArray(layers)) {
                lays = layers;
            }
            // if passing in a Layer
            else {
                lays = [layers];
            }

            this.layers = lays;
        },
        /**
         * get layers
         * @name getLayers
         * @methodOf Kinetic.Animation.prototype
         */
        getLayers: function() {
            return this.layers;
        },
        /**
         * add layer.  Returns true if the layer was added, and false if it was not
         * @name addLayer
         * @methodOf Kinetic.Animation.prototype
         * @param {Kinetic.Layer} layer
         */
        addLayer: function(layer) {
            var layers = this.layers,
                len, n;

            if (layers) {
                len = layers.length;

                // don't add the layer if it already exists
                for (n = 0; n < len; n++) {
                    if (layers[n]._id === layer._id) {
                        return false; 
                    } 
                } 
            }
            else {
                this.layers = [];
            }

            this.layers.push(layer);
            return true;
        },
        /**
         * determine if animation is running or not.  returns true or false
         * @name isRunning
         * @methodOf Kinetic.Animation.prototype
         */
        isRunning: function() {
            var a = Kinetic.Animation, animations = a.animations;
            for(var n = 0; n < animations.length; n++) {
                if(animations[n].id === this.id) {
                    return true;
                }
            }
            return false;
        },
        /**
         * start animation
         * @name start
         * @methodOf Kinetic.Animation.prototype
         */
        start: function() {
            this.stop();
            this.frame.timeDiff = 0;
            this.frame.lastTime = new Date().getTime();
            Kinetic.Animation._addAnimation(this);
        },
        /**
         * stop animation
         * @name stop
         * @methodOf Kinetic.Animation.prototype
         */
        stop: function() {
            Kinetic.Animation._removeAnimation(this);
        },
        _updateFrameObject: function(time) {
            this.frame.timeDiff = time - this.frame.lastTime;
            this.frame.lastTime = time;
            this.frame.time += this.frame.timeDiff;
            this.frame.frameRate = 1000 / this.frame.timeDiff;
        }
    };
    Kinetic.Animation.animations = [];
    Kinetic.Animation.animIdCounter = 0;
    Kinetic.Animation.animRunning = false;

    Kinetic.Animation._addAnimation = function(anim) {
        this.animations.push(anim);
        this._handleAnimation();
    };
    Kinetic.Animation._removeAnimation = function(anim) {
        var id = anim.id, animations = this.animations, len = animations.length;
        for(var n = 0; n < len; n++) {
            if(animations[n].id === id) {
                this.animations.splice(n, 1);
                break;
            }
        }
    };

    Kinetic.Animation._runFrames = function() {
        var layerHash = {}, 
            animations = this.animations,
            anim, layers, func, n, i, layersLen, layer, key;
        /*
         * loop through all animations and execute animation
         *  function.  if the animation object has specified node,
         *  we can add the node to the nodes hash to eliminate
         *  drawing the same node multiple times.  The node property
         *  can be the stage itself or a layer
         */
        /*
         * WARNING: don't cache animations.length because it could change while
         * the for loop is running, causing a JS error
         */
        for(n = 0; n < animations.length; n++) {
            anim = animations[n];
            layers = anim.layers; 
            func = anim.func;

            anim._updateFrameObject(new Date().getTime());
            layersLen = layers.length;

            for (i=0; i<layersLen; i++) {
                layer = layers[i]
                if(layer._id !== undefined) {
                    layerHash[layer._id] = layer;
                }
            }

            // if animation object has a function, execute it
            if(func) {
                func.call(anim, anim.frame);
            }
        }

        for(key in layerHash) {
            layerHash[key].draw();
        }
    };
    Kinetic.Animation._animationLoop = function() {
        var that = this;
        if(this.animations.length > 0) {
            this._runFrames();
            Kinetic.Animation.requestAnimFrame(function() {
                that._animationLoop();
            });
        }
        else {
            this.animRunning = false;
        }
    };
    Kinetic.Animation._handleAnimation = function() {
        var that = this;
        if(!this.animRunning) {
            this.animRunning = true;
            that._animationLoop();
        }
    };
    RAF = (function() {
        return window.requestAnimationFrame 
            || window.webkitRequestAnimationFrame 
            || window.mozRequestAnimationFrame 
            || window.oRequestAnimationFrame 
            || window.msRequestAnimationFrame 
            || FRAF;
    })();

    function FRAF(callback) {
        window.setTimeout(callback, 1000 / 60);
    }

    Kinetic.Animation.requestAnimFrame = function(callback) {
        var raf = Kinetic.DD && Kinetic.DD.isDragging ? FRAF : RAF;
        raf(callback);
    };
    
    var moveTo = Kinetic.Node.prototype.moveTo;
    Kinetic.Node.prototype.moveTo = function(container) {
        moveTo.call(this, container);
    };

    Kinetic.Layer.batchAnim = new Kinetic.Animation(function() {
        if (this.getLayers().length === 0) {
            this.stop();
        }
        this.setLayers([]);
    });

    /**
     * get batch draw
     * @name batchDraw
     * @methodOf Kinetic.Layer.prototype
     */
    Kinetic.Layer.prototype.batchDraw = function() {
        var batchAnim = Kinetic.Layer.batchAnim;
        batchAnim.addLayer(this);  

        if (!batchAnim.isRunning()) {
            batchAnim.start(); 
        } 
    };
})();
(function() {
    var blacklist = {
        node: 1,
        duration: 1,
        easing: 1,
        onFinish: 1,
        yoyo: 1
    },

    PAUSED = 1,
    PLAYING = 2,
    REVERSING = 3;

    function createTween(node, key, easing, end, duration, yoyo) {
        var method = 'set' + Kinetic.Util._capitalize(key);
        return new Tween(key, function(i) {
            node[method](i);  
        }, easing, node['get' + Kinetic.Util._capitalize(key)](), end, duration * 1000, yoyo);
    }

    Kinetic.Tween = function(config) {
        var that = this,
            node = config.node,
            nodeId = node._id,
            duration = config.duration || 1,
            easing = config.easing || Kinetic.Easings.Linear,
            yoyo = !!config.yoyo,
            key, tween;

        this.tweens = [];
        this.node = node;
        // event handlers
        this.onFinish = config.onFinish;

        this.anim = new Kinetic.Animation(function() {
            that.onEnterFrame();
        }, node.getLayer());

        for (key in config) {
            if (blacklist[key] === undefined) {
                tween = createTween(node, key, easing, config[key], duration, yoyo); 
                this.tweens.push(tween);
                this._addListeners(tween);
                Kinetic.Tween.add(nodeId, key, this);
            }
        }

        this.reset();
    };

    Kinetic.Tween.tweens = {};

    Kinetic.Tween.add = function(nodeId, prop, ktween) {
        var key = nodeId + '-' + prop,
            tween = Kinetic.Tween.tweens[key];

        if (tween) {
            tween._removeTween(prop);
        }
        
        Kinetic.Tween.tweens[key] = ktween;   
    };


    Kinetic.Tween.prototype = {
        _iterate: function(func) {
            var tweens = this.tweens,
                n = 0,
                tween = tweens[n];

            while(tween) {
                func(tween, n++);
                tween = tweens[n];
            }  
        },
        _addListeners: function(tween) {
            var that = this;

            // start listeners
            tween.onPlay = function() {
                that.anim.start();
            };
            tween.onReverse = function() {
                that.anim.start();
            };

            // stop listeners
            tween.onPause = function() {
                that.anim.stop();
            };
            tween.onFinish = function() {
                if (that.onFinish) {
                    that.onFinish();
                }
            };
        },
        play: function() {
            this._iterate(function(tween) {
                tween.play();
            });
        },
        reverse: function() {
            this._iterate(function(tween) {
                tween.reverse();
            });
        },
        reset: function() {
            this._iterate(function(tween) {
                tween.reset();
            });
            this.node.getLayer().draw();
        },
        seek: function(t) {
            this._iterate(function(tween) {
                tween.seek(t * 1000);
            });
            this.node.getLayer().draw();
        },
        pause: function() {
            this._iterate(function(tween) {
                tween.pause();
            });
        },
        finish: function() {
            this._iterate(function(tween) {
                tween.finish();
            });
            this.node.getLayer().draw();
        },
        onEnterFrame: function() {
            this._iterate(function(tween) {
                tween.onEnterFrame();
            });
        },
        destroy: function() {

        },
        _removeTween: function(prop) {
            var that = this;
            this._iterate(function(tween, n) {
                if (tween.prop === prop) {
                    that.tweens.splice(n, 1);
                }
            });
        }
    };

    var Tween = function(prop, propFunc, func, begin, finish, duration, yoyo) {
        this.prop = prop;
        this.propFunc = propFunc;
        this.begin = begin;
        this._pos = begin;
        this.duration = duration;
        this._change = 0;
        this.prevPos = 0;
        this.yoyo = yoyo;
        this._time = 0;
        this._position = 0;
        this._startTime = 0;
        this._finish = 0;
        this.func = func;
        this._change = finish - this.begin;
        this.pause();
    };
    /*
     * Tween methods
     */
    Tween.prototype = {
        fire: function(str) {
            var handler = this[str];
            if (handler) {
                handler();
            }
        },
        setTime: function(t) {
            if(t > this.duration) {
                if(this.yoyo) {
                    this._time = this.duration;
                    this.reverse();
                }
                else {
                    this.finish();
                }
            }
            else if(t < 0) {
                if(this.yoyo) {
                    this._time = 0;
                    this.play();
                }
                else {
                    this.reset();
                }
            }
            else {
                this._time = t;
                this.update();
            }
        },
        getTime: function() {
            return this._time;
        },
        setPosition: function(p) {
            this.prevPos = this._pos;
            this.propFunc(p);
            this._pos = p;
        },
        getPosition: function(t) {
            if(t === undefined) {
                t = this._time;
            }
            return this.func(t, this.begin, this._change, this.duration);
        },
        play: function() {
            this.state = PLAYING;
            this._startTime = this.getTimer() - this._time;
            this.onEnterFrame();
            this.fire('onPlay');
        },
        reverse: function() {
            this.state = REVERSING;
            this._time = this.duration - this._time;
            this._startTime = this.getTimer() - this._time;
            this.onEnterFrame();
            this.fire('onReverse');
        },
        seek: function(t) {
            this.pause();
            this._time = t;
            this.update();
            this.fire('onSeek');
        },
        reset: function() {
            this.pause();
            this._time = 0;
            this.update();
            this.fire('onReset');
        },
        finish: function() {
            this.pause();
            this._time = this.duration;
            this.update();
            this.fire('onFinish');
        },
        update: function() {
            this.setPosition(this.getPosition(this._time));
        },
        onEnterFrame: function() {
            var t = this.getTimer() - this._startTime;
            if(this.state === PLAYING) {
                this.setTime(t);
            }
            else if (this.state === REVERSING) {
                this.setTime(this.duration - t);
            }
        },
        pause: function() {
            this.state = PAUSED; 
            this.fire('onPause');
        },
        getTimer: function() {
            return new Date().getTime();
        }
    };

    /*
    * These eases were ported from an Adobe Flash tweening library to JavaScript
    * by Xaric
    */
    Kinetic.Easings = {
        'BackEaseIn': function(t, b, c, d, a, p) {
            var s = 1.70158;
            return c * (t /= d) * t * ((s + 1) * t - s) + b;
        },
        'BackEaseOut': function(t, b, c, d, a, p) {
            var s = 1.70158;
            return c * (( t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
        },
        'BackEaseInOut': function(t, b, c, d, a, p) {
            var s = 1.70158;
            if((t /= d / 2) < 1) {
                return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
            }
            return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
        },
        'ElasticEaseIn': function(t, b, c, d, a, p) {
            // added s = 0
            var s = 0;
            if(t === 0) {
                return b;
            }
            if((t /= d) == 1) {
                return b + c;
            }
            if(!p) {
                p = d * 0.3;
            }
            if(!a || a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }
            else {
                s = p / (2 * Math.PI) * Math.asin(c / a);
            }
            return -(a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        },
        'ElasticEaseOut': function(t, b, c, d, a, p) {
            // added s = 0
            var s = 0;
            if(t === 0) {
                return b;
            }
            if((t /= d) == 1) {
                return b + c;
            }
            if(!p) {
                p = d * 0.3;
            }
            if(!a || a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }
            else {
                s = p / (2 * Math.PI) * Math.asin(c / a);
            }
            return (a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b);
        },
        'ElasticEaseInOut': function(t, b, c, d, a, p) {
            // added s = 0
            var s = 0;
            if(t === 0) {
                return b;
            }
            if((t /= d / 2) == 2) {
                return b + c;
            }
            if(!p) {
                p = d * (0.3 * 1.5);
            }
            if(!a || a < Math.abs(c)) {
                a = c;
                s = p / 4;
            }
            else {
                s = p / (2 * Math.PI) * Math.asin(c / a);
            }
            if(t < 1) {
                return -0.5 * (a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
            }
            return a * Math.pow(2, -10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p) * 0.5 + c + b;
        },
        'BounceEaseOut': function(t, b, c, d) {
            if((t /= d) < (1 / 2.75)) {
                return c * (7.5625 * t * t) + b;
            }
            else if(t < (2 / 2.75)) {
                return c * (7.5625 * (t -= (1.5 / 2.75)) * t + 0.75) + b;
            }
            else if(t < (2.5 / 2.75)) {
                return c * (7.5625 * (t -= (2.25 / 2.75)) * t + 0.9375) + b;
            }
            else {
                return c * (7.5625 * (t -= (2.625 / 2.75)) * t + 0.984375) + b;
            }
        },
        'BounceEaseIn': function(t, b, c, d) {
            return c - Kinetic.Easings.BounceEaseOut(d - t, 0, c, d) + b;
        },
        'BounceEaseInOut': function(t, b, c, d) {
            if(t < d / 2) {
                return Kinetic.Easings.BounceEaseIn(t * 2, 0, c, d) * 0.5 + b;
            }
            else {
                return Kinetic.Easings.BounceEaseOut(t * 2 - d, 0, c, d) * 0.5 + c * 0.5 + b;
            }
        },
        'EaseIn': function(t, b, c, d) {
            return c * (t /= d) * t + b;
        },
        'EaseOut': function(t, b, c, d) {
            return -c * (t /= d) * (t - 2) + b;
        },
        'EaseInOut': function(t, b, c, d) {
            if((t /= d / 2) < 1) {
                return c / 2 * t * t + b;
            }
            return -c / 2 * ((--t) * (t - 2) - 1) + b;
        },
        'StrongEaseIn': function(t, b, c, d) {
            return c * (t /= d) * t * t * t * t + b;
        },
        'StrongEaseOut': function(t, b, c, d) {
            return c * (( t = t / d - 1) * t * t * t * t + 1) + b;
        },
        'StrongEaseInOut': function(t, b, c, d) {
            if((t /= d / 2) < 1) {
                return c / 2 * t * t * t * t * t + b;
            }
            return c / 2 * ((t -= 2) * t * t * t * t + 2) + b;
        },
        'Linear': function(t, b, c, d) {
            return c * t / d + b;
        }
    };
})();
(function() {
    Kinetic.DD = {
        // properties
        anim: new Kinetic.Animation(),
        isDragging: false,
        offset: {
            x: 0,
            y: 0
        },
        node: null,
        
        // methods
        _drag: function(evt) {
            var dd = Kinetic.DD, 
                node = dd.node;
    
            if(node) {
                var pos = node.getStage().getPointerPosition();
                var dbf = node.getDragBoundFunc();
    
                var newNodePos = {
                    x: pos.x - dd.offset.x,
                    y: pos.y - dd.offset.y
                };
    
                if(dbf !== undefined) {
                    newNodePos = dbf.call(node, newNodePos, evt);
                }
    
                node.setAbsolutePosition(newNodePos);
    
                if(!dd.isDragging) {
                    dd.isDragging = true;
                    node._handleEvent('dragstart', evt);
                }
                
                // execute ondragmove if defined
                node._handleEvent('dragmove', evt);
            }
        },
        _endDragBefore: function(evt) {
            var dd = Kinetic.DD, 
                node = dd.node,
                nodeType, layer;
    
            if(node) {
                nodeType = node.nodeType,
                layer = node.getLayer();
                dd.anim.stop();
    
                // only fire dragend event if the drag and drop
                // operation actually started. 
                if(dd.isDragging) {
                    dd.isDragging = false;

                    if (evt) {
                        evt.dragEndNode = node;
                    } 
                }
                
                delete dd.node;
               
                (layer || node).draw();
            }
        },
        _endDragAfter: function(evt) {
            var evt = evt || {},
                dragEndNode = evt.dragEndNode;
                  
            if (evt && dragEndNode) {
              dragEndNode._handleEvent('dragend', evt); 
            }
        }
    };

    // Node extenders
    
    /**
     * initiate drag and drop
     * @name startDrag
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.startDrag = function() {
        var dd = Kinetic.DD, 
            that = this, 
            stage = this.getStage(),
            layer = this.getLayer(), 
            pos = stage.getPointerPosition(),
            m = this.getTransform().getTranslation(), 
            ap = this.getAbsolutePosition();
                
        if(pos) {
            if (dd.node) {
                dd.node.stopDrag(); 
            }
          
            dd.node = this;
            dd.offset.x = pos.x - ap.x;
            dd.offset.y = pos.y - ap.y;
            dd.anim.setLayers(layer || this.getLayers());
            dd.anim.start();
        }
    };
    
    /**
     * stop drag and drop
     * @name stopDrag
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.stopDrag = function() {
        var dd = Kinetic.DD,
            evt = {};
        dd._endDragBefore(evt);
        dd._endDragAfter(evt);
    };
            
    /**
     * set draggable
     * @name setDraggable
     * @methodOf Kinetic.Node.prototype
     * @param {String} draggable
     */
    Kinetic.Node.prototype.setDraggable = function(draggable) {
        this.setAttr('draggable', draggable);
        this._dragChange();
    };

    var origDestroy = Kinetic.Node.prototype.destroy;

    Kinetic.Node.prototype.destroy = function() {
        var dd = Kinetic.DD;

        // stop DD
        if(dd.node && dd.node._id === this._id) {

            this.stopDrag();
        } 

        origDestroy.call(this); 
    };

    /**
     * determine if node is currently in drag and drop mode
     * @name isDragging
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isDragging = function() {
        var dd = Kinetic.DD;
        return dd.node && dd.node._id === this._id && dd.isDragging;
    };

    Kinetic.Node.prototype._listenDrag = function() {
        this._dragCleanup();
        var that = this;
        this.on('mousedown.kinetic touchstart.kinetic', function(evt) {
            if(!Kinetic.DD.node) {
                that.startDrag(evt);
            }
        });
    };

    Kinetic.Node.prototype._dragChange = function() {
        if(this.attrs.draggable) {
            this._listenDrag();
        }
        else {
            // remove event listeners
            this._dragCleanup();

            /*
             * force drag and drop to end
             * if this node is currently in
             * drag and drop mode
             */
            var stage = this.getStage();
            var dd = Kinetic.DD;
            if(stage && dd.node && dd.node._id === this._id) {
                dd.node.stopDrag();
            }
        }
    };
    
    Kinetic.Node.prototype._dragCleanup = function() {
        this.off('mousedown.kinetic');
        this.off('touchstart.kinetic');
    };

    Kinetic.Node.addGetterSetter(Kinetic.Node, 'dragBoundFunc');
    Kinetic.Node.addGetterSetter(Kinetic.Node, 'dragOnTop', true);
    
    Kinetic.Node.addGetter(Kinetic.Node, 'draggable', false);

    /**
     * set drag bound function.  This is used to override the default
     *  drag and drop position
     * @name setDragBoundFunc
     * @methodOf Kinetic.Node.prototype
     * @param {Function} dragBoundFunc
     */

    /**
     * set flag which enables or disables automatically moving the draggable node to a
     *  temporary top layer to improve performance.  The default is true
     * @name setDragOnTop
     * @methodOf Kinetic.Node.prototype
     * @param {Boolean} dragOnTop
     */

    /**
     * get dragBoundFunc
     * @name getDragBoundFunc
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get flag which enables or disables automatically moving the draggable node to a
     *  temporary top layer to improve performance.
     * @name getDragOnTop
     * @methodOf Kinetic.Node.prototype
     */
    
     /**
     * get draggable
     * @name getDraggable
     * @methodOf Kinetic.Node.prototype
     */

    /**
     * get draggable.  Alias of getDraggable()
     * @name isDraggable
     * @methodOf Kinetic.Node.prototype
     */
    Kinetic.Node.prototype.isDraggable = Kinetic.Node.prototype.getDraggable;

    var html = document.getElementsByTagName('html')[0];
    html.addEventListener('mouseup', Kinetic.DD._endDragBefore, true);
    html.addEventListener('touchend', Kinetic.DD._endDragBefore, true);
    
    html.addEventListener('mouseup', Kinetic.DD._endDragAfter, false);
    html.addEventListener('touchend', Kinetic.DD._endDragAfter, false);
    
})();

(function() {
    /**
     * Container constructor.&nbsp; Containers are used to contain nodes or other containers
     * @constructor
     * @augments Kinetic.Node
     * @param {Object} config
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     * @param {Function} [config.clipFunc] clipping function

     */
    Kinetic.Util.addMethods(Kinetic.Container, {
        _containerInit: function(config) {
            this.children = [];
            Kinetic.Node.call(this, config);
        },
        /**
         * get children
         * @name getChildren
         * @methodOf Kinetic.Container.prototype
         */
        getChildren: function() {
            return this.children;
        },
        /**
         * remove all children
         * @name removeChildren
         * @methodOf Kinetic.Container.prototype
         */
        removeChildren: function() {
            while(this.children.length > 0) {
                this.children[0].remove();
            }
        },
        /**
         * add node to container
         * @name add
         * @methodOf Kinetic.Container.prototype
         * @param {Node} child
         */
        add: function(child) {
            var go = Kinetic.Global, children = this.children;
            child.index = children.length;
            child.parent = this;
            children.push(child);

            // chainable
            return this;
        },
        /**
         * return an array of nodes that match the selector.  Use '#' for id selections
         * and '.' for name selections
         * ex:
         * var node = stage.get('#foo'); // selects node with id foo
         * var nodes = layer.get('.bar'); // selects nodes with name bar inside layer
         * @name get
         * @methodOf Kinetic.Container.prototype
         * @param {String} selector
         */
        get: function(selector) {
            var collection = new Kinetic.Collection();
            // ID selector
            if(selector.charAt(0) === '#') {
                var node = this._getNodeById(selector.slice(1));
                if(node) {
                    collection.push(node);
                }
            }
            // name selector
            else if(selector.charAt(0) === '.') {
                var nodeList = this._getNodesByName(selector.slice(1));
                Kinetic.Collection.apply(collection, nodeList);
            }
            // unrecognized selector, pass to children
            else {
                var retArr = [];
                var children = this.getChildren();
                var len = children.length;
                for(var n = 0; n < len; n++) {
                    retArr = retArr.concat(children[n]._get(selector));
                }
                Kinetic.Collection.apply(collection, retArr);
            }
            return collection;
        },
        _getNodeById: function(key) {
            var stage = this.getStage(), go = Kinetic.Global, node = go.ids[key];
            if(node !== undefined && this.isAncestorOf(node)) {
                return node;
            }
            return null;
        },
        _getNodesByName: function(key) {
            var go = Kinetic.Global, arr = go.names[key] || [];
            return this._getDescendants(arr);
        },
        _get: function(selector) {
            var retArr = Kinetic.Node.prototype._get.call(this, selector);
            var children = this.getChildren();
            var len = children.length;
            for(var n = 0; n < len; n++) {
                retArr = retArr.concat(children[n]._get(selector));
            }
            return retArr;
        },
        // extenders
        toObject: function() {
            var obj = Kinetic.Node.prototype.toObject.call(this);

            obj.children = [];

            var children = this.getChildren();
            var len = children.length;
            for(var n = 0; n < len; n++) {
                var child = children[n];
                obj.children.push(child.toObject());
            }

            return obj;
        },
        _getDescendants: function(arr) {
            var retArr = [];
            var len = arr.length;
            for(var n = 0; n < len; n++) {
                var node = arr[n];
                if(this.isAncestorOf(node)) {
                    retArr.push(node);
                }
            }

            return retArr;
        },
        /**
         * determine if node is an ancestor
         * of descendant
         * @name isAncestorOf
         * @methodOf Kinetic.Container.prototype
         * @param {Kinetic.Node} node
         */
        isAncestorOf: function(node) {
            var parent = node.getParent();
            while(parent) {
                if(parent._id === this._id) {
                    return true;
                }
                parent = parent.getParent();
            }

            return false;
        },
        /**
         * clone node
         * @name clone
         * @methodOf Kinetic.Container.prototype
         * @param {Object} attrs override attrs
         */
        clone: function(obj) {
            // call super method
            var node = Kinetic.Node.prototype.clone.call(this, obj)

            // perform deep clone on containers
            for(var key in this.children) {
                node.add(this.children[key].clone());
            }
            return node;
        },
        /**
         * get shapes that intersect a point
         * @name getIntersections
         * @methodOf Kinetic.Container.prototype
         * @param {Object} point
         */
        getIntersections: function() {
            var pos = Kinetic.Util._getXY(Array.prototype.slice.call(arguments));
            var arr = [];
            var shapes = this.get('Shape');

            var len = shapes.length;
            for(var n = 0; n < len; n++) {
                var shape = shapes[n];
                if(shape.isVisible() && shape.intersects(pos)) {
                    arr.push(shape);
                }
            }

            return arr;
        },
        /**
         * set children indices
         */
        _setChildrenIndices: function() {
            var children = this.children, len = children.length;
            for(var n = 0; n < len; n++) {
                children[n].index = n;
            }
        },
        drawScene: function(canvas) {
            var layer = this.getLayer(),
                clip = !!this.getClipFunc(),
                children, n, len;
                
            if (!canvas && layer) {
                canvas = layer.getCanvas(); 
            }  

            if(this.isVisible()) {
                if (clip) {
                    canvas._clip(this);
                }
                
                children = this.children; 
                len = children.length;
                
                for(n = 0; n < len; n++) {
                    children[n].drawScene(canvas);
                }
                
                if (clip) {
                    canvas.getContext().restore();
                }
            }
        },
        drawHit: function() {
            var clip = !!this.getClipFunc() && this.nodeType !== 'Stage',
                n = 0, 
                len = 0, 
                children = [],
                hitCanvas;

            if(this.shouldDrawHit()) {
                if (clip) {
                    hitCanvas = this.getLayer().hitCanvas; 
                    hitCanvas._clip(this);
                }
                
                children = this.children; 
                len = children.length;

                for(n = 0; n < len; n++) {
                    children[n].drawHit();
                }
                if (clip) {
                    hitCanvas.getContext().restore();
                }
            }
        }
    });

    Kinetic.Util.extend(Kinetic.Container, Kinetic.Node);

    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Container, 'clipFunc');

    /**
     * set clipping function 
     * @name setClipFunc
     * @methodOf Kinetic.Container.prototype
     * @param {Number} deg
     */

    /**
     * get clipping function 
     * @name getClipFunc
     * @methodOf Kinetic.Container.prototype
     */
})();

(function() {
    function _fillFunc(context) {
        context.fill();
    }
    function _strokeFunc(context) {
        context.stroke();
    }
    function _fillFuncHit(context) {
        context.fill();
    }
    function _strokeFuncHit(context) {
        context.stroke();
    }

    Kinetic.Util.addMethods(Kinetic.Shape, {
        _initShape: function(config) {
            this.nodeType = 'Shape';
            this._fillFunc = _fillFunc;
            this._strokeFunc = _strokeFunc;
            this._fillFuncHit = _fillFuncHit;
            this._strokeFuncHit = _strokeFuncHit;

            // set colorKey
            var shapes = Kinetic.Global.shapes;
            var key;

            while(true) {
                key = Kinetic.Util.getRandomColor();
                if(key && !( key in shapes)) {
                    break;
                }
            }

            this.colorKey = key;
            shapes[key] = this;

            this.createAttrs();
            // call super constructor
            Kinetic.Node.call(this, config);
        },
        /**
         * get canvas context tied to the layer
         * @name getContext
         * @methodOf Kinetic.Shape.prototype
         */
        getContext: function() {
            return this.getLayer().getContext();
        },
        /**
         * get canvas renderer tied to the layer.  Note that this returns a canvas renderer, not a canvas element
         * @name getCanvas
         * @methodOf Kinetic.Shape.prototype
         */
        getCanvas: function() {
            return this.getLayer().getCanvas();
        },
        /**
         * returns whether or not a shadow will be rendered
         * @name hasShadow
         * @methodOf Kinetic.Shape.prototype
         */
        hasShadow: function() {
            return !!(this.getShadowColor() || this.getShadowBlur() || this.getShadowOffsetX() || this.getShadowOffsetY());
        },
        /**
         * returns whether or not a fill is present
         * @name hasFill
         * @methodOf Kinetic.Shape.prototype
         */
        hasFill: function() {
            return !!(this.getFill() || this.getFillPatternImage() || this.getFillLinearGradientColorStops() || this.getFillRadialGradientColorStops());
        },
        _get: function(selector) {
            return this.nodeType === selector || this.shapeType === selector ? [this] : [];
        },
        /**
         * determines if point is in the shape, regardless if other shapes are on top of it.  Note: because
         * this method clears a temp hit canvas, and redraws the shape, it performs very poorly if executed many times
         * consecutively.  If possible, it's better to use the stage.getIntersections() method instead
         * @name intersects
         * @methodOf Kinetic.Shape.prototype
         * @param {Object} point point can be an object containing
         *  an x and y property, or it can be an array with two elements
         *  in which the first element is the x component and the second
         *  element is the y component
         */
        intersects: function() {
            var pos = Kinetic.Util._getXY(Array.prototype.slice.call(arguments));
            var stage = this.getStage();
            var hitCanvas = stage.hitCanvas;
            hitCanvas.clear();
            this.drawScene(hitCanvas);
            var p = hitCanvas.context.getImageData(pos.x | 0, pos.y | 0, 1, 1).data;
            return p[3] > 0;
        },
        /**
         * enable fill
         */
        enableFill: function() {
            this.setAttr('fillEnabled', true);
        },
        /**
         * disable fill
         */
        disableFill: function() {
            this.setAttr('fillEnabled', false);
        },
        /**
         * enable stroke
         */
        enableStroke: function() {
            this.setAttr('strokeEnabled', true);
        },
        /**
         * disable stroke
         */
        disableStroke: function() {
            this.setAttr('strokeEnabled', false);
        },
        /**
         * enable stroke scale
         */
        enableStrokeScale: function() {
            this.setAttr('strokeScaleEnabled', true);
        },
        /**
         * disable stroke scale
         */
        disableStrokeScale: function() {
            this.setAttr('strokeScaleEnabled', false);
        },
        /**
         * enable shadow
         */
        enableShadow: function() {
            this.setAttr('shadowEnabled', true);
        },
        /**
         * disable shadow
         */
        disableShadow: function() {
            this.setAttr('shadowEnabled', false);
        },
        /**
         * enable dash array
         */
        enableDashArray: function() {
            this.setAttr('dashArrayEnabled', true);
        },
        /**
         * disable dash array
         */
        disableDashArray: function() {
            this.setAttr('dashArrayEnabled', false);
        },
        /**
         * get shape type.  Ex. 'Circle', 'Rect', 'Text', etc.
         * @name getShapeType
         * @methodOf Kinetic.Shape.prototype
         */
        getShapeType: function() {
            return this.shapeType;
        },
        destroy: function() {
            Kinetic.Node.prototype.destroy.call(this);
            delete Kinetic.Global.shapes[this.colorKey];
        },
        drawScene: function(canvas) {
            var drawFunc = this.getDrawFunc(), 
                canvas = canvas || this.getLayer().getCanvas(), 
                context = canvas.getContext();

            if(drawFunc && this.isVisible()) {
                context.save();
                canvas._applyOpacity(this);
                canvas._applyLineJoin(this);                
                canvas._applyAncestorTransforms(this);
                drawFunc.call(this, canvas);
                context.restore();
            }
        },
        drawHit: function() {
            var attrs = this.getAttrs(), 
                drawFunc = attrs.drawHitFunc || attrs.drawFunc, 
                canvas = this.getLayer().hitCanvas, 
                context = canvas.getContext();

            if(drawFunc && this.shouldDrawHit()) {
                context.save();
                canvas._applyLineJoin(this);
                canvas._applyAncestorTransforms(this);

                drawFunc.call(this, canvas);
                context.restore();
            }
        },
        _setDrawFuncs: function() {
            if(!this.attrs.drawFunc && this.drawFunc) {
                this.setDrawFunc(this.drawFunc);
            }
            if(!this.attrs.drawHitFunc && this.drawHitFunc) {
                this.setDrawHitFunc(this.drawHitFunc);
            }
        }
    });
    Kinetic.Util.extend(Kinetic.Shape, Kinetic.Node);

    // add getters and setters
    Kinetic.Node.addColorGetterSetter(Kinetic.Shape, 'stroke');

    /**
     * set stroke color
     * @name setStroke
     * @methodOf Kinetic.Shape.prototype
     * @param {String} color
     */

     /**
     * set stroke color with an object literal
     * @name setStrokeRGB
     * @methodOf Kinetic.Shape.prototype
     * @param {Obect} color requires an object literal containing an r, g, and b component
     */

     /**
     * set stroke color red component
     * @name setStrokeR
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} red
     */

     /**
     * set stroke color green component
     * @name setStrokeG
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} green
     */

     /**
     * set stroke color blue component
     * @name setStrokeB
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} blue
     */

     /**
     * get stroke color
     * @name getStroke
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get stroke color as an object literal
     * @name getStrokeRGB
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get stroke color red component
     * @name getStrokeR
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get stroke color green component
     * @name getStrokeG
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get stroke color blue component
     * @name getStrokeB
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'lineJoin');

    /**
     * set line join
     * @name setLineJoin
     * @methodOf Kinetic.Shape.prototype
     * @param {String} lineJoin.  Can be miter, round, or bevel.  The
     *  default is miter
     */

     /**
     * get line join
     * @name getLineJoin
     * @methodOf Kinetic.Shape.prototype
     */


    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'lineCap');

    /**
     * set line cap.  Can be butt, round, or square
     * @name setLineCap
     * @methodOf Kinetic.Shape.prototype
     * @param {String} lineCap
     */

     /**
     * get line cap
     * @name getLineCap
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'strokeWidth');

    /**
     * set stroke width
     * @name setStrokeWidth
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} strokeWidth
     */

     /**
     * get stroke width
     * @name getStrokeWidth
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'drawFunc');

    /**
     * set draw function
     * @name setDrawFunc
     * @methodOf Kinetic.Shape.prototype
     * @param {Function} drawFunc drawing function
     */

     /**
     * get draw function
     * @name getDrawFunc
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'drawHitFunc');

    /**
     * set draw hit function used for hit detection
     * @name setDrawHitFunc
     * @methodOf Kinetic.Shape.prototype
     * @param {Function} drawHitFunc drawing function used for hit detection
     */

     /**
     * get draw hit function
     * @name getDrawHitFunc
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'dashArray');

    /**
     * set dash array.
     * @name setDashArray
     * @methodOf Kinetic.Shape.prototype
     * @param {Array} dashArray
     *  examples:<br>
     *  [10, 5] dashes are 10px long and 5 pixels apart
     *  [10, 20, 0.001, 20] if using a round lineCap, the line will
     *  be made up of alternating dashed lines that are 10px long
     *  and 20px apart, and dots that have a radius of 5px and are 20px
     *  apart
     */

     /**
     * get dash array
     * @name getDashArray
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addColorGetterSetter(Kinetic.Shape, 'shadowColor');

    /**
     * set shadow color
     * @name setShadowColor
     * @methodOf Kinetic.Shape.prototype
     * @param {String} color
     */

     /**
     * set shadow color with an object literal
     * @name setShadowColorRGB
     * @methodOf Kinetic.Shape.prototype
     * @param {Obect} color requires an object literal containing an r, g, and b component
     */

     /**
     * set shadow color red component
     * @name setShadowColorR
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} red
     */

     /**
     * set shadow color green component
     * @name setShadowColorG
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} green
     */

     /**
     * set shadow color blue component
     * @name setShadowColorB
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} blue
     */

     /**
     * get shadow color
     * @name getShadowColor
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get shadow color as an object literal
     * @name getShadowColorRGB
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get shadow color red component
     * @name getShadowColorR
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get shadow color green component
     * @name getShadowColorG
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get shadow color blue component
     * @name getShadowColorB
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'shadowBlur');

    /**
     * set shadow blur
     * @name setShadowBlur
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} blur
     */

     /**
     * get shadow blur
     * @name getShadowBlur
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'shadowOpacity');

    /**
     * set shadow opacity
     * @name setShadowOpacity
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} opacity must be a value between 0 and 1
     */

     /**
     * get shadow opacity
     * @name getShadowOpacity
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillPatternImage');

    /**
     * set fill pattern image
     * @name setFillPatternImage
     * @methodOf Kinetic.Shape.prototype
     * @param {Image} image object
     */

     /**
     * get fill pattern image
     * @name getFillPatternImage
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addColorGetterSetter(Kinetic.Shape, 'fill');

    /**
     * set fill color
     * @name setFill
     * @methodOf Kinetic.Shape.prototype
     * @param {String} color
     */

     /**
     * set fill color with an object literal
     * @name setFillRGB
     * @methodOf Kinetic.Shape.prototype
     * @param {Obect} color requires an object literal containing an r, g, and b component
     */

     /**
     * set fill color red component
     * @name setFillR
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} red
     */

     /**
     * set fill color green component
     * @name setFillG
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} green
     */

     /**
     * set fill color blue component
     * @name setFillB
     * @methodOf Kinetic.Shape.prototype
     * @param {Integer} blue
     */

     /**
     * get fill color
     * @name getFill
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill color as an object literal
     * @name getFillRGB
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill color red component
     * @name getFillR
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill color green component
     * @name getFillG
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill color blue component
     * @name getFillB
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillPatternX');

    /**
     * set fill pattern x
     * @name setFillPatternX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * get fill pattern x
     * @name getFillPatternX
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillPatternY');

    /**
     * set fill pattern y
     * @name setFillPatternY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

     /**
     * get fill pattern y
     * @name getFillPatternY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillLinearGradientColorStops');

    /**
     * set fill linear gradient color stops
     * @name setFillLinearGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     * @param {Array} colorStops
     */

     /**
     * get fill linear gradient color stops
     * @name getFillLinearGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     * @param {Array} colorStops
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillRadialGradientStartRadius');

    /**
     * set fill radial gradient start radius
     * @name setFillRadialGradientStartRadius
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} radius
     */

     /**
     * get fill radial gradient start radius
     * @name getFillRadialGradientStartRadius
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillRadialGradientEndRadius');

    /**
     * set fill radial gradient end radius
     * @name setFillRadialGradientEndRadius
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} radius
     */

     /**
     * get fill radial gradient end radius
     * @name getFillRadialGradientEndRadius
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillRadialGradientColorStops');

    /**
     * set fill radial gradient color stops
     * @name setFillRadialGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} colorStops
     */

     /**
     * get fill radial gradient color stops
     * @name getFillRadialGradientColorStops
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillPatternRepeat');

    /**
     * set fill pattern repeat
     * @name setFillPatternRepeat
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} repeat can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     */

     /**
     * get fill pattern repeat
     * @name getFillPatternRepeat
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillEnabled', true);

    /**
     * set fill enabled
     * @name setFillEnabled
     * @methodOf Kinetic.Shape.prototype
     * @param {Boolean} enabled
     */

     /**
     * get fill enabled
     * @name getFillEnabled
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'strokeEnabled', true);

    /**
     * set stroke enabled
     * @name setStrokeEnabled
     * @methodOf Kinetic.Shape.prototype
     * @param {Boolean} enabled
     */

     /**
     * get stroke enabled
     * @name getStrokeEnabled
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'shadowEnabled', true);

    /**
     * set shadow enabled
     * @name setShadowEnabled
     * @methodOf Kinetic.Shape.prototype
     * @param {Boolean} enabled
     */

     /**
     * get shadow enabled
     * @name getShadowEnabled
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'dashArrayEnabled', true);

    /**
     * set dash array enabled
     * @name setDashArrayEnabled
     * @methodOf Kinetic.Shape.prototype
     * @param {Boolean} enabled
     */

     /**
     * get dash array enabled
     * @name getDashArrayEnabled
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'fillPriority', 'color');

    /**
     * set fill priority
     * @name setFillPriority
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} priority can be color, pattern, linear-gradient, or radial-gradient
     *  The default is color.
     */

     /**
     * get fill priority
     * @name getFillPriority
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Shape, 'strokeScaleEnabled', true);

     /**
     * set stroke scale enabled
     * @name setStrokeScaleEnabled
     * @methodOf Kinetic.Shape.prototype
     * @param {Boolean} enabled
     */

     /**
     * get stroke scale enabled
     * @name getStrokeScaleEnabled
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Shape, 'fillPatternOffset', 0);

    /**
     * set fill pattern offset
     * @name setFillPatternOffset
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} offset
     */

     /**
     * set fill pattern offset x
     * @name setFillPatternOffsetX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * set fill pattern offset y
     * @name setFillPatternOffsetY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

     /**
     * get fill pattern offset
     * @name getFillPatternOffset
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill pattern offset x
     * @name getFillPatternOffsetX
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill pattern offset y
     * @name getFillPatternOffsetY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Shape, 'fillPatternScale', 1);

    /**
     * set fill pattern scale
     * @name setFillPatternScale
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} scale
     */

     /**
     * set fill pattern scale x
     * @name setFillPatternScaleX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * set fill pattern scale y
     * @name setFillPatternScaleY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

     /**
     * get fill pattern scale
     * @name getFillPatternScale
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill pattern scale x
     * @name getFillPatternScaleX
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill pattern scale y
     * @name getFillPatternScaleY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Shape, 'fillLinearGradientStartPoint', 0);

    /**
     * set fill linear gradient start point
     * @name setFillLinearGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} startPoint
     */

     /**
     * set fill linear gradient start point x
     * @name setFillLinearGradientStartPointX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * set fill linear gradient start point y
     * @name setFillLinearGradientStartPointY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

     /**
     * get fill linear gradient start point
     * @name getFillLinearGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill linear gradient start point x
     * @name getFillLinearGradientStartPointX
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill linear gradient start point y
     * @name getFillLinearGradientStartPointY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Shape, 'fillLinearGradientEndPoint', 0);

    /**
     * set fill linear gradient end point
     * @name setFillLinearGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} endPoint
     */

     /**
     * set fill linear gradient end point x
     * @name setFillLinearGradientEndPointX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * set fill linear gradient end point y
     * @name setFillLinearGradientEndPointY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

     /**
     * get fill linear gradient end point
     * @name getFillLinearGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill linear gradient end point x
     * @name getFillLinearGradientEndPointX
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill linear gradient end point y
     * @name getFillLinearGradientEndPointY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Shape, 'fillRadialGradientStartPoint', 0);

    /**
     * set fill radial gradient start point
     * @name setFillRadialGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} startPoint
     */

     /**
     * set fill radial gradient start point x
     * @name setFillRadialGradientStartPointX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * set fill radial gradient start point y
     * @name setFillRadialGradientStartPointY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

     /**
     * get fill radial gradient start point
     * @name getFillRadialGradientStartPoint
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill radial gradient start point x
     * @name getFillRadialGradientStartPointX
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill radial gradient start point y
     * @name getFillRadialGradientStartPointY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Shape, 'fillRadialGradientEndPoint', 0);

    /**
     * set fill radial gradient end point
     * @name setFillRadialGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} endPoint
     */

     /**
     * set fill radial gradient end point x
     * @name setFillRadialGradientEndPointX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * set fill radial gradient end point y
     * @name setFillRadialGradientEndPointY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

     /**
     * get fill radial gradient end point
     * @name getFillRadialGradientEndPoint
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill radial gradient end point x
     * @name getFillRadialGradientEndPointX
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get fill radial gradient end point y
     * @name getFillRadialGradientEndPointY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addPointGetterSetter(Kinetic.Shape, 'shadowOffset', 0);

    /**
     * set shadow offset
     * @name setShadowOffset
     * @methodOf Kinetic.Shape.prototype
     * @param {Number|Array|Object} offset
     */

     /**
     * set shadow offset x
     * @name setShadowOffsetX
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} x
     */

     /**
     * set shadow offset y
     * @name setShadowOffsetY
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} y
     */

    /**
     * get shadow offset
     * @name getShadowOffset
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get shadow offset x
     * @name getShadowOffsetX
     * @methodOf Kinetic.Shape.prototype
     */

     /**
     * get shadow offset y
     * @name getShadowOffsetY
     * @methodOf Kinetic.Shape.prototype
     */

    Kinetic.Node.addRotationGetterSetter(Kinetic.Shape, 'fillPatternRotation', 0);

    /**
     * set fill pattern rotation in radians
     * @name setFillPatternRotation
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} rotation
     */

    /**
     * set fill pattern rotation in degrees
     * @name setFillPatternRotationDeg
     * @methodOf Kinetic.Shape.prototype
     * @param {Number} rotationDeg
     */

    /**
     * get fill pattern rotation in radians
     * @name getFillPatternRotation
     * @methodOf Kinetic.Shape.prototype
     */

    /**
     * get fill pattern rotation in degrees
     * @name getFillPatternRotationDeg
     * @methodOf Kinetic.Shape.prototype
     */

})();

(function() {
    // CONSTANTS
    var STAGE = 'Stage',
        STRING = 'string',
        PX = 'px',
        MOUSEOUT = 'mouseout',
        MOUSELEAVE = 'mouseleave',
        MOUSEOUT = 'mouseout',
        MOUSEOVER = 'mouseover',
        MOUSEENTER = 'mouseenter',
        MOUSEMOVE = 'mousemove',
        MOUSEDOWN = 'mousedown',
        MOUSEUP = 'mouseup',
        CLICK = 'click',
        DBL_CLICK = 'dblclick',
        TOUCHSTART = 'touchstart'
        TOUCHEND = 'touchend'
        TAP = 'tap',
        DBL_TAP = 'dbltap',
        TOUCHMOVE = 'touchmove',
        DIV = 'div',
        RELATIVE = 'relative',
        INLINE_BLOCK = 'inline-block',
        KINETICJS_CONTENT = 'kineticjs-content',
        SPACE = ' ',
        CONTAINER = 'container',
        EVENTS = [MOUSEDOWN, MOUSEMOVE, MOUSEUP, MOUSEOUT, TOUCHSTART, TOUCHMOVE, TOUCHEND],
        
    // cached variables
    eventsLength = EVENTS.length;

    function addEvent(ctx, eventName) {
      ctx.content.addEventListener(eventName, function(evt) {
        ctx['_' + eventName](evt);
      }, false);
    }

    Kinetic.Util.addMethods(Kinetic.Stage, {
        _initStage: function(config) {
            this.createAttrs();
            // call super constructor
            Kinetic.Container.call(this, config);
            this.nodeType = STAGE;
            this.dblClickWindow = 400;
            this._id = Kinetic.Global.idCounter++;
            this._buildDOM();
            this._bindContentEvents();
            Kinetic.Global.stages.push(this);
        },
        /**
         * set container dom element which contains the stage wrapper div element
         * @name setContainer
         * @methodOf Kinetic.Stage.prototype
         * @param {DomElement} container can pass in a dom element or id string
         */
        setContainer: function(container) {
            if( typeof container === STRING) {
                container = document.getElementById(container);
            }
            this.setAttr(CONTAINER, container);
        },
        draw: function() {
            // clear children layers
            var children = this.getChildren(), 
                len = children.length,
                n, layer;
            
            for(n = 0; n < len; n++) {
                layer = children[n];
                if (layer.getClearBeforeDraw()) {
                    layer.getCanvas().clear();
                    layer.getHitCanvas().clear();
                }
            }
          
            Kinetic.Node.prototype.draw.call(this);
        },
        /**
         * draw layer scene graphs
         * @name draw
         * @methodOf Kinetic.Stage.prototype
         */

        /**
         * draw layer hit graphs
         * @name drawHit
         * @methodOf Kinetic.Stage.prototype
         */

        /**
         * set height
         * @name setHeight
         * @methodOf Kinetic.Stage.prototype
         * @param {Number} height
         */
        setHeight: function(height) {
            Kinetic.Node.prototype.setHeight.call(this, height);
            this._resizeDOM();
        },
        /**
         * set width
         * @name setWidth
         * @methodOf Kinetic.Stage.prototype
         * @param {Number} width
         */
        setWidth: function(width) {
            Kinetic.Node.prototype.setWidth.call(this, width);
            this._resizeDOM();
        },
        /**
         * clear all layers
         * @name clear
         * @methodOf Kinetic.Stage.prototype
         */
        clear: function() {
            var layers = this.children,
                len = layers.length,
                n;
                
            for(n = 0; n < len; n++) {
                layers[n].clear();
            }
        },
        /**
         * remove stage
         */
        remove: function() {
            var content = this.content;
            Kinetic.Node.prototype.remove.call(this);

            if(content && Kinetic.Util._isInDocument(content)) {
                this.getContainer().removeChild(content);
            }
        },
        /**
         * get mouse position for desktop apps
         * @name getMousePosition
         * @methodOf Kinetic.Stage.prototype
         */
        getMousePosition: function() {
            return this.mousePos;
        },
        /**
         * get touch position for mobile apps
         * @name getTouchPosition
         * @methodOf Kinetic.Stage.prototype
         */
        getTouchPosition: function() {
            return this.touchPos;
        },
        /**
         * get pointer position which can be a touc position or mouse position
         * @name getPointerPosition
         * @methodOf Kinetic.Stage.prototype
         */
        getPointerPosition: function() {
            return this.getTouchPosition() || this.getMousePosition();
        },
        getStage: function() {
            return this;
        },
        /**
         * get stage content div element which has the
         *  the class name "kineticjs-content"
         * @name getContent
         * @methodOf Kinetic.Stage.prototype
         */
        getContent: function() {
            return this.content;
        },
        /**
         * Creates a composite data URL and requires a callback because the composite is generated asynchronously.
         * @name toDataURL
         * @methodOf Kinetic.Stage.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toDataURL: function(config) {
            var config = config || {},
                mimeType = config.mimeType || null, 
                quality = config.quality || null, 
                x = config.x || 0, 
                y = config.y || 0, 
                canvas = new Kinetic.SceneCanvas({
                    width: config.width || this.getWidth(), 
                    height: config.height || this.getHeight(),
                    pixelRatio: 1
                }), 
                context = canvas.getContext(), 
                layers = this.children;

            if(x || y) {
                context.translate(-1 * x, -1 * y);
            }

            function drawLayer(n) {
                var layer = layers[n],
                    layerUrl = layer.toDataURL(),
                    imageObj = new Image();
                    
                imageObj.onload = function() {
                    context.drawImage(imageObj, 0, 0);

                    if(n < layers.length - 1) {
                        drawLayer(n + 1);
                    }
                    else {
                        config.callback(canvas.toDataURL(mimeType, quality));
                    }
                };
                imageObj.src = layerUrl;
            }
            drawLayer(0);
        },
        /**
         * converts stage into an image.
         * @name toImage
         * @methodOf Kinetic.Stage.prototype
         * @param {Object} config
         * @param {Function} config.callback function executed when the composite has completed
         * @param {String} [config.mimeType] can be "image/png" or "image/jpeg".
         *  "image/png" is the default
         * @param {Number} [config.x] x position of canvas section
         * @param {Number} [config.y] y position of canvas section
         * @param {Number} [config.width] width of canvas section
         * @param {Number} [config.height] height of canvas section
         * @param {Number} [config.quality] jpeg quality.  If using an "image/jpeg" mimeType,
         *  you can specify the quality from 0 to 1, where 0 is very poor quality and 1
         *  is very high quality
         */
        toImage: function(config) {
            var cb = config.callback;

            config.callback = function(dataUrl) {
                Kinetic.Util._getImage(dataUrl, function(img) {
                    cb(img);
                });
            };
            this.toDataURL(config);
        },
        /**
         * get intersection object that contains shape and pixel data
         * @name getIntersection
         * @methodOf Kinetic.Stage.prototype
         * @param {Object} pos point object
         */
        getIntersection: function() {
            var pos = Kinetic.Util._getXY(Array.prototype.slice.call(arguments)),
                layers = this.getChildren(),
                len = layers.length,
                end = len - 1,
                n, obj;

            for(n = end; n >= 0; n--) {
                obj = layers[n].getIntersection(pos);
                if (obj) {
                    return obj;
                }
            }

            return null;
        },
        _resizeDOM: function() {
            if(this.content) {
                var width = this.getWidth(),
                    height = this.getHeight(),
                    layers = this.getChildren(),
                    len = layers.length,
                    n;

                // set content dimensions
                this.content.style.width = width + PX;
                this.content.style.height = height + PX;

                this.bufferCanvas.setSize(width, height, 1);
                this.hitCanvas.setSize(width, height);
                
                // set pointer defined layer dimensions
                for(n = 0; n < len; n++) {
                    layer = layers[n];
                    layer.getCanvas().setSize(width, height);
                    layer.hitCanvas.setSize(width, height);
                    layer.draw();
                }
            }
        },
        /**
         * add layer to stage
         * @param {Kinetic.Layer} layer
         */
        add: function(layer) {
            Kinetic.Container.prototype.add.call(this, layer);
            layer.canvas.setSize(this.attrs.width, this.attrs.height);
            layer.hitCanvas.setSize(this.attrs.width, this.attrs.height);

            // draw layer and append canvas to container
            layer.draw();
            this.content.appendChild(layer.canvas.element);
            
            // chainable
            return this;
        },
        getParent: function() {
            return null;
        },
        getLayer: function() {
            return null;
        },
        /**
         * get layers
         * @name getLayers
         * @methodOf Kinetic.Stage.prototype
         */
         getLayers: function() {
             return this.getChildren();
         },
        _setPointerPosition: function(evt) {
            if(!evt) {
                evt = window.event;
            }
            this._setMousePosition(evt);
            this._setTouchPosition(evt);
        },
        /**
         * begin listening for events by adding event handlers
         * to the container
         */
        _bindContentEvents: function() {
            var that = this,
                n;

            for (n = 0; n < eventsLength; n++) {
              addEvent(this, EVENTS[n]);
            }
        },
        _mouseout: function(evt) {
            this._setPointerPosition(evt);
            var go = Kinetic.Global,
                targetShape = this.targetShape;
                
            if(targetShape && !go.isDragging()) {
                targetShape._handleEvent(MOUSEOUT, evt);
                targetShape._handleEvent(MOUSELEAVE, evt);
                this.targetShape = null;
            }
            this.mousePos = undefined;
        },
        _mousemove: function(evt) {
            this._setPointerPosition(evt);
            var go = Kinetic.Global,
                dd = Kinetic.DD,
                obj = this.getIntersection(this.getPointerPosition()),
                shape;

            if(obj) {
                shape = obj.shape;
                if(shape) {
                    if(!go.isDragging() && obj.pixel[3] === 255 && (!this.targetShape || this.targetShape._id !== shape._id)) {
                        if(this.targetShape) {
                            this.targetShape._handleEvent(MOUSEOUT, evt, shape);
                            this.targetShape._handleEvent(MOUSELEAVE, evt, shape);
                        }
                        shape._handleEvent(MOUSEOVER, evt, this.targetShape);
                        shape._handleEvent(MOUSEENTER, evt, this.targetShape);
                        this.targetShape = shape;
                    }
                    else {
                        shape._handleEvent(MOUSEMOVE, evt);
                    }
                }
            }
            /*
             * if no shape was detected, clear target shape and try
             * to run mouseout from previous target shape
             */
            else if(this.targetShape && !go.isDragging()) {
                this.targetShape._handleEvent(MOUSEOUT, evt);
                this.targetShape._handleEvent(MOUSELEAVE, evt);
                this.targetShape = null;
            }

            if(dd) {
                dd._drag(evt);
            }
        },
        _mousedown: function(evt) {
            this._setPointerPosition(evt);
        	var go = Kinetic.Global,
        	    obj = this.getIntersection(this.getPointerPosition()), 
        	    shape;

            if(obj && obj.shape) {
                shape = obj.shape;
                this.clickStart = true;
                this.clickStartShape = shape;
                shape._handleEvent(MOUSEDOWN, evt);
            }

            //init stage drag and drop
            if(this.isDraggable() && !go.isDragReady()) {
                this.startDrag(evt);
            }
        },
        _mouseup: function(evt) {
            this._setPointerPosition(evt);
            var that = this, 
                go = Kinetic.Global, 
                obj = this.getIntersection(this.getPointerPosition()),
                shape;
                
            if(obj && obj.shape) {
                shape = obj.shape;
                shape._handleEvent(MOUSEUP, evt);

                // detect if click or double click occurred
                if(this.clickStart) {
                    /*
                     * if dragging and dropping, or if click doesn't map to 
                     * the correct shape, don't fire click or dbl click event
                     */
                    if(!go.isDragging() && shape._id === this.clickStartShape._id) {
                        shape._handleEvent(CLICK, evt);

                        if(this.inDoubleClickWindow) {
                            shape._handleEvent(DBL_CLICK, evt);
                        }
                        this.inDoubleClickWindow = true;
                        setTimeout(function() {
                            that.inDoubleClickWindow = false;
                        }, this.dblClickWindow);
                    }
                }
            }
            this.clickStart = false;
        },
        _touchstart: function(evt) {
          this._setPointerPosition(evt);
        	var go = Kinetic.Global,
        	    obj = this.getIntersection(this.getPointerPosition()), 
        	    shape;
            
            evt.preventDefault();

            if(obj && obj.shape) {
                shape = obj.shape;
                this.tapStart = true;
                this.tapStartShape = shape;
                shape._handleEvent(TOUCHSTART, evt);
            }

            //init stage drag and drop
            if(this.isDraggable() && !go.isDragReady()) {
                this.startDrag(evt);
            }
        },
        _touchend: function(evt) {
            this._setPointerPosition(evt);
            var that = this, 
                go = Kinetic.Global, 
                obj = this.getIntersection(this.getPointerPosition()),
                shape;

            if(obj && obj.shape) {
                shape = obj.shape;
                shape._handleEvent(TOUCHEND, evt);

                // detect if tap or double tap occurred
                if(this.tapStart) {
                    /*
                     * if dragging and dropping, don't fire tap or dbltap
                     * event
                     */
                    if(!go.isDragging() && shape._id === this.tapStartShape._id) {
                        shape._handleEvent(TAP, evt);

                        if(this.inDoubleClickWindow) {
                            shape._handleEvent(DBL_TAP, evt);
                        }
                        this.inDoubleClickWindow = true;
                        setTimeout(function() {
                            that.inDoubleClickWindow = false;
                        }, this.dblClickWindow);
                    }
                }
            }

            this.tapStart = false;
        },
        _touchmove: function(evt) {
            this._setPointerPosition(evt);
            var dd = Kinetic.DD,
                obj = this.getIntersection(this.getPointerPosition()),
                shape;
            
            evt.preventDefault();
            
            if(obj && obj.shape) {
                shape = obj.shape;
                shape._handleEvent(TOUCHMOVE, evt);
            }

            // start drag and drop
            if(dd) {
                dd._drag(evt);
            }
        },
        /**
         * set mouse positon for desktop apps
         * @param {Event} evt
         */
        _setMousePosition: function(evt) {
            var mouseX = evt.clientX - this._getContentPosition().left,
                mouseY = evt.clientY - this._getContentPosition().top;
                
            this.mousePos = {
                x: mouseX,
                y: mouseY
            };
        },
        /**
         * set touch position for mobile apps
         * @param {Event} evt
         */
        _setTouchPosition: function(evt) {
            var touch, touchX, touchY;
            
            if(evt.touches !== undefined && evt.touches.length === 1) {
                // one finger
                touch = evt.touches[0];
                
                // get the information for finger #1
                touchX = touch.clientX - this._getContentPosition().left;
                touchY = touch.clientY - this._getContentPosition().top;

                this.touchPos = {
                    x: touchX,
                    y: touchY
                };
            }
        },
        /**
         * get container position
         */
        _getContentPosition: function() {
            var rect = this.content.getBoundingClientRect();
            return {
                top: rect.top,
                left: rect.left
            };
        },
        /**
         * build dom
         */
        _buildDOM: function() {
            // content
            this.content = document.createElement(DIV);
            this.content.style.position = RELATIVE;
            this.content.style.display = INLINE_BLOCK;
            this.content.className = KINETICJS_CONTENT;
            this.attrs.container.appendChild(this.content);

            this.bufferCanvas = new Kinetic.SceneCanvas();
            this.hitCanvas = new Kinetic.HitCanvas();

            this._resizeDOM();
        },
        /**
         * bind event listener to container DOM element
         * @param {String} typesStr
         * @param {function} handler
         */
        _onContent: function(typesStr, handler) {
            var types = typesStr.split(SPACE),
                len = types.length,
                n, baseEvent;
                
            for(n = 0; n < len; n++) {
                baseEvent = types[n];
                this.content.addEventListener(baseEvent, handler, false);
            }
        }
    });
    Kinetic.Util.extend(Kinetic.Stage, Kinetic.Container);

    // add getters and setters
    Kinetic.Node.addGetter(Kinetic.Stage, 'container');

    /**
     * get container DOM element
     * @name getContainer
     * @methodOf Kinetic.Stage.prototype
     */
})();

(function() {
    Kinetic.Util.addMethods(Kinetic.Layer, {
        _initLayer: function(config) {
            this.nodeType = 'Layer';
            this.createAttrs();
            // call super constructor
            Kinetic.Container.call(this, config);

            this.canvas = new Kinetic.SceneCanvas();
            this.canvas.getElement().style.position = 'absolute';
            this.hitCanvas = new Kinetic.HitCanvas();
        },
        /**
         * get intersection object that contains shape and pixel data
         * @name getIntersection
         * @methodOf Kinetic.Layer.prototype
         */
        getIntersection: function() {
            var pos = Kinetic.Util._getXY(Array.prototype.slice.call(arguments)),
                p, colorKey, shape;

            if(this.isVisible() && this.isListening()) {
                p = this.hitCanvas.context.getImageData(pos.x | 0, pos.y | 0, 1, 1).data;
                // this indicates that a hit pixel may have been found
                if(p[3] === 255) {
                    colorKey = Kinetic.Util._rgbToHex(p[0], p[1], p[2]);
                    shape = Kinetic.Global.shapes[colorKey];
                    return {
                        shape: shape,
                        pixel: p
                    };
                }
                // if no shape mapped to that pixel, return pixel array
                else if(p[0] > 0 || p[1] > 0 || p[2] > 0 || p[3] > 0) {
                    return {
                        pixel: p
                    };
                }
            }

            return null;
        },
        drawScene: function(canvas) {
            var canvas = canvas || this.getCanvas();

            if(this.getClearBeforeDraw()) {
                canvas.clear();
            }

            Kinetic.Container.prototype.drawScene.call(this, canvas);
        },
        drawHit: function() {
            var layer = this.getLayer();
            
            if(layer && layer.getClearBeforeDraw()) {
                layer.getHitCanvas().clear();
            }

            Kinetic.Container.prototype.drawHit.call(this);
        },
        /**
         * get layer canvas
         * @name getCanvas
         * @methodOf Kinetic.Layer.prototype
         */
        getCanvas: function() {
            return this.canvas;     
        },
        /**
         * get layer hit canvas
         * @name getHitCanvas
         * @methodOf Kinetic.Layer.prototype
         */
        getHitCanvas: function() {
            return this.hitCanvas;
        },
        /**
         * get layer canvas context
         * @name getContext
         * @methodOf Kinetic.Layer.prototype
         */
        getContext: function() {
            return this.getCanvas().getContext(); 
        },
        /**
         * clear canvas tied to the layer
         * @name clear
         * @methodOf Kinetic.Layer.prototype
         */
        clear: function() {
            this.getCanvas().clear();
        },
        // extenders
        setVisible: function(visible) {
            Kinetic.Node.prototype.setVisible.call(this, visible);
            if(visible) {
                this.getCanvas().element.style.display = 'block';
                this.hitCanvas.element.style.display = 'block';
            }
            else {
                this.getCanvas().element.style.display = 'none';
                this.hitCanvas.element.style.display = 'none';
            }
        },
        setZIndex: function(index) {
            Kinetic.Node.prototype.setZIndex.call(this, index);
            var stage = this.getStage();
            if(stage) {
                stage.content.removeChild(this.getCanvas().element);

                if(index < stage.getChildren().length - 1) {
                    stage.content.insertBefore(this.getCanvas().element, stage.getChildren()[index + 1].getCanvas().element);
                }
                else {
                    stage.content.appendChild(this.getCanvas().element);
                }
            }
        },
        moveToTop: function() {
            Kinetic.Node.prototype.moveToTop.call(this);
            var stage = this.getStage();
            if(stage) {
                stage.content.removeChild(this.getCanvas().element);
                stage.content.appendChild(this.getCanvas().element);
            }
        },
        moveUp: function() {
            if(Kinetic.Node.prototype.moveUp.call(this)) {
                var stage = this.getStage();
                if(stage) {
                    stage.content.removeChild(this.getCanvas().element);

                    if(this.index < stage.getChildren().length - 1) {
                        stage.content.insertBefore(this.getCanvas().element, stage.getChildren()[this.index + 1].getCanvas().element);
                    }
                    else {
                        stage.content.appendChild(this.getCanvas().element);
                    }
                }
            }
        },
        moveDown: function() {
            if(Kinetic.Node.prototype.moveDown.call(this)) {
                var stage = this.getStage();
                if(stage) {
                    var children = stage.getChildren();
                    stage.content.removeChild(this.getCanvas().element);
                    stage.content.insertBefore(this.getCanvas().element, children[this.index + 1].getCanvas().element);
                }
            }
        },
        moveToBottom: function() {
            if(Kinetic.Node.prototype.moveToBottom.call(this)) {
                var stage = this.getStage();
                if(stage) {
                    var children = stage.getChildren();
                    stage.content.removeChild(this.getCanvas().element);
                    stage.content.insertBefore(this.getCanvas().element, children[1].getCanvas().element);
                }
            }
        },
        getLayer: function() {
            return this;
        },
        /**
         * remove layer from stage
         */
        remove: function() {
            var stage = this.getStage(), canvas = this.getCanvas(), element = canvas.element;
            Kinetic.Node.prototype.remove.call(this);

            if(stage && canvas && Kinetic.Util._isInDocument(element)) {
                stage.content.removeChild(element);
            }
        }
    });
    Kinetic.Util.extend(Kinetic.Layer, Kinetic.Container);

    // add getters and setters
    Kinetic.Node.addGetterSetter(Kinetic.Layer, 'clearBeforeDraw', true);

    /**
     * set flag which determines if the layer is cleared or not
     *  before drawing
     * @name setClearBeforeDraw
     * @methodOf Kinetic.Layer.prototype
     * @param {Boolean} clearBeforeDraw
     */

    /**
     * get flag which determines if the layer is cleared or not
     *  before drawing
     * @name getClearBeforeDraw
     * @methodOf Kinetic.Layer.prototype
     */
})();

(function() {
    Kinetic.Util.addMethods(Kinetic.Group, {
        _initGroup: function(config) {
            this.nodeType = 'Group';
            this.createAttrs();
            // call super constructor
            Kinetic.Container.call(this, config);
        }
    });
    Kinetic.Util.extend(Kinetic.Group, Kinetic.Container);
})();

(function() {
    /**
     * Rect constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} [config.cornerRadius]
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Rect = function(config) {
        this._initRect(config);
    };
    
    Kinetic.Rect.prototype = {
        _initRect: function(config) {
            this.createAttrs();
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Rect';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(),
                cornerRadius = this.getCornerRadius(), 
                width = this.getWidth(), 
                height = this.getHeight();
                
            context.beginPath();
            
            if(!cornerRadius) {
                // simple rect - don't bother doing all that complicated maths stuff.
                context.rect(0, 0, width, height);
            }
            else {
                // arcTo would be nicer, but browser support is patchy (Opera)
                context.moveTo(cornerRadius, 0);
                context.lineTo(width - cornerRadius, 0);
                context.arc(width - cornerRadius, cornerRadius, cornerRadius, Math.PI * 3 / 2, 0, false);
                context.lineTo(width, height - cornerRadius);
                context.arc(width - cornerRadius, height - cornerRadius, cornerRadius, 0, Math.PI / 2, false);
                context.lineTo(cornerRadius, height);
                context.arc(cornerRadius, height - cornerRadius, cornerRadius, Math.PI / 2, Math.PI, false);
                context.lineTo(0, cornerRadius);
                context.arc(cornerRadius, cornerRadius, cornerRadius, Math.PI, Math.PI * 3 / 2, false);
            }
            context.closePath();
            canvas.fillStroke(this);
        }
    };

    Kinetic.Util.extend(Kinetic.Rect, Kinetic.Shape);

    Kinetic.Node.addGetterSetter(Kinetic.Rect, 'cornerRadius', 0);

    /**
     * set corner radius
     * @name setCornerRadius
     * @methodOf Kinetic.Rect.prototype
     * @param {Number} corner radius
     */

    /**
     * get corner radius
     * @name getCornerRadius
     * @methodOf Kinetic.Rect.prototype
     */

})();

(function() {
    /**
     * Circle constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} config.radius
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Circle = function(config) {
        this._initCircle(config);
    };

    Kinetic.Circle.prototype = {
        _initCircle: function(config) {
            this.createAttrs();
            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Circle';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
        	  var context = canvas.getContext();
            context.beginPath();
            context.arc(0, 0, this.getRadius(), 0, Math.PI * 2, true);
            context.closePath();
            canvas.fillStroke(this);
        },
        getWidth: function() {
            return this.getRadius() * 2;
        },
        getHeight: function() {
            return this.getRadius() * 2;
        },
        setWidth: function(width) {
            Kinetic.Node.prototype.setWidth.call(this, width);
            this.setRadius(width / 2);
        },
        setHeight: function(height) {
            Kinetic.Node.prototype.setHeight.call(this, height);
            this.setRadius(height / 2);
        }
    };
    Kinetic.Util.extend(Kinetic.Circle, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Circle, 'radius', 0);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.Circle.prototype
     * @param {Number} radius
     */

    /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.Circle.prototype
     */
})();

(function() {
    /**
     * Wedge constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} config.angle
     * @param {Number} config.angleDeg angle in degrees
     * @param {Number} config.radius
     * @param {Boolean} [config.clockwise]
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Wedge = function(config) {
        this._initWedge(config);
    };

    Kinetic.Wedge.prototype = {
        _initWedge: function(config) {
            this.createAttrs();

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Wedge';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext();
            context.beginPath();
            context.arc(0, 0, this.getRadius(), 0, this.getAngle(), this.getClockwise());
            context.lineTo(0, 0);
            context.closePath();
            canvas.fillStroke(this);
        },
        setAngleDeg: function(deg) {
            this.setAngle(Kinetic.Util._degToRad(deg));
        },
        getAngleDeg: function() {
            return Kinetic.Util._radToDeg(this.getAngle());
        }
    };
    Kinetic.Util.extend(Kinetic.Wedge, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Wedge, 'radius', 0);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.Wedge.prototype
     * @param {Number} radius
     */

     /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.Wedge.prototype
     */

    Kinetic.Node.addRotationGetterSetter(Kinetic.Wedge, 'angle', 0);

    /**
     * set angle
     * @name setAngle
     * @methodOf Kinetic.Wedge.prototype
     * @param {Number} angle
     */

     /**
     * set angle in degrees
     * @name setAngleDeg
     * @methodOf Kinetic.Wedge.prototype
     * @param {Number} angleDeg
     */

     /**
     * get angle
     * @name getAngle
     * @methodOf Kinetic.Wedge.prototype
     */

     /**
     * get angle in degrees
     * @name getAngleDeg
     * @methodOf Kinetic.Wedge.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Wedge, 'clockwise', false);

    /**
     * set clockwise draw direction.  If set to true, the wedge will be drawn clockwise
     *  If set to false, the wedge will be drawn anti-clockwise.  The default is false.
     * @name setClockwise
     * @methodOf Kinetic.Wedge.prototype
     * @param {Boolean} clockwise
     */

    /**
     * get clockwise
     * @name getClockwise
     * @methodOf Kinetic.Wedge.prototype
     */
})();

(function() {
    /**
     * Ellipse constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number|Array|Object} config.radius defines x and y radius
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Ellipse = function(config) {
        this._initEllipse(config);
    };

    Kinetic.Ellipse.prototype = {
        _initEllipse: function(config) {
            this.createAttrs();

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Ellipse';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), r = this.getRadius();
            context.beginPath();
            context.save();
            if(r.x !== r.y) {
                context.scale(1, r.y / r.x);
            }
            context.arc(0, 0, r.x, 0, Math.PI * 2, true);
            context.restore();
            context.closePath();
            canvas.fillStroke(this);
        },
        getWidth: function() {
            return this.getRadius().x * 2;
        },
        getHeight: function() {
            return this.getRadius().y * 2;
        },
        setWidth: function(width) {
            Kinetic.Node.prototype.setWidth.call(this, width);
            this.setRadius({
                x: width / 2
            });
        },
        setHeight: function(height) {
            Kinetic.Node.prototype.setHeight.call(this, height);
            this.setRadius({
                y: height / 2
            });
        }
    };
    Kinetic.Util.extend(Kinetic.Ellipse, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addPointGetterSetter(Kinetic.Ellipse, 'radius', 0);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.Ellipse.prototype
     * @param {Object|Array} radius
     *  radius can be a number, in which the ellipse becomes a circle,
     *  it can be an object with an x and y component, or it
     *  can be an array in which the first element is the x component
     *  and the second element is the y component.  The x component
     *  defines the horizontal radius and the y component
     *  defines the vertical radius
     */

    /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.Ellipse.prototype
     */
})();

(function() {
    // CONSTANTS
    var IMAGE = 'Image',
        CROP = 'crop',
        SET = 'set';
    
    /**
     * Image constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {ImageObject} config.image
     * @param {Object} [config.crop]
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Image = function(config) {
        this._initImage(config);
    };

    Kinetic.Image.prototype = {
        _initImage: function(config) {
            var that = this;
            
            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = IMAGE;
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var width = this.getWidth(), 
                height = this.getHeight(), 
                params, 
                that = this, 
                context = canvas.getContext(),
                crop = this.getCrop(),
                cropX, cropY, cropWidth, cropHeight, image;

            // if a filter is set, and the filter needs to be updated, reapply
            if (this.getFilter() && this._applyFilter) {
                this.applyFilter();
                this._applyFilter = false;
            }

            // NOTE: this.filterCanvas may be set by the above code block
            if (this.filterCanvas) {
                image = this.filterCanvas.getElement();
            }
            else {
                image = this.getImage();
            }

            context.beginPath();
            context.rect(0, 0, width, height);
            context.closePath();
            canvas.fillStroke(this);

            if(image) {
                // if cropping
                if(crop) {
                    cropX = crop.x || 0;
                    cropY = crop.y || 0;
                    cropWidth = crop.width || 0;
                    cropHeight = crop.height || 0;
                    params = [image, cropX, cropY, cropWidth, cropHeight, 0, 0, width, height];
                }
                // no cropping
                else {
                    params = [image, 0, 0, width, height];
                }

                if(this.hasShadow()) {
                    canvas.applyShadow(this, function() {
                        that._drawImage(context, params);
                    });
                }
                else {
                    this._drawImage(context, params);
                }
            }
        },
        drawHitFunc: function(canvas) {
            var width = this.getWidth(), 
                height = this.getHeight(), 
                imageHitRegion = this.imageHitRegion, 
                context = canvas.getContext();

            if(imageHitRegion) {
                context.drawImage(imageHitRegion, 0, 0, width, height);
                context.beginPath();
                context.rect(0, 0, width, height);
                context.closePath();
                canvas.stroke(this);
            }
            else {
                context.beginPath();
                context.rect(0, 0, width, height);
                context.closePath();
                canvas.fillStroke(this);
            }
        },
        /**
         * apply filter
         * @name applyFilter
         * @methodOf Kinetic.Image.prototype
         */
        applyFilter: function() {
            var image = this.getImage(),
                that = this,
                width = this.getWidth(),
                height = this.getHeight(),
                filter = this.getFilter(),
                filterCanvas, context, imageData;

            if (this.filterCanvas){
                filterCanvas = this.filterCanvas;
            }
            else {
                filterCanvas = this.filterCanvas = new Kinetic.SceneCanvas({
                    width: width, 
                    height: height
                });
            }

            context = filterCanvas.getContext();

            try {
                this._drawImage(context, [image, 0, 0, width, height]);
                imageData = context.getImageData(0, 0, filterCanvas.getWidth(), filterCanvas.getHeight());
                filter.call(this, imageData);
                context.putImageData(imageData, 0, 0);
            }
            catch(e) {
                this.clearFilter();
                Kinetic.Util.warn('Unable to apply filter. ' + e.message);
            }
        },
        /**
         * clear filter
         * @name clearFilter
         * @methodOf Kinetic.Image.prototype
         */
        clearFilter: function() {
            this.filterCanvas = null;
            this._applyFilter = false;
        },
        /**
         * set crop
         * @name setCrop
         * @methodOf Kinetic.Image.prototype
         * @param {Object|Array} config
         * @param {Number} config.x
         * @param {Number} config.y
         * @param {Number} config.width
         * @param {Number} config.height
         */
        setCrop: function() {
            var config = [].slice.call(arguments),
                pos = Kinetic.Util._getXY(config),
                size = Kinetic.Util._getSize(config),
                both = Kinetic.Util._merge(pos, size);
                
            this.setAttr(CROP, Kinetic.Util._merge(both, this.getCrop()));
        },
        /**
         * create image hit region which enables more accurate hit detection mapping of the image
         *  by avoiding event detections for transparent pixels
         * @name createImageHitRegion
         * @methodOf Kinetic.Image.prototype
         * @param {Function} [callback] callback function to be called once
         *  the image hit region has been created
         */
        createImageHitRegion: function(callback) {
            var that = this,
                width = this.getWidth(),
                height = this.getHeight(),
                canvas = new Kinetic.Canvas({
                    width: width,
                    height: height
                }),
                context = canvas.getContext(),
                image = this.getImage(),
                imageData, data, rgbColorKey, i, n;
                
            context.drawImage(image, 0, 0);
             
            try {
                imageData = context.getImageData(0, 0, width, height);
                data = imageData.data;
                rgbColorKey = Kinetic.Util._hexToRgb(this.colorKey);
                
                // replace non transparent pixels with color key
                for(i = 0, n = data.length; i < n; i += 4) {
                    if (data[i + 3] > 0) {
                        data[i] = rgbColorKey.r;
                        data[i + 1] = rgbColorKey.g;
                        data[i + 2] = rgbColorKey.b;
                    }
                }

                Kinetic.Util._getImage(imageData, function(imageObj) {
                    that.imageHitRegion = imageObj;
                    if(callback) {
                        callback();
                    }
                });
            }
            catch(e) {
                Kinetic.Util.warn('Unable to create image hit region. ' + e.message);
            }
        },
        /**
         * clear image hit region
         * @name clearImageHitRegion
         * @methodOf Kinetic.Image.prototype
         */
        clearImageHitRegion: function() {
            delete this.imageHitRegion;
        },
        getWidth: function() {
            var image = this.getImage(); 
            return this.attrs.width || (image ? image.width : 0); 
        },
        getHeight: function() {
            var image = this.getImage();
            return this.attrs.height || (image ? image.height : 0);
        },
        _drawImage: function(context, a) {
            if(a.length === 5) {
                context.drawImage(a[0], a[1], a[2], a[3], a[4]);
            }
            else if(a.length === 9) {
                context.drawImage(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8]);
            }
        }
    };
    Kinetic.Util.extend(Kinetic.Image, Kinetic.Shape);


    Kinetic.Node.addFilterGetterSetter = function(constructor, attr, def) {
        this.addGetter(constructor, attr, def);
        this.addFilterSetter(constructor, attr);
    };

    Kinetic.Node.addFilterSetter = function(constructor, attr) {
        var that = this,
            method = SET + Kinetic.Util._capitalize(attr);
            
        constructor.prototype[method] = function(val) {
            this.setAttr(attr, val);
            this._applyFilter = true;
        };
    };

    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Image, 'image');

    /**
     * set image
     * @name setImage
     * @methodOf Kinetic.Image.prototype
     * @param {ImageObject} image
     */

    /**
     * get image
     * @name getImage
     * @methodOf Kinetic.Image.prototype
     */
     
    Kinetic.Node.addGetter(Kinetic.Image, 'crop');

    /**
     * get crop
     * @name getCrop
     * @methodOf Kinetic.Image.prototype
     */

     Kinetic.Node.addFilterGetterSetter(Kinetic.Image, 'filter');

     /**
     * set filter
     * @name setFilter
     * @methodOf Kinetic.Image.prototype
     * @param {Function} filter
     */

    /**
     * get filter
     * @name getFilter
     * @methodOf Kinetic.Image.prototype
     */
})();

(function() {
    /**
     * Polygon constructor.&nbsp; Polygons are defined by an array of points
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Polygon = function(config) {
        this._initPolygon(config);
    };

    Kinetic.Polygon.prototype = {
        _initPolygon: function(config) {
            this.createAttrs();

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Polygon';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), points = this.getPoints(), length = points.length;
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);
            for(var n = 1; n < length; n++) {
                context.lineTo(points[n].x, points[n].y);
            }
            context.closePath();
            canvas.fillStroke(this);
        },
        /**
         * set points array
         * @name setPoints
         * @methodOf Kinetic.Polygon.prototype
         * @param {Array} can be an array of point objects or an array
         *  of Numbers.  e.g. [{x:1,y:2},{x:3,y:4}] or [1,2,3,4]
         */
        setPoints: function(val) {
            this.setAttr('points', Kinetic.Util._getPoints(val));
        },
        /**
         * get points array
         * @name getPoints
         * @methodOf Kinetic.Polygon.prototype
         */
         // NOTE: cannot use getter method because we need to return a new
         // default array literal each time because arrays are modified by reference
        getPoints: function() {
            return this.attrs.points || [];
        }
    };
    Kinetic.Util.extend(Kinetic.Polygon, Kinetic.Shape);
})();

(function() {
    // constants
    var AUTO = 'auto', 
        CALIBRI = 'Calibri',
        CANVAS = 'canvas', 
        CENTER = 'center',
        CHANGE_KINETIC = 'Change.kinetic',
        CONTEXT_2D = '2d',
        DASH = '-',
        EMPTY_STRING = '', 
        LEFT = 'left',
        NEW_LINE = '\n',
        TEXT = 'text',
        TEXT_UPPER = 'Text', 
        TOP = 'top', 
        MIDDLE = 'middle',
        NORMAL = 'normal',
        PX_SPACE = 'px ',
        SPACE = ' ',
        RIGHT = 'right',
        WORD = 'word',
        CHAR = 'char',
        NONE = 'none',
        ATTR_CHANGE_LIST = ['fontFamily', 'fontSize', 'fontStyle', 'padding', 'align', 'lineHeight', 'text', 'width', 'height', 'wrap'],
        
        // cached variables
        attrChangeListLen = ATTR_CHANGE_LIST.length,
        dummyContext = document.createElement(CANVAS).getContext(CONTEXT_2D);

    /**
     * Text constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} [config.fontFamily] default is Calibri
     * @param {Number} [config.fontSize] in pixels.  Default is 12
     * @param {String} [config.fontStyle] can be normal, bold, or italic.  Default is normal
     * @param {String} config.text
     * @param {String} [config.align] can be left, center, or right
     * @param {Number} [config.padding]
     * @param {Number} [config.width] default is auto
     * @param {Number} [config.height] default is auto
     * @param {Number} [config.lineHeight] default is 1
     * @param {String} [config.wrap] can be word, char, or none. Default is word
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Text = function(config) {
        this._initText(config);
    };
    function _fillFunc(context) {
        context.fillText(this.partialText, 0, 0);
    }
    function _strokeFunc(context) {
        context.strokeText(this.partialText, 0, 0);
    }

    Kinetic.Text.prototype = {
        _initText: function(config) {
            var that = this;
            this.createAttrs();
            
            // since width and height work a bit different for Text,
            // we need to default the values here
            this.attrs.width = AUTO;
            this.attrs.height = AUTO;
            
            // call super constructor
            Kinetic.Shape.call(this, config);

            this.shapeType = TEXT;
            this._fillFunc = _fillFunc;
            this._strokeFunc = _strokeFunc;
            this.shapeType = TEXT_UPPER;
            this._setDrawFuncs();

            // update text data for certain attr changes
            for(var n = 0; n < attrChangeListLen; n++) {
                this.on(ATTR_CHANGE_LIST[n] + CHANGE_KINETIC, that._setTextData);
            }

            this._setTextData();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), 
                p = this.getPadding(), 
                fontStyle = this.getFontStyle(),
                fontSize = this.getFontSize(),
                fontFamily = this.getFontFamily(),
                textHeight = this.getTextHeight(),
                lineHeightPx = this.getLineHeight() * textHeight, 
                textArr = this.textArr,
                textArrLen = textArr.length,
                totalWidth = this.getWidth();

            context.font = this._getContextFont();
            context.textBaseline = MIDDLE;
            context.textAlign = LEFT;
            context.save();
            context.translate(p, 0);
            context.translate(0, p + textHeight / 2);

            // draw text lines
            for(var n = 0; n < textArrLen; n++) {
                var obj = textArr[n],
                    text = obj.text,
                    width = obj.width;

                // horizontal alignment
                context.save();
                if(this.getAlign() === RIGHT) {
                    context.translate(totalWidth - width - p * 2, 0);
                }
                else if(this.getAlign() === CENTER) {
                    context.translate((totalWidth - width - p * 2) / 2, 0);
                }

                this.partialText = text;
                canvas.fillStroke(this);
                context.restore();
                context.translate(0, lineHeightPx);
            }
            context.restore();
        },
        drawHitFunc: function(canvas) {
            var context = canvas.getContext(), 
                width = this.getWidth(), 
                height = this.getHeight();

            context.beginPath();
            context.rect(0, 0, width, height);
            context.closePath();
            canvas.fillStroke(this);
        },
        /**
         * set text
         * @name setText
         * @methodOf Kinetic.Text.prototype
         * @param {String} text
         */
        setText: function(text) {
            var str = Kinetic.Util._isString(text) ? text : text.toString();
            this.setAttr(TEXT, str);
        },
        /**
         * get width
         * @name getWidth
         * @methodOf Kinetic.Text.prototype
         */
        getWidth: function() {
            return this.attrs.width === AUTO ? this.getTextWidth() + this.getPadding() * 2 : this.attrs.width;
        },
        /**
         * get height
         * @name getHeight
         * @methodOf Kinetic.Text.prototype
         */
        getHeight: function() {
            return this.attrs.height === AUTO ? (this.getTextHeight() * this.textArr.length * this.getLineHeight()) + this.getPadding() * 2 : this.attrs.height;
        },
        /**
         * get text width
         * @name getTextWidth
         * @methodOf Kinetic.Text.prototype
         */
        getTextWidth: function() {
            return this.textWidth;
        },
        /**
         * get text height
         * @name getTextHeight
         * @methodOf Kinetic.Text.prototype
         */
        getTextHeight: function() {
            return this.textHeight;
        },
        _getTextSize: function(text) {
            var context = dummyContext,
                fontSize = this.getFontSize(),
                metrics;

            context.save();
            context.font = this._getContextFont();
            
            metrics = context.measureText(text);
            context.restore();
            return {
                width: metrics.width,
                height: parseInt(fontSize, 10)
            };
        },
        _getContextFont: function() {
            return this.getFontStyle() + SPACE + this.getFontSize() + PX_SPACE + this.getFontFamily();
        },
        _addTextLine: function (line, width, height) {
            return this.textArr.push({text: line, width: width});
        },
        _getTextWidth: function (text) {
            return dummyContext.measureText(text).width;
        },
        /**
         * set text data.  wrap logic and width and height setting occurs
         * here
         */
         _setTextData: function () {
             var lines = this.getText().split('\n'),
                 fontSize = +this.getFontSize(),
                 textWidth = 0,
                 lineHeightPx = this.getLineHeight() * fontSize,
                 width = this.attrs.width,
                 height = this.attrs.height,
                 fixedWidth = width !== AUTO,
                 fixedHeight = height !== AUTO,
                 padding = this.getPadding(),
                 maxWidth = width - padding * 2,
                 maxHeightPx = height - padding * 2,
                 currentHeightPx = 0,
                 wrap = this.getWrap(),
                 shouldWrap = wrap !== NONE,
                 wrapAtWord = wrap !==  CHAR && shouldWrap;

             this.textArr = [];
             dummyContext.save();
             dummyContext.font = this.getFontStyle() + SPACE + fontSize + PX_SPACE + this.getFontFamily();
             for (var i = 0, max = lines.length; i < max; ++i) {
                 var line = lines[i],
                     lineWidth = this._getTextWidth(line);
                 if (fixedWidth && lineWidth > maxWidth) {
                     /* 
                      * if width is fixed and line does not fit entirely
                      * break the line into multiple fitting lines
                      */
                     while (line.length > 0) {
                        /*
                         * use binary search to find the longest substring that
                         * that would fit in the specified width
                         */
                         var low = 0, high = line.length,
                             match = '', matchWidth = 0;
                         while (low < high) {
                             var mid = (low + high) >>> 1,
                                 substr = line.slice(0, mid + 1),
                                 substrWidth = this._getTextWidth(substr);
                             if (substrWidth <= maxWidth) {
                                 low = mid + 1;
                                 match = substr;
                                 matchWidth = substrWidth;
                             } else {
                                 high = mid;
                             }
                         }
                         /*
                          * 'low' is now the index of the substring end
                          * 'match' is the substring
                          * 'matchWidth' is the substring width in px
                          */
                         if (match) {
                             // a fitting substring was found
                             if (wrapAtWord) {
                                 // try to find a space or dash where wrapping could be done
                                 var wrapIndex = Math.max(match.lastIndexOf(SPACE),
                                                          match.lastIndexOf(DASH)) + 1;
                                 if (wrapIndex > 0) {
                                     // re-cut the substring found at the space/dash position
                                     low = wrapIndex;
                                     match = match.slice(0, low);
                                     matchWidth = this._getTextWidth(match);
                                 }
                             }
                             this._addTextLine(match, matchWidth);
                             currentHeightPx += lineHeightPx;
                             if (!shouldWrap ||
                                 (fixedHeight && currentHeightPx + lineHeightPx > maxHeightPx)) {
                                 /*
                                  * stop wrapping if wrapping is disabled or if adding
                                  * one more line would overflow the fixed height
                                  */
                                 break;
                             }
                             line = line.slice(low);
                             if (line.length > 0) {
                                 // Check if the remaining text would fit on one line
                                 lineWidth = this._getTextWidth(line);
                                 if (lineWidth <= maxWidth) {
                                     // if it does, add the line and break out of the loop
                                     this._addTextLine(line, lineWidth);
                                     currentHeightPx += lineHeightPx;
                                     break;
                                 }
                             }
                         } else {
                             // not even one character could fit in the element, abort
                             break;
                         }
                     }
                 } else {
                     // element width is automatically adjusted to max line width
                     this._addTextLine(line, lineWidth);
                     currentHeightPx += lineHeightPx;
                     textWidth = Math.max(textWidth, lineWidth);
                 }
                 // if element height is fixed, abort if adding one more line would overflow
                 if (fixedHeight && currentHeightPx + lineHeightPx > maxHeightPx) {
                     break;
                 }
             }
             dummyContext.restore();
             this.textHeight = fontSize;
             this.textWidth = textWidth;
         }
    };
    Kinetic.Util.extend(Kinetic.Text, Kinetic.Shape);
 
    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Text, 'fontFamily', CALIBRI);

    /**
     * set font family
     * @name setFontFamily
     * @methodOf Kinetic.Text.prototype
     * @param {String} fontFamily
     */

     /**
     * get font family
     * @name getFontFamily
     * @methodOf Kinetic.Text.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Text, 'fontSize', 12);

    /**
     * set font size in pixels
     * @name setFontSize
     * @methodOf Kinetic.Text.prototype
     * @param {int} fontSize
     */

     /**
     * get font size
     * @name getFontSize
     * @methodOf Kinetic.Text.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Text, 'fontStyle', NORMAL);

    /**
     * set font style.  Can be 'normal', 'italic', or 'bold'.  'normal' is the default.
     * @name setFontStyle
     * @methodOf Kinetic.Text.prototype
     * @param {String} fontStyle
     */

     /**
     * get font style
     * @name getFontStyle
     * @methodOf Kinetic.Text.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Text, 'padding', 0);

    /**
     * set padding
     * @name setPadding
     * @methodOf Kinetic.Text.prototype
     * @param {int} padding
     */

     /**
     * get padding
     * @name getPadding
     * @methodOf Kinetic.Text.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Text, 'align', LEFT);

    /**
     * set horizontal align of text
     * @name setAlign
     * @methodOf Kinetic.Text.prototype
     * @param {String} align align can be 'left', 'center', or 'right'
     */

     /**
     * get horizontal align
     * @name getAlign
     * @methodOf Kinetic.Text.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Text, 'lineHeight', 1);

    /**
     * set line height
     * @name setLineHeight
     * @methodOf Kinetic.Text.prototype
     * @param {Number} lineHeight default is 1
     */

     /**
     * get line height
     * @name getLineHeight
     * @methodOf Kinetic.Text.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Text, 'wrap', WORD);

    /**
     * set wrap
     * @name setWrap
     * @methodOf Kinetic.Text.prototype
     * @param {String} wrap can be word, char, or none. Default is word
     */

     /**
     * get wrap
     * @name getWrap
     * @methodOf Kinetic.Text.prototype
     */

    Kinetic.Node.addGetter(Kinetic.Text, TEXT, EMPTY_STRING);

    /**
     * get text
     * @name getText
     * @methodOf Kinetic.Text.prototype
     */
    
    Kinetic.Node.addSetter(Kinetic.Text, 'width');

    /**
     * set width
     * @name setWidth
     * @methodOf Kinetic.Text.prototype
     * @param {Number|String} width default is auto
     */

    Kinetic.Node.addSetter(Kinetic.Text, 'height'); 

    /**
     * set height
     * @name setHeight
     * @methodOf Kinetic.Text.prototype
     * @param {Number|String} height default is auto
     */
})();

(function() {
    /**
     * Line constructor.&nbsp; Lines are defined by an array of points
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Line = function(config) {
        this._initLine(config);
    };

    Kinetic.Line.prototype = {
        _initLine: function(config) {
            this.createAttrs();

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Line';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var points = this.getPoints(), length = points.length, context = canvas.getContext();
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);

            for(var n = 1; n < length; n++) {
                var point = points[n];
                context.lineTo(point.x, point.y);
            }

            canvas.stroke(this);
        },
        /**
         * set points array
         * @name setPoints
         * @methodOf Kinetic.Line.prototype
         * @param {Array} can be an array of point objects or an array
         *  of Numbers.  e.g. [{x:1,y:2},{x:3,y:4}] or [1,2,3,4]
         */
        setPoints: function(val) {
            this.setAttr('points', Kinetic.Util._getPoints(val));
        },
        /**
         * get points array
         * @name getPoints
         * @methodOf Kinetic.Line.prototype
         */
         // NOTE: cannot use getter method because we need to return a new
         // default array literal each time because arrays are modified by reference
        getPoints: function() {
            return this.attrs.points || [];
        }
    };
    Kinetic.Util.extend(Kinetic.Line, Kinetic.Shape);
})();

(function() {
    /**
     * Spline constructor.&nbsp; Splines are defined by an array of points and
     *  a tension
     * @constructor
     * @augments Kinetic.Line
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     * @param {Number} [config.tension] default value is 1.  Higher values will result in a more curvy line.  A value of 0 will result in no interpolation.
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Spline = function(config) {
        this._initSpline(config);
    };
    Kinetic.Spline._getControlPoints = function(p0, p1, p2, t) {
        var x0 = p0.x;
        var y0 = p0.y;
        var x1 = p1.x;
        var y1 = p1.y;
        var x2 = p2.x;
        var y2 = p2.y;
        var d01 = Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));
        var d12 = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        var fa = t * d01 / (d01 + d12);
        var fb = t * d12 / (d01 + d12);
        var p1x = x1 - fa * (x2 - x0);
        var p1y = y1 - fa * (y2 - y0);
        var p2x = x1 + fb * (x2 - x0);
        var p2y = y1 + fb * (y2 - y0);
        return [{
            x: p1x,
            y: p1y
        }, {
            x: p2x,
            y: p2y
        }];
    };

    Kinetic.Spline.prototype = {
        _initSpline: function(config) {
            this.createAttrs();
            // call super constructor
            Kinetic.Line.call(this, config);
            this.shapeType = 'Spline';
        },
        drawFunc: function(canvas) {
            var points = this.getPoints(), length = points.length, context = canvas.getContext(), tension = this.getTension();
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);

            // tension
            if(tension !== 0 && length > 2) {
                var ap = this.allPoints, len = ap.length;
                context.quadraticCurveTo(ap[0].x, ap[0].y, ap[1].x, ap[1].y);

                var n = 2;
                while(n < len - 1) {
                    context.bezierCurveTo(ap[n].x, ap[n++].y, ap[n].x, ap[n++].y, ap[n].x, ap[n++].y);
                }

                context.quadraticCurveTo(ap[len - 1].x, ap[len - 1].y, points[length - 1].x, points[length - 1].y);

            }
            // no tension
            else {
                for(var n = 1; n < length; n++) {
                    var point = points[n];
                    context.lineTo(point.x, point.y);
                }
            }

            canvas.stroke(this);
        },
        setPoints: function(val) {
            Kinetic.Line.prototype.setPoints.call(this, val);
            this._setAllPoints();
        },
        /**
         * set tension
         * @name setTension
         * @methodOf Kinetic.Spline.prototype
         * @param {Number} tension
         */
        setTension: function(tension) {
            this.setAttr('tension', tension);
            this._setAllPoints();
        },
        _setAllPoints: function() {
            var points = this.getPoints(), length = points.length, tension = this.getTension(), allPoints = [];

            for(var n = 1; n < length - 1; n++) {
                var cp = Kinetic.Spline._getControlPoints(points[n - 1], points[n], points[n + 1], tension);
                allPoints.push(cp[0]);
                allPoints.push(points[n]);
                allPoints.push(cp[1]);
            }

            this.allPoints = allPoints;
        }
    };
    Kinetic.Util.extend(Kinetic.Spline, Kinetic.Line);

    // add getters setters
    Kinetic.Node.addGetter(Kinetic.Spline, 'tension', 1);

    /**
     * get tension
     * @name getTension
     * @methodOf Kinetic.Spline.prototype
     */
})();

(function() {
    /**
     * Blob constructor.&nbsp; Blobs are defined by an array of points and
     *  a tension
     * @constructor
     * @augments Kinetic.Spline
     * @param {Object} config
     * @param {Array} config.points can be a flattened array of points, an array of point arrays, or an array of point objects.
     *  e.g. [0,1,2,3], [[0,1],[2,3]] and [{x:0,y:1},{x:2,y:3}] are equivalent
     * @param {Number} [config.tension] default value is 1.  Higher values will result in a more curvy line.  A value of 0 will result in no interpolation.
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Blob = function(config) {
        this._initBlob(config);
    };

    Kinetic.Blob.prototype = {
        _initBlob: function(config) {
            // call super constructor
            Kinetic.Spline.call(this, config);
            this.shapeType = 'Blob';
        },
        drawFunc: function(canvas) {
            var points = this.getPoints(), length = points.length, context = canvas.getContext(), tension = this.getTension();
            context.beginPath();
            context.moveTo(points[0].x, points[0].y);

            // tension
            if(tension !== 0 && length > 2) {
                var ap = this.allPoints, len = ap.length;
                var n = 0;
                while(n < len-1) {
                    context.bezierCurveTo(ap[n].x, ap[n++].y, ap[n].x, ap[n++].y, ap[n].x, ap[n++].y);
                } 
            }
            // no tension
            else {
                for(var n = 1; n < length; n++) {
                    var point = points[n];
                    context.lineTo(point.x, point.y);
                }
            }

			context.closePath();
            canvas.fillStroke(this);
        },
        _setAllPoints: function() {
            var points = this.getPoints(), length = points.length, tension = this.getTension(), firstControlPoints = Kinetic.Spline._getControlPoints(points[length - 1], points[0], points[1], tension), lastControlPoints = Kinetic.Spline._getControlPoints(points[length - 2], points[length - 1], points[0], tension);

            Kinetic.Spline.prototype._setAllPoints.call(this);

            // prepend control point
            this.allPoints.unshift(firstControlPoints[1]);

            // append cp, point, cp, cp, first point
            this.allPoints.push(lastControlPoints[0]);
            this.allPoints.push(points[length - 1]);
            this.allPoints.push(lastControlPoints[1]);
            this.allPoints.push(firstControlPoints[0]);
            this.allPoints.push(points[0]);
        }
    };

    Kinetic.Util.extend(Kinetic.Blob, Kinetic.Spline);
})();

(function() {
    /**
     * Sprite constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} config.animation animation key
     * @param {Object} config.animations animation map
     * @param {Integer} [config.index] animation index
     * @param {Image} image image object
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Sprite = function(config) {
        this._initSprite(config);
    }

    Kinetic.Sprite.prototype = {
        _initSprite: function(config) {
            this.createAttrs();
            
            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Sprite';
            this._setDrawFuncs();

            this.anim = new Kinetic.Animation();
            var that = this;
            this.on('animationChange', function() {
                // reset index when animation changes
                that.setIndex(0);
            });
        },
        drawFunc: function(canvas) {
            var anim = this.getAnimation(), 
                index = this.getIndex(), 
                f = this.getAnimations()[anim][index], 
                context = canvas.getContext(), 
                image = this.getImage();

            if(image) {
                context.drawImage(image, f.x, f.y, f.width, f.height, 0, 0, f.width, f.height);
            }
        },
        drawHitFunc: function(canvas) {
            var anim = this.getAnimation(), 
                index = this.getIndex(), 
                f = this.getAnimations()[anim][index], 
                context = canvas.getContext();

            context.beginPath();
            context.rect(0, 0, f.width, f.height);
            context.closePath();
            canvas.fill(this);
        },
        /**
         * start sprite animation
         * @name start
         * @methodOf Kinetic.Sprite.prototype
         */
        start: function() {
            var that = this;
            var layer = this.getLayer();

            /*
             * animation object has no executable function because
             *  the updates are done with a fixed FPS with the setInterval
             *  below.  The anim object only needs the layer reference for
             *  redraw
             */
            this.anim.setLayers(layer);

            this.interval = setInterval(function() {
                var index = that.getIndex();
                that._updateIndex();
                if(that.afterFrameFunc && index === that.afterFrameIndex) {
                    that.afterFrameFunc();
                    delete that.afterFrameFunc;
                    delete that.afterFrameIndex;
                }
            }, 1000 / this.getFrameRate());

            this.anim.start();
        },
        /**
         * stop sprite animation
         * @name stop
         * @methodOf Kinetic.Sprite.prototype
         */
        stop: function() {
            this.anim.stop();
            clearInterval(this.interval);
        },
        /**
         * set after frame event handler
         * @name afterFrame
         * @methodOf Kinetic.Sprite.prototype
         * @param {Integer} index frame index
         * @param {Function} func function to be executed after frame has been drawn
         */
        afterFrame: function(index, func) {
            this.afterFrameIndex = index;
            this.afterFrameFunc = func;
        },
        _updateIndex: function() {
            var index = this.getIndex(),
                animation = this.getAnimation(),
                animations = this.getAnimations(),
                anim = animations[animation], 
                len = anim.length;
                 
            if(index < len - 1) {
                this.setIndex(index + 1);
            }
            else {
                this.setIndex(0);
            }
        }
    };
    Kinetic.Util.extend(Kinetic.Sprite, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Sprite, 'animation');

    /**
     * set animation key
     * @name setAnimation
     * @methodOf Kinetic.Sprite.prototype
     * @param {String} anim animation key
     */

     /**
     * get animation key
     * @name getAnimation
     * @methodOf Kinetic.Sprite.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Sprite, 'animations');

    /**
     * set animations map
     * @name setAnimations
     * @methodOf Kinetic.Sprite.prototype
     * @param {Object} animations
     */

     /**
     * get animations map
     * @name getAnimations
     * @methodOf Kinetic.Sprite.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Sprite, 'image');

    /**
     * set image 
     * @name setImage
     * @methodOf Kinetic.Sprite.prototype
     * @param {Image} image 
     */

     /**
     * get image
     * @name getImage
     * @methodOf Kinetic.Sprite.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Sprite, 'index', 0);

    /**
     * set animation frame index
     * @name setIndex
     * @methodOf Kinetic.Sprite.prototype
     * @param {Integer} index frame index
     */

     /**
     * get animation frame index
     * @name getIndex
     * @methodOf Kinetic.Sprite.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Sprite, 'frameRate', 17);

    /**
     * set frame rate in frames / second.  Default is 17 frames per second.  Increase this number to make the sprite
     *  animation run faster, and decrease the number to make the sprite animation run slower
     * @name setFrameRate
     * @methodOf Kinetic.Sprite.prototype
     * @param {Integer} frameRate
     */

     /**
     * get frame rate
     * @name getFrameRate
     * @methodOf Kinetic.Sprite.prototype
     */

})();

(function() {
    /**
     * Path constructor.
     * @author Jason Follas
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} config.data SVG data string
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Path = function(config) {
        this._initPath(config);
    };

    Kinetic.Path.prototype = {
        _initPath: function(config) {
            this.dataArray = [];
            var that = this;

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Path';
            this._setDrawFuncs();

            this.dataArray = Kinetic.Path.parsePathData(this.getData());
            this.on('dataChange', function() {
                that.dataArray = Kinetic.Path.parsePathData(this.getData());
            });
        },
        drawFunc: function(canvas) {
            var ca = this.dataArray, context = canvas.getContext();
            // context position
            context.beginPath();
            for(var n = 0; n < ca.length; n++) {
                var c = ca[n].command;
                var p = ca[n].points;
                switch (c) {
                    case 'L':
                        context.lineTo(p[0], p[1]);
                        break;
                    case 'M':
                        context.moveTo(p[0], p[1]);
                        break;
                    case 'C':
                        context.bezierCurveTo(p[0], p[1], p[2], p[3], p[4], p[5]);
                        break;
                    case 'Q':
                        context.quadraticCurveTo(p[0], p[1], p[2], p[3]);
                        break;
                    case 'A':
                        var cx = p[0], cy = p[1], rx = p[2], ry = p[3], theta = p[4], dTheta = p[5], psi = p[6], fs = p[7];

                        var r = (rx > ry) ? rx : ry;
                        var scaleX = (rx > ry) ? 1 : rx / ry;
                        var scaleY = (rx > ry) ? ry / rx : 1;

                        context.translate(cx, cy);
                        context.rotate(psi);
                        context.scale(scaleX, scaleY);
                        context.arc(0, 0, r, theta, theta + dTheta, 1 - fs);
                        context.scale(1 / scaleX, 1 / scaleY);
                        context.rotate(-psi);
                        context.translate(-cx, -cy);

                        break;
                    case 'z':
                        context.closePath();
                        break;
                }
            }
            canvas.fillStroke(this);
        }
    };
    Kinetic.Util.extend(Kinetic.Path, Kinetic.Shape);

    Kinetic.Path.getLineLength = function(x1, y1, x2, y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    };
    Kinetic.Path.getPointOnLine = function(dist, P1x, P1y, P2x, P2y, fromX, fromY) {
        if(fromX === undefined) {
            fromX = P1x;
        }
        if(fromY === undefined) {
            fromY = P1y;
        }

        var m = (P2y - P1y) / ((P2x - P1x) + 0.00000001);
        var run = Math.sqrt(dist * dist / (1 + m * m));
        if(P2x < P1x)
            run *= -1;
        var rise = m * run;
        var pt;

        if((fromY - P1y) / ((fromX - P1x) + 0.00000001) === m) {
            pt = {
                x: fromX + run,
                y: fromY + rise
            };
        }
        else {
            var ix, iy;

            var len = this.getLineLength(P1x, P1y, P2x, P2y);
            if(len < 0.00000001) {
                return undefined;
            }
            var u = (((fromX - P1x) * (P2x - P1x)) + ((fromY - P1y) * (P2y - P1y)));
            u = u / (len * len);
            ix = P1x + u * (P2x - P1x);
            iy = P1y + u * (P2y - P1y);

            var pRise = this.getLineLength(fromX, fromY, ix, iy);
            var pRun = Math.sqrt(dist * dist - pRise * pRise);
            run = Math.sqrt(pRun * pRun / (1 + m * m));
            if(P2x < P1x)
                run *= -1;
            rise = m * run;
            pt = {
                x: ix + run,
                y: iy + rise
            };
        }

        return pt;
    };

    Kinetic.Path.getPointOnCubicBezier = function(pct, P1x, P1y, P2x, P2y, P3x, P3y, P4x, P4y) {
        function CB1(t) {
            return t * t * t;
        }
        function CB2(t) {
            return 3 * t * t * (1 - t);
        }
        function CB3(t) {
            return 3 * t * (1 - t) * (1 - t);
        }
        function CB4(t) {
            return (1 - t) * (1 - t) * (1 - t);
        }
        var x = P4x * CB1(pct) + P3x * CB2(pct) + P2x * CB3(pct) + P1x * CB4(pct);
        var y = P4y * CB1(pct) + P3y * CB2(pct) + P2y * CB3(pct) + P1y * CB4(pct);

        return {
            x: x,
            y: y
        };
    };
    Kinetic.Path.getPointOnQuadraticBezier = function(pct, P1x, P1y, P2x, P2y, P3x, P3y) {
        function QB1(t) {
            return t * t;
        }
        function QB2(t) {
            return 2 * t * (1 - t);
        }
        function QB3(t) {
            return (1 - t) * (1 - t);
        }
        var x = P3x * QB1(pct) + P2x * QB2(pct) + P1x * QB3(pct);
        var y = P3y * QB1(pct) + P2y * QB2(pct) + P1y * QB3(pct);

        return {
            x: x,
            y: y
        };
    };
    Kinetic.Path.getPointOnEllipticalArc = function(cx, cy, rx, ry, theta, psi) {
        var cosPsi = Math.cos(psi), sinPsi = Math.sin(psi);
        var pt = {
            x: rx * Math.cos(theta),
            y: ry * Math.sin(theta)
        };
        return {
            x: cx + (pt.x * cosPsi - pt.y * sinPsi),
            y: cy + (pt.x * sinPsi + pt.y * cosPsi)
        };
    };
    /**
     * get parsed data array from the data
     *  string.  V, v, H, h, and l data are converted to
     *  L data for the purpose of high performance Path
     *  rendering
     */
    Kinetic.Path.parsePathData = function(data) {
        // Path Data Segment must begin with a moveTo
        //m (x y)+  Relative moveTo (subsequent points are treated as lineTo)
        //M (x y)+  Absolute moveTo (subsequent points are treated as lineTo)
        //l (x y)+  Relative lineTo
        //L (x y)+  Absolute LineTo
        //h (x)+    Relative horizontal lineTo
        //H (x)+    Absolute horizontal lineTo
        //v (y)+    Relative vertical lineTo
        //V (y)+    Absolute vertical lineTo
        //z (closepath)
        //Z (closepath)
        //c (x1 y1 x2 y2 x y)+ Relative Bezier curve
        //C (x1 y1 x2 y2 x y)+ Absolute Bezier curve
        //q (x1 y1 x y)+       Relative Quadratic Bezier
        //Q (x1 y1 x y)+       Absolute Quadratic Bezier
        //t (x y)+    Shorthand/Smooth Relative Quadratic Bezier
        //T (x y)+    Shorthand/Smooth Absolute Quadratic Bezier
        //s (x2 y2 x y)+       Shorthand/Smooth Relative Bezier curve
        //S (x2 y2 x y)+       Shorthand/Smooth Absolute Bezier curve
        //a (rx ry x-axis-rotation large-arc-flag sweep-flag x y)+     Relative Elliptical Arc
        //A (rx ry x-axis-rotation large-arc-flag sweep-flag x y)+  Absolute Elliptical Arc

        // return early if data is not defined
        if(!data) {
            return [];
        }

        // command string
        var cs = data;

        // command chars
        var cc = ['m', 'M', 'l', 'L', 'v', 'V', 'h', 'H', 'z', 'Z', 'c', 'C', 'q', 'Q', 't', 'T', 's', 'S', 'a', 'A'];
        // convert white spaces to commas
        cs = cs.replace(new RegExp(' ', 'g'), ',');
        // create pipes so that we can split the data
        for(var n = 0; n < cc.length; n++) {
            cs = cs.replace(new RegExp(cc[n], 'g'), '|' + cc[n]);
        }
        // create array
        var arr = cs.split('|');
        var ca = [];
        // init context point
        var cpx = 0;
        var cpy = 0;
        for(var n = 1; n < arr.length; n++) {
            var str = arr[n];
            var c = str.charAt(0);
            str = str.slice(1);
            // remove ,- for consistency
            str = str.replace(new RegExp(',-', 'g'), '-');
            // add commas so that it's easy to split
            str = str.replace(new RegExp('-', 'g'), ',-');
            str = str.replace(new RegExp('e,-', 'g'), 'e-');
            var p = str.split(',');
            if(p.length > 0 && p[0] === '') {
                p.shift();
            }
            // convert strings to floats
            for(var i = 0; i < p.length; i++) {
                p[i] = parseFloat(p[i]);
            }
            while(p.length > 0) {
                if(isNaN(p[0]))// case for a trailing comma before next command
                    break;

                var cmd = null;
                var points = [];
                var startX = cpx, startY = cpy;

                // convert l, H, h, V, and v to L
                switch (c) {

                    // Note: Keep the lineTo's above the moveTo's in this switch
                    case 'l':
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'L':
                        cpx = p.shift();
                        cpy = p.shift();
                        points.push(cpx, cpy);
                        break;

                    // Note: lineTo handlers need to be above this point
                    case 'm':
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'M';
                        points.push(cpx, cpy);
                        c = 'l';
                        // subsequent points are treated as relative lineTo
                        break;
                    case 'M':
                        cpx = p.shift();
                        cpy = p.shift();
                        cmd = 'M';
                        points.push(cpx, cpy);
                        c = 'L';
                        // subsequent points are treated as absolute lineTo
                        break;

                    case 'h':
                        cpx += p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'H':
                        cpx = p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'v':
                        cpy += p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'V':
                        cpy = p.shift();
                        cmd = 'L';
                        points.push(cpx, cpy);
                        break;
                    case 'C':
                        points.push(p.shift(), p.shift(), p.shift(), p.shift());
                        cpx = p.shift();
                        cpy = p.shift();
                        points.push(cpx, cpy);
                        break;
                    case 'c':
                        points.push(cpx + p.shift(), cpy + p.shift(), cpx + p.shift(), cpy + p.shift());
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'C';
                        points.push(cpx, cpy);
                        break;
                    case 'S':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'C') {
                            ctlPtx = cpx + (cpx - prevCmd.points[2]);
                            ctlPty = cpy + (cpy - prevCmd.points[3]);
                        }
                        points.push(ctlPtx, ctlPty, p.shift(), p.shift());
                        cpx = p.shift();
                        cpy = p.shift();
                        cmd = 'C';
                        points.push(cpx, cpy);
                        break;
                    case 's':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'C') {
                            ctlPtx = cpx + (cpx - prevCmd.points[2]);
                            ctlPty = cpy + (cpy - prevCmd.points[3]);
                        }
                        points.push(ctlPtx, ctlPty, cpx + p.shift(), cpy + p.shift());
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'C';
                        points.push(cpx, cpy);
                        break;
                    case 'Q':
                        points.push(p.shift(), p.shift());
                        cpx = p.shift();
                        cpy = p.shift();
                        points.push(cpx, cpy);
                        break;
                    case 'q':
                        points.push(cpx + p.shift(), cpy + p.shift());
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'Q';
                        points.push(cpx, cpy);
                        break;
                    case 'T':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'Q') {
                            ctlPtx = cpx + (cpx - prevCmd.points[0]);
                            ctlPty = cpy + (cpy - prevCmd.points[1]);
                        }
                        cpx = p.shift();
                        cpy = p.shift();
                        cmd = 'Q';
                        points.push(ctlPtx, ctlPty, cpx, cpy);
                        break;
                    case 't':
                        var ctlPtx = cpx, ctlPty = cpy;
                        var prevCmd = ca[ca.length - 1];
                        if(prevCmd.command === 'Q') {
                            ctlPtx = cpx + (cpx - prevCmd.points[0]);
                            ctlPty = cpy + (cpy - prevCmd.points[1]);
                        }
                        cpx += p.shift();
                        cpy += p.shift();
                        cmd = 'Q';
                        points.push(ctlPtx, ctlPty, cpx, cpy);
                        break;
                    case 'A':
                        var rx = p.shift(), ry = p.shift(), psi = p.shift(), fa = p.shift(), fs = p.shift();
                        var x1 = cpx, y1 = cpy; cpx = p.shift(), cpy = p.shift();
                        cmd = 'A';
                        points = this.convertEndpointToCenterParameterization(x1, y1, cpx, cpy, fa, fs, rx, ry, psi);
                        break;
                    case 'a':
                        var rx = p.shift(), ry = p.shift(), psi = p.shift(), fa = p.shift(), fs = p.shift();
                        var x1 = cpx, y1 = cpy; cpx += p.shift(), cpy += p.shift();
                        cmd = 'A';
                        points = this.convertEndpointToCenterParameterization(x1, y1, cpx, cpy, fa, fs, rx, ry, psi);
                        break;
                }

                ca.push({
                    command: cmd || c,
                    points: points,
                    start: {
                        x: startX,
                        y: startY
                    },
                    pathLength: this.calcLength(startX, startY, cmd || c, points)
                });
            }

            if(c === 'z' || c === 'Z') {
                ca.push({
                    command: 'z',
                    points: [],
                    start: undefined,
                    pathLength: 0
                });
            }
        }

        return ca;
    };
    Kinetic.Path.calcLength = function(x, y, cmd, points) {
        var len, p1, p2;
        var path = Kinetic.Path;

        switch (cmd) {
            case 'L':
                return path.getLineLength(x, y, points[0], points[1]);
            case 'C':
                // Approximates by breaking curve into 100 line segments
                len = 0.0;
                p1 = path.getPointOnCubicBezier(0, x, y, points[0], points[1], points[2], points[3], points[4], points[5]);
                for( t = 0.01; t <= 1; t += 0.01) {
                    p2 = path.getPointOnCubicBezier(t, x, y, points[0], points[1], points[2], points[3], points[4], points[5]);
                    len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                    p1 = p2;
                }
                return len;
            case 'Q':
                // Approximates by breaking curve into 100 line segments
                len = 0.0;
                p1 = path.getPointOnQuadraticBezier(0, x, y, points[0], points[1], points[2], points[3]);
                for( t = 0.01; t <= 1; t += 0.01) {
                    p2 = path.getPointOnQuadraticBezier(t, x, y, points[0], points[1], points[2], points[3]);
                    len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                    p1 = p2;
                }
                return len;
            case 'A':
                // Approximates by breaking curve into line segments
                len = 0.0;
                var start = points[4];
                // 4 = theta
                var dTheta = points[5];
                // 5 = dTheta
                var end = points[4] + dTheta;
                var inc = Math.PI / 180.0;
                // 1 degree resolution
                if(Math.abs(start - end) < inc) {
                    inc = Math.abs(start - end);
                }
                // Note: for purpose of calculating arc length, not going to worry about rotating X-axis by angle psi
                p1 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], start, 0);
                if(dTheta < 0) {// clockwise
                    for( t = start - inc; t > end; t -= inc) {
                        p2 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], t, 0);
                        len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                        p1 = p2;
                    }
                }
                else {// counter-clockwise
                    for( t = start + inc; t < end; t += inc) {
                        p2 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], t, 0);
                        len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);
                        p1 = p2;
                    }
                }
                p2 = path.getPointOnEllipticalArc(points[0], points[1], points[2], points[3], end, 0);
                len += path.getLineLength(p1.x, p1.y, p2.x, p2.y);

                return len;
        }

        return 0;
    };
    Kinetic.Path.convertEndpointToCenterParameterization = function(x1, y1, x2, y2, fa, fs, rx, ry, psiDeg) {
        // Derived from: http://www.w3.org/TR/SVG/implnote.html#ArcImplementationNotes
        var psi = psiDeg * (Math.PI / 180.0);
        var xp = Math.cos(psi) * (x1 - x2) / 2.0 + Math.sin(psi) * (y1 - y2) / 2.0;
        var yp = -1 * Math.sin(psi) * (x1 - x2) / 2.0 + Math.cos(psi) * (y1 - y2) / 2.0;

        var lambda = (xp * xp) / (rx * rx) + (yp * yp) / (ry * ry);

        if(lambda > 1) {
            rx *= Math.sqrt(lambda);
            ry *= Math.sqrt(lambda);
        }

        var f = Math.sqrt((((rx * rx) * (ry * ry)) - ((rx * rx) * (yp * yp)) - ((ry * ry) * (xp * xp))) / ((rx * rx) * (yp * yp) + (ry * ry) * (xp * xp)));

        if(fa == fs) {
            f *= -1;
        }
        if(isNaN(f)) {
            f = 0;
        }

        var cxp = f * rx * yp / ry;
        var cyp = f * -ry * xp / rx;

        var cx = (x1 + x2) / 2.0 + Math.cos(psi) * cxp - Math.sin(psi) * cyp;
        var cy = (y1 + y2) / 2.0 + Math.sin(psi) * cxp + Math.cos(psi) * cyp;

        var vMag = function(v) {
            return Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        };
        var vRatio = function(u, v) {
            return (u[0] * v[0] + u[1] * v[1]) / (vMag(u) * vMag(v));
        };
        var vAngle = function(u, v) {
            return (u[0] * v[1] < u[1] * v[0] ? -1 : 1) * Math.acos(vRatio(u, v));
        };
        var theta = vAngle([1, 0], [(xp - cxp) / rx, (yp - cyp) / ry]);
        var u = [(xp - cxp) / rx, (yp - cyp) / ry];
        var v = [(-1 * xp - cxp) / rx, (-1 * yp - cyp) / ry];
        var dTheta = vAngle(u, v);

        if(vRatio(u, v) <= -1) {
            dTheta = Math.PI;
        }
        if(vRatio(u, v) >= 1) {
            dTheta = 0;
        }
        if(fs === 0 && dTheta > 0) {
            dTheta = dTheta - 2 * Math.PI;
        }
        if(fs == 1 && dTheta < 0) {
            dTheta = dTheta + 2 * Math.PI;
        }
        return [cx, cy, rx, ry, theta, dTheta, psi, fs];
    };
    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Path, 'data');

    /**
     * set SVG path data string.  This method
     *  also automatically parses the data string
     *  into a data array.  Currently supported SVG data:
     *  M, m, L, l, H, h, V, v, Q, q, T, t, C, c, S, s, A, a, Z, z
     * @name setData
     * @methodOf Kinetic.Path.prototype
     * @param {String} SVG path command string
     */

    /**
     * get SVG path data string
     * @name getData
     * @methodOf Kinetic.Path.prototype
     */
})();

(function() {
    var EMPTY_STRING = '',
        CALIBRI = 'Calibri',
        NORMAL = 'normal';

    /**
     * Path constructor.
     * @author Jason Follas
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {String} [config.fontFamily] default is Calibri
     * @param {Number} [config.fontSize] default is 12
     * @param {String} [config.fontStyle] can be normal, bold, or italic.  Default is normal
     * @param {String} config.text
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.TextPath = function(config) {
        this._initTextPath(config);
    };
    
    function _fillFunc(context) {
        context.fillText(this.partialText, 0, 0);
    }
    function _strokeFunc(context) {
        context.strokeText(this.partialText, 0, 0);
    }

    Kinetic.TextPath.prototype = {
        _initTextPath: function(config) {
            var that = this;
          
            this.createAttrs();
            this.dummyCanvas = document.createElement('canvas');
            this.dataArray = [];
            
            // call super constructor
            Kinetic.Shape.call(this, config);

            // overrides
            // TODO: shouldn't this be on the prototype?
            this._fillFunc = _fillFunc;
            this._strokeFunc = _strokeFunc;

            this.shapeType = 'TextPath';
            this._setDrawFuncs();

            this.dataArray = Kinetic.Path.parsePathData(this.attrs.data);
            this.on('dataChange', function() {
                that.dataArray = Kinetic.Path.parsePathData(this.attrs.data);
            });
            // update text data for certain attr changes
            var attrs = ['text', 'textStroke', 'textStrokeWidth'];
            for(var n = 0; n < attrs.length; n++) {
                var attr = attrs[n];
                this.on(attr + 'Change', that._setTextData);
            }
            that._setTextData();
        },
        drawFunc: function(canvas) {
            var charArr = this.charArr, context = canvas.getContext();

            context.font = this._getContextFont();
            context.textBaseline = 'middle';
            context.textAlign = 'left';
            context.save();

            var glyphInfo = this.glyphInfo;
            for(var i = 0; i < glyphInfo.length; i++) {
                context.save();

                var p0 = glyphInfo[i].p0;
                var p1 = glyphInfo[i].p1;
                var ht = parseFloat(this.attrs.fontSize);

                context.translate(p0.x, p0.y);
                context.rotate(glyphInfo[i].rotation);
                this.partialText = glyphInfo[i].text;
                
                canvas.fillStroke(this);
                context.restore();

                //// To assist with debugging visually, uncomment following
                // context.beginPath();
                // if (i % 2)
                // context.strokeStyle = 'cyan';
                // else
                // context.strokeStyle = 'green';

                // context.moveTo(p0.x, p0.y);
                // context.lineTo(p1.x, p1.y);
                // context.stroke();
            }
            context.restore();
        },
        /**
         * get text width in pixels
         * @name getTextWidth
         * @methodOf Kinetic.TextPath.prototype
         */
        getTextWidth: function() {
            return this.textWidth;
        },
        /**
         * get text height in pixels
         * @name getTextHeight
         * @methodOf Kinetic.TextPath.prototype
         */
        getTextHeight: function() {
            return this.textHeight;
        },
        /**
         * set text
         * @name setText
         * @methodOf Kinetic.TextPath.prototype
         * @param {String} text
         */
        setText: function(text) {
            Kinetic.Text.prototype.setText.call(this, text);
        },
        _getTextSize: function(text) {
            var dummyCanvas = this.dummyCanvas;
            var context = dummyCanvas.getContext('2d');

            context.save();

            context.font = this._getContextFont();
            var metrics = context.measureText(text);

            context.restore();

            return {
                width: metrics.width,
                height: parseInt(this.attrs.fontSize, 10)
            };
        },
        /**
         * set text data.
         */
        _setTextData: function() {

            var that = this;
            var size = this._getTextSize(this.attrs.text);
            this.textWidth = size.width;
            this.textHeight = size.height;

            this.glyphInfo = [];

            var charArr = this.attrs.text.split('');

            var p0, p1, pathCmd;

            var pIndex = -1;
            var currentT = 0;

            var getNextPathSegment = function() {
                currentT = 0;
                var pathData = that.dataArray;

                for(var i = pIndex + 1; i < pathData.length; i++) {
                    if(pathData[i].pathLength > 0) {
                        pIndex = i;

                        return pathData[i];
                    }
                    else if(pathData[i].command == 'M') {
                        p0 = {
                            x: pathData[i].points[0],
                            y: pathData[i].points[1]
                        };
                    }
                }

                return {};
            };
            var findSegmentToFitCharacter = function(c, before) {

                var glyphWidth = that._getTextSize(c).width;

                var currLen = 0;
                var attempts = 0;
                var needNextSegment = false;
                p1 = undefined;
                while(Math.abs(glyphWidth - currLen) / glyphWidth > 0.01 && attempts < 25) {
                    attempts++;
                    var cumulativePathLength = currLen;
                    while(pathCmd === undefined) {
                        pathCmd = getNextPathSegment();

                        if(pathCmd && cumulativePathLength + pathCmd.pathLength < glyphWidth) {
                            cumulativePathLength += pathCmd.pathLength;
                            pathCmd = undefined;
                        }
                    }

                    if(pathCmd === {} || p0 === undefined)
                        return undefined;

                    var needNewSegment = false;

                    switch (pathCmd.command) {
                        case 'L':
                            if(Kinetic.Path.getLineLength(p0.x, p0.y, pathCmd.points[0], pathCmd.points[1]) > glyphWidth) {
                                p1 = Kinetic.Path.getPointOnLine(glyphWidth, p0.x, p0.y, pathCmd.points[0], pathCmd.points[1], p0.x, p0.y);
                            }
                            else
                                pathCmd = undefined;
                            break;
                        case 'A':

                            var start = pathCmd.points[4];
                            // 4 = theta
                            var dTheta = pathCmd.points[5];
                            // 5 = dTheta
                            var end = pathCmd.points[4] + dTheta;

                            if(currentT === 0)
                                currentT = start + 0.00000001;
                            // Just in case start is 0
                            else if(glyphWidth > currLen)
                                currentT += (Math.PI / 180.0) * dTheta / Math.abs(dTheta);
                            else
                                currentT -= Math.PI / 360.0 * dTheta / Math.abs(dTheta);

                            if(Math.abs(currentT) > Math.abs(end)) {
                                currentT = end;
                                needNewSegment = true;
                            }
                            p1 = Kinetic.Path.getPointOnEllipticalArc(pathCmd.points[0], pathCmd.points[1], pathCmd.points[2], pathCmd.points[3], currentT, pathCmd.points[6]);
                            break;
                        case 'C':
                            if(currentT === 0) {
                                if(glyphWidth > pathCmd.pathLength)
                                    currentT = 0.00000001;
                                else
                                    currentT = glyphWidth / pathCmd.pathLength;
                            }
                            else if(glyphWidth > currLen)
                                currentT += (glyphWidth - currLen) / pathCmd.pathLength;
                            else
                                currentT -= (currLen - glyphWidth) / pathCmd.pathLength;

                            if(currentT > 1.0) {
                                currentT = 1.0;
                                needNewSegment = true;
                            }
                            p1 = Kinetic.Path.getPointOnCubicBezier(currentT, pathCmd.start.x, pathCmd.start.y, pathCmd.points[0], pathCmd.points[1], pathCmd.points[2], pathCmd.points[3], pathCmd.points[4], pathCmd.points[5]);
                            break;
                        case 'Q':
                            if(currentT === 0)
                                currentT = glyphWidth / pathCmd.pathLength;
                            else if(glyphWidth > currLen)
                                currentT += (glyphWidth - currLen) / pathCmd.pathLength;
                            else
                                currentT -= (currLen - glyphWidth) / pathCmd.pathLength;

                            if(currentT > 1.0) {
                                currentT = 1.0;
                                needNewSegment = true;
                            }
                            p1 = Kinetic.Path.getPointOnQuadraticBezier(currentT, pathCmd.start.x, pathCmd.start.y, pathCmd.points[0], pathCmd.points[1], pathCmd.points[2], pathCmd.points[3]);
                            break;

                    }

                    if(p1 !== undefined) {
                        currLen = Kinetic.Path.getLineLength(p0.x, p0.y, p1.x, p1.y);
                    }

                    if(needNewSegment) {
                        needNewSegment = false;
                        pathCmd = undefined;
                    }
                }
            };
            for(var i = 0; i < charArr.length; i++) {

                // Find p1 such that line segment between p0 and p1 is approx. width of glyph
                findSegmentToFitCharacter(charArr[i]);

                if(p0 === undefined || p1 === undefined)
                    break;

                var width = Kinetic.Path.getLineLength(p0.x, p0.y, p1.x, p1.y);

                // Note: Since glyphs are rendered one at a time, any kerning pair data built into the font will not be used.
                // Can foresee having a rough pair table built in that the developer can override as needed.

                var kern = 0;
                // placeholder for future implementation

                var midpoint = Kinetic.Path.getPointOnLine(kern + width / 2.0, p0.x, p0.y, p1.x, p1.y);

                var rotation = Math.atan2((p1.y - p0.y), (p1.x - p0.x));
                this.glyphInfo.push({
                    transposeX: midpoint.x,
                    transposeY: midpoint.y,
                    text: charArr[i],
                    rotation: rotation,
                    p0: p0,
                    p1: p1
                });
                p0 = p1;
            }
        }
    };

    // map TextPath methods to Text
    Kinetic.TextPath.prototype._getContextFont = Kinetic.Text.prototype._getContextFont;
    
    Kinetic.Util.extend(Kinetic.TextPath, Kinetic.Shape);

    // add setters and getters
    Kinetic.Node.addGetterSetter(Kinetic.TextPath, 'fontFamily', CALIBRI);

    /**
     * set font family
     * @name setFontFamily
     * @methodOf Kinetic.TextPath.prototype
     * @param {String} fontFamily
     */

     /**
     * get font family
     * @name getFontFamily
     * @methodOf Kinetic.TextPath.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.TextPath, 'fontSize', 12);

    /**
     * set font size
     * @name setFontSize
     * @methodOf Kinetic.TextPath.prototype
     * @param {int} fontSize
     */

     /**
     * get font size
     * @name getFontSize
     * @methodOf Kinetic.TextPath.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.TextPath, 'fontStyle', NORMAL);

    /**
     * set font style.  Can be 'normal', 'italic', or 'bold'.  'normal' is the default.
     * @name setFontStyle
     * @methodOf Kinetic.TextPath.prototype
     * @param {String} fontStyle
     */

     /**
     * get font style
     * @name getFontStyle
     * @methodOf Kinetic.TextPath.prototype
     */
    
    Kinetic.Node.addGetter(Kinetic.TextPath, 'text', EMPTY_STRING);

    /**
     * get text
     * @name getText
     * @methodOf Kinetic.TextPath.prototype
     */
})();

(function() {
    /**
     * RegularPolygon constructor.&nbsp; Examples include triangles, squares, pentagons, hexagons, etc.
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Number} config.sides
     * @param {Number} config.radius
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.RegularPolygon = function(config) {
        this._initRegularPolygon(config);
    };

    Kinetic.RegularPolygon.prototype = {
        _initRegularPolygon: function(config) {
            this.createAttrs();

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'RegularPolygon';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
        	var context = canvas.getContext(), sides = this.attrs.sides, radius = this.attrs.radius;
            context.beginPath();
            context.moveTo(0, 0 - radius);

            for(var n = 1; n < sides; n++) {
                var x = radius * Math.sin(n * 2 * Math.PI / sides);
                var y = -1 * radius * Math.cos(n * 2 * Math.PI / sides);
                context.lineTo(x, y);
            }
            context.closePath();
            canvas.fillStroke(this);
        }
    };
    Kinetic.Util.extend(Kinetic.RegularPolygon, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.RegularPolygon, 'radius', 0);

    /**
     * set radius
     * @name setRadius
     * @methodOf Kinetic.RegularPolygon.prototype
     * @param {Number} radius
     */

     /**
     * get radius
     * @name getRadius
     * @methodOf Kinetic.RegularPolygon.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.RegularPolygon, 'sides', 0);

    /**
     * set number of sides
     * @name setSides
     * @methodOf Kinetic.RegularPolygon.prototype
     * @param {int} sides
     */

    /**
     * get number of sides
     * @name getSides
     * @methodOf Kinetic.RegularPolygon.prototype
     */
})();

(function() {
    /**
     * Star constructor
     * @constructor
     * @augments Kinetic.Shape
     * @param {Object} config
     * @param {Integer} config.numPoints
     * @param {Number} config.innerRadius
     * @param {Number} config.outerRadius
     * @param {String} [config.fill] fill color
     * @param {Object} [config.fillRGB] set fill color with an object literal containing an r, g, and b component
     * @param {Integer} [config.fillR] set fill red component
     * @param {Integer} [config.fillG] set fill green component
     * @param {Integer} [config.fillB] set fill blue component
     * @param {Image} [config.fillPatternImage] fill pattern image
     * @param {Number} [config.fillPatternX]
     * @param {Number} [config.fillPatternY]
     * @param {Number|Array|Object} [config.fillPatternOffset] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternOffsetX] 
     * @param {Number} [config.fillPatternOffsetY] 
     * @param {Number|Array|Object} [config.fillPatternScale] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillPatternScaleX]
     * @param {Number} [config.fillPatternScaleY]
     * @param {Number} [config.fillPatternRotation]
     * @param {String} [config.fillPatternRepeat] can be 'repeat', 'repeat-x', 'repeat-y', or 'no-repeat'.  The default is 'no-repeat'
     * @param {Number|Array|Object} [config.fillLinearGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientStartPointX]
     * @param {Number} [config.fillLinearGradientStartPointY]
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number|Array|Object} [config.fillLinearGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillLinearGradientEndPointX]
     * @param {Number} [config.fillLinearGradientEndPointY]
     * @param {Array} [config.fillLinearGradientColorStops] array of color stops
     * @param {Number|Array|Object} [config.fillRadialGradientStartPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientStartPointX]
     * @param {Number} [config.fillRadialGradientStartPointY]
     * @param {Number|Array|Object} [config.fillRadialGradientEndPoint] number, array with two elements, or object with x and y component
     * @param {Number} [config.fillRadialGradientEndPointX] 
     * @param {Number} [config.fillRadialGradientEndPointY] 
     * @param {Number} [config.fillRadialGradientStartRadius]
     * @param {Number} [config.fillRadialGradientEndRadius]
     * @param {Array} [config.fillRadialGradientColorStops] array of color stops
     * @param {Boolean} [config.fillEnabled] flag which enables or disables the fill.  The default value is true
     * @param {String} [config.fillPriority] can be color, linear-gradient, radial-graident, or pattern.  The default value is color.  The fillPriority property makes it really easy to toggle between different fill types.  For example, if you want to toggle between a fill color style and a fill pattern style, simply set the fill property and the fillPattern properties, and then use setFillPriority('color') to render the shape with a color fill, or use setFillPriority('pattern') to render the shape with the pattern fill configuration
     * @param {String} [config.stroke] stroke color
     * @param {Object} [config.strokeRGB] set stroke color with an object literal containing an r, g, and b component
     * @param {Integer} [config.strokeR] set stroke red component
     * @param {Integer} [config.strokeG] set stroke green component
     * @param {Integer} [config.strokeB] set stroke blue component
     * @param {Number} [config.strokeWidth] stroke width
     * @param {Boolean} [config.strokeScaleEnabled] flag which enables or disables stroke scale.  The default is true
     * @param {Boolean} [config.strokeEnabled] flag which enables or disables the stroke.  The default value is true
     * @param {String} [config.lineJoin] can be miter, round, or bevel.  The default
     *  is miter
     * @param {String} [config.lineCap] can be butt, round, or sqare.  The default
     *  is butt
     * @param {String} [config.shadowColor]
     * @param {Object} [config.shadowColorRGB] set shadowColor color with an object literal containing an r, g, and b component
     * @param {Integer} [config.shadowColorR] set shadowColor red component
     * @param {Integer} [config.shadowColorG] set shadowColor green component
     * @param {Integer} [config.shadowColorB] set shadowColor blue component
     * @param {Number} [config.shadowBlur]
     * @param {Object} [config.shadowOffset]
     * @param {Number} [config.shadowOffsetX]
     * @param {Number} [config.shadowOffsetY]
     * @param {Number} [config.shadowOpacity] shadow opacity.  Can be any real number
     *  between 0 and 1
     * @param {Boolean} [config.shadowEnabled] flag which enables or disables the shadow.  The default value is true
     * @param {Array} [config.dashArray]
     * @param {Boolean} [config.dashArrayEnabled] flag which enables or disables the dashArray.  The default value is true
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Star = function(config) {
        this._initStar(config);
    };

    Kinetic.Star.prototype = {
        _initStar: function(config) {
            this.createAttrs();

            // call super constructor
            Kinetic.Shape.call(this, config);
            this.shapeType = 'Star';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var context = canvas.getContext(), innerRadius = this.attrs.innerRadius, outerRadius = this.attrs.outerRadius, numPoints = this.attrs.numPoints;

            context.beginPath();
            context.moveTo(0, 0 - this.attrs.outerRadius);

            for(var n = 1; n < numPoints * 2; n++) {
                var radius = n % 2 === 0 ? outerRadius : innerRadius;
                var x = radius * Math.sin(n * Math.PI / numPoints);
                var y = -1 * radius * Math.cos(n * Math.PI / numPoints);
                context.lineTo(x, y);
            }
            context.closePath();

            canvas.fillStroke(this);
        }
    };
    Kinetic.Util.extend(Kinetic.Star, Kinetic.Shape);

    // add getters setters
    Kinetic.Node.addGetterSetter(Kinetic.Star, 'numPoints', 0);

    /**
     * set number of points
     * @name setNumPoints
     * @methodOf Kinetic.Star.prototype
     * @param {Integer} points
     */

     /**
     * get number of points
     * @name getNumPoints
     * @methodOf Kinetic.Star.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Star, 'innerRadius', 0);

    /**
     * set inner radius
     * @name setInnerRadius
     * @methodOf Kinetic.Star.prototype
     * @param {Number} radius
     */

     /**
     * get inner radius
     * @name getInnerRadius
     * @methodOf Kinetic.Star.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.Star, 'outerRadius', 0);

    /**
     * set outer radius
     * @name setOuterRadius
     * @methodOf Kinetic.Star.prototype
     * @param {Number} radius
     */

     /**
     * get outer radius
     * @name getOuterRadius
     * @methodOf Kinetic.Star.prototype
     */
})();

(function() {
    // constants
    var ATTR_CHANGE_LIST = ['fontFamily', 'fontSize', 'fontStyle', 'padding', 'lineHeight', 'text'],
        CHANGE_KINETIC = 'Change.kinetic',
        NONE = 'none',
        UP = 'up',
        RIGHT = 'right',
        DOWN = 'down',
        LEFT = 'left',
        
     // cached variables
     attrChangeListLen = ATTR_CHANGE_LIST.length;
        
    /**
     * Label constructor.&nbsp; Labels are groups that contain Text and LabelRect shape 
     * @constructor
     * @param {Object} config
     * @param {Object} config.text Text config
     * @param {String} [config.text.fontFamily] default is Calibri
     * @param {Number} [config.text.fontSize] in pixels.  Default is 12
     * @param {String} [config.text.fontStyle] can be normal, bold, or italic.  Default is normal
     * @param {String} config.text.text 
     * @param {String} [config.text.align] can be left, center, or right
     * @param {Number} [config.text.padding]
     * @param {Number} [config.text.lineHeight] default is 1
     * @param {Object} [config.rect] LabelRect config
     * @param {String} [config.rect.pointerDirection] can be up, right, down, left, or none; the default
     *  is none.  When a pointer is present, the positioning of the label is relative to the tip of the pointer.
     * @param {Number} [config.rect.pointerWidth]
     * @param {Number} [config.rect.pointerHeight]
     * @param {Number} [config.rect.cornerRadius] 
     * @param {Number} [config.x]
     * @param {Number} [config.y]
     * @param {Number} [config.width]
     * @param {Number} [config.height]
     * @param {Boolean} [config.visible]
     * @param {Boolean} [config.listening] whether or not the node is listening for events
     * @param {String} [config.id] unique id
     * @param {String} [config.name] non-unique name
     * @param {Number} [config.opacity] determines node opacity.  Can be any number between 0 and 1
     * @param {Object} [config.scale] set scale
     * @param {Number} [config.scaleX] set scale x
     * @param {Number} [config.scaleY] set scale y
     * @param {Number} [config.rotation] rotation in radians
     * @param {Number} [config.rotationDeg] rotation in degrees
     * @param {Object} [config.offset] offset from center point and rotation point
     * @param {Number} [config.offsetX] set offset x
     * @param {Number} [config.offsetY] set offset y
     * @param {Boolean} [config.draggable] makes the node draggable.  When stages are draggable, you can drag and drop
     *  the entire stage by dragging any portion of the stage
     * @param {Function} [config.dragBoundFunc]
     */
    Kinetic.Label = function(config) {
        this._initLabel(config);
    };

    Kinetic.Label.prototype = {
        _initLabel: function(config) {
            var that = this,
                text = null;
            
            this.innerGroup = new Kinetic.Group();
            this.createAttrs();
            Kinetic.Group.call(this, config);
            text = new Kinetic.Text(config.text);
            this.setText(text);
            this.setRect(new Kinetic.LabelRect(config.rect));
            this.innerGroup.add(this.getRect());
            this.innerGroup.add(text); 
            this.add(this.innerGroup);   
            
            this._setGroupOffset();
            
            // update text data for certain attr changes
            for(var n = 0; n < attrChangeListLen; n++) {
                text.on(ATTR_CHANGE_LIST[n] + CHANGE_KINETIC, function() {
                    that._setGroupOffset();
                 });
             }     
        },
        getWidth: function() {
            return this.getText().getWidth();
        },
        getHeight: function() {
            return this.getText().getHeight();
        },
        _setGroupOffset: function() {
            var text = this.getText(),
                width = text.getWidth(),
                height = text.getHeight(),
                rect = this.getRect(),
                pointerDirection = rect.getPointerDirection(),
                pointerWidth = rect.getPointerWidth(),
                pointerHeight = rect.getPointerHeight(),
                x = 0, 
                y = 0;
            
            switch(pointerDirection) {
                case UP:
                    x = width / 2;
                    y = -1 * pointerHeight;
                    break;
                case RIGHT:
                    x = width + pointerWidth;
                    y = height / 2;
                    break;
                case DOWN:
                    x = width / 2;
                    y = height + pointerHeight;
                    break;
                case LEFT:
                    x = -1 * pointerWidth;
                    y = height / 2;
                    break;
            }
            
            this.setOffset({
                x: x,
                y: y
            }); 
        }
    };

    /**
     * get LabelRect shape for the label.  You need to access the LabelRect shape in order to update
     * the pointer properties and the corner radius
     * @name getRect
     * @methodOf Kinetic.Label.prototype
     */

    /**
     * get Text shape for the label.  You need to access the Text shape in order to update
     * the text properties
     * @name getText
     * @methodOf Kinetic.Label.prototype
     */
    
    Kinetic.Util.extend(Kinetic.Label, Kinetic.Group);
    Kinetic.Node.addGetterSetter(Kinetic.Label, 'text');
    Kinetic.Node.addGetterSetter(Kinetic.Label, 'rect');
       
    /**
     * LabelRect constructor.&nbsp; A LabelRect is similar to a Rect, except that it can be configured
     *  to have a pointer element that points up, right, down, or left 
     * @constructor
     * @param {Object} config
     * @param {String} [config.pointerDirection] can be up, right, down, left, or none; the default
     *  is none.  When a pointer is present, the positioning of the label is relative to the tip of the pointer.
     * @param {Number} [config.pointerWidth]
     * @param {Number} [config.pointerHeight]
     * @param {Number} [config.cornerRadius] 
     */ 
    Kinetic.LabelRect = function(config) {
        this._initLabelRect(config);
    };

    Kinetic.LabelRect.prototype = {
        _initLabelRect: function(config) {
            this.createAttrs();
            Kinetic.Shape.call(this, config);
            this.shapeType = 'LabelRect';
            this._setDrawFuncs();
        },
        drawFunc: function(canvas) {
            var label = this.getParent().getParent(),
                context = canvas.getContext(),
                width = label.getWidth(),
                height = label.getHeight(),
                pointerDirection = this.getPointerDirection(),
                pointerWidth = this.getPointerWidth(),
                pointerHeight = this.getPointerHeight(),
                cornerRadius = this.getCornerRadius();
                
            context.beginPath();
            context.moveTo(0,0);
            
            if (pointerDirection === UP) {
                context.lineTo((width - pointerWidth)/2, 0);
                context.lineTo(width/2, -1 * pointerHeight);
                context.lineTo((width + pointerWidth)/2, 0);
            }
            
            context.lineTo(width, 0);
           
            if (pointerDirection === RIGHT) {
                context.lineTo(width, (height - pointerHeight)/2);
                context.lineTo(width + pointerWidth, height/2);
                context.lineTo(width, (height + pointerHeight)/2);
            }
            
            context.lineTo(width, height);
    
            if (pointerDirection === DOWN) {
                context.lineTo((width + pointerWidth)/2, height);
                context.lineTo(width/2, height + pointerHeight);
                context.lineTo((width - pointerWidth)/2, height); 
            }
            
            context.lineTo(0, height);
            
            if (pointerDirection === LEFT) {
                context.lineTo(0, (height + pointerHeight)/2);
                context.lineTo(-1 * pointerWidth, height/2);
                context.lineTo(0, (height - pointerHeight)/2);
            } 
            
            context.closePath();
            canvas.fillStroke(this);
        }
    };
    
    Kinetic.Util.extend(Kinetic.LabelRect, Kinetic.Shape);
    Kinetic.Node.addGetterSetter(Kinetic.LabelRect, 'pointerDirection', NONE);

    /**
     * set pointer Direction
     * @name setPointerDirection
     * @methodOf Kinetic.LabelRect.prototype
     * @param {String} pointerDirection can be up, right, down, left, or none.  The
     *  default is none 
     */

     /**
     * get pointer Direction
     * @name getPointerDirection
     * @methodOf Kinetic.LabelRect.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.LabelRect, 'pointerWidth', 0);

    /**
     * set pointer width 
     * @name setPointerWidth
     * @methodOf Kinetic.LabelRect.prototype
     * @param {Number} pointerWidth 
     */

     /**
     * get pointer width 
     * @name getPointerWidth
     * @methodOf Kinetic.LabelRect.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.LabelRect, 'pointerHeight', 0);

    /**
     * set pointer height 
     * @name setPointerHeight
     * @methodOf Kinetic.LabelRect.prototype
     * @param {Number} pointerHeight
     */

     /**
     * get pointer height 
     * @name getPointerHeight
     * @methodOf Kinetic.LabelRect.prototype
     */

    Kinetic.Node.addGetterSetter(Kinetic.LabelRect, 'cornerRadius', 0);

    /**
     * set corner radius
     * @name setCornerRadius
     * @methodOf Kinetic.LabelRect.prototype
     * @param {Number} corner radius
     */

    /**
     * get corner radius
     * @name getCornerRadius
     * @methodOf Kinetic.LabelRect.prototype
     */
})();
(function() {
    /**
     * Grayscale Filter
     * @function
     * @memberOf Kinetic.Filters
     * @param {Object} imageData
     */
    Kinetic.Filters.Grayscale = function(imageData) {
        var data = imageData.data;
        for(var i = 0; i < data.length; i += 4) {
            var brightness = 0.34 * data[i] + 0.5 * data[i + 1] + 0.16 * data[i + 2];
            // red
            data[i] = brightness;
            // green
            data[i + 1] = brightness;
            // blue
            data[i + 2] = brightness;
        }
    };
})();

(function() {
    /**
     * Brighten Filter.  
     * @function
     * @memberOf Kinetic.Filters
     * @param {Object} imageData
     */
    Kinetic.Filters.Brighten = function(imageData) {
        var brightness = this.getFilterBrightness();
        var data = imageData.data;
        for(var i = 0; i < data.length; i += 4) {
            // red
            data[i] += brightness;
            // green
            data[i + 1] += brightness;
            // blue
            data[i + 2] += brightness;
        }
    };

    Kinetic.Node.addFilterGetterSetter(Kinetic.Image, 'filterBrightness', 0);
    /**
    * get filter brightness.  The brightness is a number between -255 and 255.&nbsp; Positive values 
    *  increase the brightness and negative values decrease the brightness, making the image darker
    * @name getFilterBrightness
    * @methodOf Kinetic.Image.prototype
    */

    /**
    * set filter brightness
    * @name setFilterBrightness
    * @methodOf Kinetic.Image.prototype
    */
})();

(function() {
    /**
     * Invert Filter
     * @function
     * @memberOf Kinetic.Filters
     * @param {Object} imageData
     */
    Kinetic.Filters.Invert = function(imageData) {
        var data = imageData.data;
        for(var i = 0; i < data.length; i += 4) {
            // red
            data[i] = 255 - data[i];
            // green
            data[i + 1] = 255 - data[i + 1];
            // blue
            data[i + 2] = 255 - data[i + 2];
        }
    };
})();

/*
 the Gauss filter
 master repo: https://github.com/pavelpower/kineticjsGaussFilter/
*/
(function() {
    /*

     StackBlur - a fast almost Gaussian Blur For Canvas

     Version:   0.5
     Author:		Mario Klingemann
     Contact: 	mario@quasimondo.com
     Website:	http://www.quasimondo.com/StackBlurForCanvas
     Twitter:	@quasimondo

     In case you find this class useful - especially in commercial projects -
     I am not totally unhappy for a small donation to my PayPal account
     mario@quasimondo.de

     Or support me on flattr:
     https://flattr.com/thing/72791/StackBlur-a-fast-almost-Gaussian-Blur-Effect-for-CanvasJavascript

     Copyright (c) 2010 Mario Klingemann

     Permission is hereby granted, free of charge, to any person
     obtaining a copy of this software and associated documentation
     files (the "Software"), to deal in the Software without
     restriction, including without limitation the rights to use,
     copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the
     Software is furnished to do so, subject to the following
     conditions:

     The above copyright notice and this permission notice shall be
     included in all copies or substantial portions of the Software.

     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
     NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
     HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
     FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
     OTHER DEALINGS IN THE SOFTWARE.
     */

    function BlurStack() {
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.a = 0;
        this.next = null;
    }

    var mul_table = [
        512,512,456,512,328,456,335,512,405,328,271,456,388,335,292,512,
        454,405,364,328,298,271,496,456,420,388,360,335,312,292,273,512,
        482,454,428,405,383,364,345,328,312,298,284,271,259,496,475,456,
        437,420,404,388,374,360,347,335,323,312,302,292,282,273,265,512,
        497,482,468,454,441,428,417,405,394,383,373,364,354,345,337,328,
        320,312,305,298,291,284,278,271,265,259,507,496,485,475,465,456,
        446,437,428,420,412,404,396,388,381,374,367,360,354,347,341,335,
        329,323,318,312,307,302,297,292,287,282,278,273,269,265,261,512,
        505,497,489,482,475,468,461,454,447,441,435,428,422,417,411,405,
        399,394,389,383,378,373,368,364,359,354,350,345,341,337,332,328,
        324,320,316,312,309,305,301,298,294,291,287,284,281,278,274,271,
        268,265,262,259,257,507,501,496,491,485,480,475,470,465,460,456,
        451,446,442,437,433,428,424,420,416,412,408,404,400,396,392,388,
        385,381,377,374,370,367,363,360,357,354,350,347,344,341,338,335,
        332,329,326,323,320,318,315,312,310,307,304,302,299,297,294,292,
        289,287,285,282,280,278,275,273,271,269,267,265,263,261,259];

    var shg_table = [
        9, 11, 12, 13, 13, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17,
        17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19,
        19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21,
        21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
        21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22,
        22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
        22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24 ];

    function filterGaussBlurRGBA( imageData, radius) {

        var pixels = imageData.data,
            width = imageData.width,
            height = imageData.height;

        var x, y, i, p, yp, yi, yw, r_sum, g_sum, b_sum, a_sum,
            r_out_sum, g_out_sum, b_out_sum, a_out_sum,
            r_in_sum, g_in_sum, b_in_sum, a_in_sum,
            pr, pg, pb, pa, rbs;

        var div = radius + radius + 1,
            widthMinus1  = width - 1,
            heightMinus1 = height - 1,
            radiusPlus1  = radius + 1,
            sumFactor = radiusPlus1 * ( radiusPlus1 + 1 ) / 2,
            stackStart = new BlurStack(),
            stack = stackStart,
            stackIn = null,
            stackOut = null,
            mul_sum = mul_table[radius],
            shg_sum = shg_table[radius];

        for ( i = 1; i < div; i++ ) {
            stack = stack.next = new BlurStack();
            if ( i == radiusPlus1 ) var stackEnd = stack;
        }

        stack.next = stackStart;

        yw = yi = 0;

        for ( y = 0; y < height; y++ )
        {
            r_in_sum = g_in_sum = b_in_sum = a_in_sum = r_sum = g_sum = b_sum = a_sum = 0;

            r_out_sum = radiusPlus1 * ( pr = pixels[yi] );
            g_out_sum = radiusPlus1 * ( pg = pixels[yi+1] );
            b_out_sum = radiusPlus1 * ( pb = pixels[yi+2] );
            a_out_sum = radiusPlus1 * ( pa = pixels[yi+3] );

            r_sum += sumFactor * pr;
            g_sum += sumFactor * pg;
            b_sum += sumFactor * pb;
            a_sum += sumFactor * pa;

            stack = stackStart;

            for( i = 0; i < radiusPlus1; i++ )
            {
                stack.r = pr;
                stack.g = pg;
                stack.b = pb;
                stack.a = pa;
                stack = stack.next;
            }

            for( i = 1; i < radiusPlus1; i++ )
            {
                p = yi + (( widthMinus1 < i ? widthMinus1 : i ) << 2 );
                r_sum += ( stack.r = ( pr = pixels[p])) * ( rbs = radiusPlus1 - i );
                g_sum += ( stack.g = ( pg = pixels[p+1])) * rbs;
                b_sum += ( stack.b = ( pb = pixels[p+2])) * rbs;
                a_sum += ( stack.a = ( pa = pixels[p+3])) * rbs;

                r_in_sum += pr;
                g_in_sum += pg;
                b_in_sum += pb;
                a_in_sum += pa;

                stack = stack.next;
            }


            stackIn = stackStart;
            stackOut = stackEnd;
            for ( x = 0; x < width; x++ )
            {
                pixels[yi+3] = pa = (a_sum * mul_sum) >> shg_sum;
                if ( pa != 0 )
                {
                    pa = 255 / pa;
                    pixels[yi]   = ((r_sum * mul_sum) >> shg_sum) * pa;
                    pixels[yi+1] = ((g_sum * mul_sum) >> shg_sum) * pa;
                    pixels[yi+2] = ((b_sum * mul_sum) >> shg_sum) * pa;
                } else {
                    pixels[yi] = pixels[yi+1] = pixels[yi+2] = 0;
                }

                r_sum -= r_out_sum;
                g_sum -= g_out_sum;
                b_sum -= b_out_sum;
                a_sum -= a_out_sum;

                r_out_sum -= stackIn.r;
                g_out_sum -= stackIn.g;
                b_out_sum -= stackIn.b;
                a_out_sum -= stackIn.a;

                p =  ( yw + ( ( p = x + radius + 1 ) < widthMinus1 ? p : widthMinus1 ) ) << 2;

                r_in_sum += ( stackIn.r = pixels[p]);
                g_in_sum += ( stackIn.g = pixels[p+1]);
                b_in_sum += ( stackIn.b = pixels[p+2]);
                a_in_sum += ( stackIn.a = pixels[p+3]);

                r_sum += r_in_sum;
                g_sum += g_in_sum;
                b_sum += b_in_sum;
                a_sum += a_in_sum;

                stackIn = stackIn.next;

                r_out_sum += ( pr = stackOut.r );
                g_out_sum += ( pg = stackOut.g );
                b_out_sum += ( pb = stackOut.b );
                a_out_sum += ( pa = stackOut.a );

                r_in_sum -= pr;
                g_in_sum -= pg;
                b_in_sum -= pb;
                a_in_sum -= pa;

                stackOut = stackOut.next;

                yi += 4;
            }
            yw += width;
        }


        for ( x = 0; x < width; x++ )
        {
            g_in_sum = b_in_sum = a_in_sum = r_in_sum = g_sum = b_sum = a_sum = r_sum = 0;

            yi = x << 2;
            r_out_sum = radiusPlus1 * ( pr = pixels[yi]);
            g_out_sum = radiusPlus1 * ( pg = pixels[yi+1]);
            b_out_sum = radiusPlus1 * ( pb = pixels[yi+2]);
            a_out_sum = radiusPlus1 * ( pa = pixels[yi+3]);

            r_sum += sumFactor * pr;
            g_sum += sumFactor * pg;
            b_sum += sumFactor * pb;
            a_sum += sumFactor * pa;

            stack = stackStart;

            for( i = 0; i < radiusPlus1; i++ )
            {
                stack.r = pr;
                stack.g = pg;
                stack.b = pb;
                stack.a = pa;
                stack = stack.next;
            }

            yp = width;

            for( i = 1; i <= radius; i++ )
            {
                yi = ( yp + x ) << 2;

                r_sum += ( stack.r = ( pr = pixels[yi])) * ( rbs = radiusPlus1 - i );
                g_sum += ( stack.g = ( pg = pixels[yi+1])) * rbs;
                b_sum += ( stack.b = ( pb = pixels[yi+2])) * rbs;
                a_sum += ( stack.a = ( pa = pixels[yi+3])) * rbs;

                r_in_sum += pr;
                g_in_sum += pg;
                b_in_sum += pb;
                a_in_sum += pa;

                stack = stack.next;

                if( i < heightMinus1 )
                {
                    yp += width;
                }
            }

            yi = x;
            stackIn = stackStart;
            stackOut = stackEnd;
            for ( y = 0; y < height; y++ )
            {
                p = yi << 2;
                pixels[p+3] = pa = (a_sum * mul_sum) >> shg_sum;
                if ( pa > 0 )
                {
                    pa = 255 / pa;
                    pixels[p]   = ((r_sum * mul_sum) >> shg_sum ) * pa;
                    pixels[p+1] = ((g_sum * mul_sum) >> shg_sum ) * pa;
                    pixels[p+2] = ((b_sum * mul_sum) >> shg_sum ) * pa;
                } else {
                    pixels[p] = pixels[p+1] = pixels[p+2] = 0;
                }

                r_sum -= r_out_sum;
                g_sum -= g_out_sum;
                b_sum -= b_out_sum;
                a_sum -= a_out_sum;

                r_out_sum -= stackIn.r;
                g_out_sum -= stackIn.g;
                b_out_sum -= stackIn.b;
                a_out_sum -= stackIn.a;

                p = ( x + (( ( p = y + radiusPlus1) < heightMinus1 ? p : heightMinus1 ) * width )) << 2;

                r_sum += ( r_in_sum += ( stackIn.r = pixels[p]));
                g_sum += ( g_in_sum += ( stackIn.g = pixels[p+1]));
                b_sum += ( b_in_sum += ( stackIn.b = pixels[p+2]));
                a_sum += ( a_in_sum += ( stackIn.a = pixels[p+3]));

                stackIn = stackIn.next;

                r_out_sum += ( pr = stackOut.r );
                g_out_sum += ( pg = stackOut.g );
                b_out_sum += ( pb = stackOut.b );
                a_out_sum += ( pa = stackOut.a );

                r_in_sum -= pr;
                g_in_sum -= pg;
                b_in_sum -= pb;
                a_in_sum -= pa;

                stackOut = stackOut.next;

                yi += width;
            }
        }
    }

    /**
     * Blur Filter
     * @function
     * @memberOf Kinetic.Filters
     * @param {Object} imageData
     */
    Kinetic.Filters.Blur = function(imageData) {
        var radius = this.getFilterRadius() | 0;

        if (radius > 0) {
            filterGaussBlurRGBA(imageData, radius);
        }
    };

    Kinetic.Node.addFilterGetterSetter(Kinetic.Image, 'filterRadius', 0);

})();

(function() {

	function pixelAt(idata, x, y) {
		var idx = (y * idata.width + x) * 4;
		var d = [];
		d.push(idata.data[idx++], idata.data[idx++], idata.data[idx++], idata.data[idx++]);
		return d;
	};

	function rgbDistance(p1, p2) {
		return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2) + Math.pow(p1[2] - p2[2], 2));
	};

	function rgbMean(pTab) {
		var m = [0, 0, 0];

		for (var i = 0; i < pTab.length; i++) {
			m[0] += pTab[i][0];
			m[1] += pTab[i][1];
			m[2] += pTab[i][2];
		}

		m[0] /= pTab.length;
		m[1] /= pTab.length;
		m[2] /= pTab.length;

		return m;
	};

	function backgroundMask(idata, threshold) {
		var rgbv_no = pixelAt(idata, 0, 0);
		var rgbv_ne = pixelAt(idata, idata.width - 1, 0);
		var rgbv_so = pixelAt(idata, 0, idata.height - 1);
		var rgbv_se = pixelAt(idata, idata.width - 1, idata.height - 1);


		var thres = threshold || 10; 
		if (rgbDistance(rgbv_no, rgbv_ne) < thres && rgbDistance(rgbv_ne, rgbv_se) < thres && rgbDistance(rgbv_se, rgbv_so) < thres && rgbDistance(rgbv_so, rgbv_no) < thres) {

			// Mean color
			var mean = rgbMean([rgbv_ne, rgbv_no, rgbv_se, rgbv_so]);

			// Mask based on color distance
			var mask = [];
			for (var i = 0; i < idata.width * idata.height; i++) {
				var d = rgbDistance(mean, [idata.data[i * 4], idata.data[i * 4 + 1], idata.data[i * 4 + 2]]);
				mask[i] = (d < thres) ? 0 : 255;
			}

			return mask;
		}
	};

	function applyMask(idata, mask) {
		for (var i = 0; i < idata.width * idata.height; i++) {
			idata.data[4 * i + 3] = mask[i];
		}
	};

	function erodeMask(mask, sw, sh) {

		var weights = [1, 1, 1, 1, 0, 1, 1, 1, 1];
		var side = Math.round(Math.sqrt(weights.length));
		var halfSide = Math.floor(side / 2);

		var maskResult = [];
		for (var y = 0; y < sh; y++) {
			for (var x = 0; x < sw; x++) {

				var so = y * sw + x;
				var a = 0;
				for (var cy = 0; cy < side; cy++) {
					for (var cx = 0; cx < side; cx++) {
						var scy = y + cy - halfSide;
						var scx = x + cx - halfSide;

						if (scy >= 0 && scy < sh && scx >= 0 && scx < sw) {

							var srcOff = scy * sw + scx;
							var wt = weights[cy * side + cx];

							a += mask[srcOff] * wt;
						}
					}
				}

				maskResult[so] = (a === 255 * 8) ? 255 : 0;
			}
		}

		return maskResult;
	};

	function dilateMask(mask, sw, sh) {

		var weights = [1, 1, 1, 1, 1, 1, 1, 1, 1];
		var side = Math.round(Math.sqrt(weights.length));
		var halfSide = Math.floor(side / 2);

		var maskResult = [];
		for (var y = 0; y < sh; y++) {
			for (var x = 0; x < sw; x++) {

				var so = y * sw + x;
				var a = 0;
				for (var cy = 0; cy < side; cy++) {
					for (var cx = 0; cx < side; cx++) {
						var scy = y + cy - halfSide;
						var scx = x + cx - halfSide;

						if (scy >= 0 && scy < sh && scx >= 0 && scx < sw) {

							var srcOff = scy * sw + scx;
							var wt = weights[cy * side + cx];

							a += mask[srcOff] * wt;
						}
					}
				}

				maskResult[so] = (a >= 255 * 4) ? 255 : 0;
			}
		}

		return maskResult;
	};

	function smoothEdgeMask(mask, sw, sh) {

		var weights = [1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9];
		var side = Math.round(Math.sqrt(weights.length));
		var halfSide = Math.floor(side / 2);

		var maskResult = [];
		for (var y = 0; y < sh; y++) {
			for (var x = 0; x < sw; x++) {

				var so = y * sw + x;
				var a = 0;
				for (var cy = 0; cy < side; cy++) {
					for (var cx = 0; cx < side; cx++) {
						var scy = y + cy - halfSide;
						var scx = x + cx - halfSide;

						if (scy >= 0 && scy < sh && scx >= 0 && scx < sw) {

							var srcOff = scy * sw + scx;
							var wt = weights[cy * side + cx];

							a += mask[srcOff] * wt;
						}
					}
				}

				maskResult[so] = a;
			}
		}

		return maskResult;
	}
	
	/**
	 * Mask Filter
	 *
	 * Only crop unicolor background images for instance
	 *
	 * @function
	 * @memberOf Kinetic.Filters
	 * @param {Object} imageData
	 */
	Kinetic.Filters.Mask = function(idata) {
		// Detect pixels close to the background color
		var threshold = this.getFilterThreshold(),
		    mask = backgroundMask(idata, threshold);
		if (mask) {
			// Erode
			mask = erodeMask(mask, idata.width, idata.height);

			// Dilate
			mask = dilateMask(mask, idata.width, idata.height);

			// Gradient
			mask = smoothEdgeMask(mask, idata.width, idata.height);

			// Apply mask
			applyMask(idata, mask);
			
			// todo : Update hit region function according to mask
		}

		return idata;
	};

	Kinetic.Node.addFilterGetterSetter(Kinetic.Image, 'filterThreshold', 0);

	//threshold The RGB euclidian distance threshold (default : 10) 

})();

