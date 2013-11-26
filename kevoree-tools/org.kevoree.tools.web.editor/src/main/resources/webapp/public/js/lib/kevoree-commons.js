!function(e){"object"==typeof exports?module.exports=e():"function"==typeof define&&define.amd?define(e):"undefined"!=typeof window?window.kevoreeCommons=e():"undefined"!=typeof global?global.kevoreeCommons=e():"undefined"!=typeof self&&(self.kevoreeCommons=e())}(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);throw new Error("Cannot find module '"+o+"'")}var f=n[o]={exports:{}};t[o][0].call(f.exports,function(e){var n=t[o][1][e];return s(n?n:e)},f,f.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
module.exports.Resolver         = require('./lib/Resolver');
module.exports.Bootstrapper     = require('./lib/Bootstrapper');
module.exports.KevoreeLogger    = require('./lib/KevoreeLogger');
module.exports.FileSystem       = require('./lib/FileSystem');
},{"./lib/Bootstrapper":2,"./lib/FileSystem":3,"./lib/KevoreeLogger":4,"./lib/Resolver":5}],2:[function(require,module,exports){
var Class = require('pseudoclass');

/**
 * Bootstrapper API
 * @type {Bootstrapper}
 */
var Bootstrapper = Class({
    toString: 'Bootstrapper',

    /**
     *
     * @param nodeName
     * @param model
     * @param callback
     */
    bootstrapNodeType: function (nodeName, model, callback) {
        callback = callback || function () {};

        var nodeInstance = model.findNodesByID(nodeName);
        if (nodeInstance != undefined && nodeInstance != null) {
            var deployUnits = nodeInstance.typeDefinition.deployUnits;
            if (deployUnits.size() > 0) {
                // bootstrap node deploy unit
                this.bootstrap(deployUnits.get(0), callback);

            } else {
                callback.call(this, new Error("'"+nodeName+"' NodeType deploy units not found. Have you forgotten to merge nodetype library ?"));
                return;
            }
        } else {
            callback.call(this, new Error("Unable to find '"+nodeName+"' in the given model."));
            return;
        }
    },

    /**
     *
     * @param deployUnit
     * @param callback
     */
    bootstrap: function (deployUnit, callback) {},

    /**
     *
     * @param deployUnit
     * @param callback
     */
    uninstall: function (deployUnit, callback) {}
});

module.exports = Bootstrapper;
},{"pseudoclass":7}],3:[function(require,module,exports){
var Class         = require('pseudoclass');

var FileSystem = Class({
    toString: 'FileSystem',

    getFileSystem: function (size, callback) {
        if (document) {
            getBrowserFileSystem(this, size, callback);
        } else {
            console.error('Kevoree FileSystem API only handles Browser FS for now.');
        }
    }
});

var getBrowserFileSystem = function getBrowserFileSystem(fsapi, size, callback) {
    window.requestFileSystem = window.requestFileSystem || window.webkitRequestFileSystem;
    navigator.persistentStorage = navigator.persistentStorage || navigator.webkitPersistentStorage;

    if (window.requestFileSystem && navigator.persistentStorage) {

        var successHandler = function successHandler(grantedSize) {
            window.requestFileSystem(window.PERSISTENT, grantedSize, function (fs) {
                callback.call(fsapi, null, fs);
            });
        }

        var errorHandler = function errorHandler(e) {
            callback.call(fsapi, null);
        }

        navigator.persistentStorage.requestQuota(size, successHandler, errorHandler);
    }
}

module.exports = FileSystem;
},{"pseudoclass":7}],4:[function(require,module,exports){
var Class  = require('pseudoclass'),
    colors = require('colors');

var KevoreeLogger = Class({
  toString: 'KevoreeLogger',

  construct: function (tag) {
    this.tag = tag;
  },

  info: function (tag, msg) {
    if (typeof(msg) == 'undefined') {
      msg = tag;
      tag = this.tag;
    }
    console.log(getTime()+'  '+'INFO'.grey+'   '+processTag(tag)+'  '+msg.grey);
  },

  warn: function (tag, msg) {
    if (typeof(msg) == 'undefined') {
      msg = tag;
      tag = this.tag;
    }
    console.warn(getTime()+'  '+'WARN'.grey.yellowBG+'   '+processTag(tag)+'  '+msg.yellow);
  },

  error: function (tag, msg) {
    if (typeof(msg) == 'undefined') {
      msg = tag;
      tag = this.tag;
    }
    console.error(getTime()+'  '+'ERROR'.white.redBG+'  '+processTag(tag)+'  '+msg.red);
  },

  debug: function (tag, msg) {
    if (typeof(msg) == 'undefined') {
      msg = tag;
      tag = this.tag;
    }
    console.log(getTime()+'  '+'DEBUG '.cyan+' '+processTag(tag)+'  '+msg.cyan);
  }
});

var processTag = function processTag(tag) {
  if (tag.length >= 15) {
    tag = tag.substr(0, 14)+'.';
  } else {
    var spaces = '';
    for (var i=0; i < 15 - tag.length; i++) spaces += ' ';
    tag += spaces;
  }

  return (tag+'').magenta;
}

var getTime = function getTime() {
  var time = new Date;
  var hours = (time.getHours().toString().length == 1) ? '0'+time.getHours() : time.getHours();
  var mins = (time.getMinutes().toString().length == 1) ? '0'+time.getMinutes() : time.getMinutes();
  var secs = (time.getSeconds().toString().length == 1) ? '0'+time.getSeconds() : time.getSeconds();
  return (hours+':'+mins+':'+secs).grey;
}

module.exports = KevoreeLogger;
},{"colors":6,"pseudoclass":7}],5:[function(require,module,exports){
var Class = require('pseudoclass');

/**
 * Resolver API
 * @type {Resolver}
 */
var Resolver = Class({
    toString: 'Resolver',

    /**
     *
     * @param deployUnit Kevoree DeployUnit
     * @param callback(err, Class)
     */
    resolve: function (deployUnit, callback) {},

    uninstall: function (deployUnit, callback) {}
});

module.exports = Resolver;
},{"pseudoclass":7}],6:[function(require,module,exports){
/*
colors.js

Copyright (c) 2010

Marak Squires
Alexis Sellier (cloudhead)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

*/

var isHeadless = false;

if (typeof module !== 'undefined') {
  isHeadless = true;
}

if (!isHeadless) {
  var exports = {};
  var module = {};
  var colors = exports;
  exports.mode = "browser";
} else {
  exports.mode = "console";
}

//
// Prototypes the string object to have additional method calls that add terminal colors
//
var addProperty = function (color, func) {
  exports[color] = function (str) {
    return func.apply(str);
  };
  String.prototype.__defineGetter__(color, func);
};

function stylize(str, style) {

  var styles;

  if (exports.mode === 'console') {
    styles = {
      //styles
      'bold'      : ['\x1B[1m',  '\x1B[22m'],
      'italic'    : ['\x1B[3m',  '\x1B[23m'],
      'underline' : ['\x1B[4m',  '\x1B[24m'],
      'inverse'   : ['\x1B[7m',  '\x1B[27m'],
      'strikethrough' : ['\x1B[9m',  '\x1B[29m'],
      //text colors
      //grayscale
      'white'     : ['\x1B[37m', '\x1B[39m'],
      'grey'      : ['\x1B[90m', '\x1B[39m'],
      'black'     : ['\x1B[30m', '\x1B[39m'],
      //colors
      'blue'      : ['\x1B[34m', '\x1B[39m'],
      'cyan'      : ['\x1B[36m', '\x1B[39m'],
      'green'     : ['\x1B[32m', '\x1B[39m'],
      'magenta'   : ['\x1B[35m', '\x1B[39m'],
      'red'       : ['\x1B[31m', '\x1B[39m'],
      'yellow'    : ['\x1B[33m', '\x1B[39m'],
      //background colors
      //grayscale
      'whiteBG'     : ['\x1B[47m', '\x1B[49m'],
      'greyBG'      : ['\x1B[49;5;8m', '\x1B[49m'],
      'blackBG'     : ['\x1B[40m', '\x1B[49m'],
      //colors
      'blueBG'      : ['\x1B[44m', '\x1B[49m'],
      'cyanBG'      : ['\x1B[46m', '\x1B[49m'],
      'greenBG'     : ['\x1B[42m', '\x1B[49m'],
      'magentaBG'   : ['\x1B[45m', '\x1B[49m'],
      'redBG'       : ['\x1B[41m', '\x1B[49m'],
      'yellowBG'    : ['\x1B[43m', '\x1B[49m']
    };
  } else if (exports.mode === 'browser') {
    styles = {
      //styles
      'bold'      : ['<b>',  '</b>'],
      'italic'    : ['<i>',  '</i>'],
      'underline' : ['<u>',  '</u>'],
      'inverse'   : ['<span style="background-color:black;color:white;">',  '</span>'],
      'strikethrough' : ['<del>',  '</del>'],
      //text colors
      //grayscale
      'white'     : ['<span style="color:white;">',   '</span>'],
      'grey'      : ['<span style="color:gray;">',    '</span>'],
      'black'     : ['<span style="color:black;">',   '</span>'],
      //colors
      'blue'      : ['<span style="color:blue;">',    '</span>'],
      'cyan'      : ['<span style="color:cyan;">',    '</span>'],
      'green'     : ['<span style="color:green;">',   '</span>'],
      'magenta'   : ['<span style="color:magenta;">', '</span>'],
      'red'       : ['<span style="color:red;">',     '</span>'],
      'yellow'    : ['<span style="color:yellow;">',  '</span>'],
      //background colors
      //grayscale
      'whiteBG'     : ['<span style="background-color:white;">',   '</span>'],
      'greyBG'      : ['<span style="background-color:gray;">',    '</span>'],
      'blackBG'     : ['<span style="background-color:black;">',   '</span>'],
      //colors
      'blueBG'      : ['<span style="background-color:blue;">',    '</span>'],
      'cyanBG'      : ['<span style="background-color:cyan;">',    '</span>'],
      'greenBG'     : ['<span style="background-color:green;">',   '</span>'],
      'magentaBG'   : ['<span style="background-color:magenta;">', '</span>'],
      'redBG'       : ['<span style="background-color:red;">',     '</span>'],
      'yellowBG'    : ['<span style="background-color:yellow;">',  '</span>']
    };
  } else if (exports.mode === 'none') {
    return str + '';
  } else {
    console.log('unsupported mode, try "browser", "console" or "none"');
  }
  return styles[style][0] + str + styles[style][1];
}

function applyTheme(theme) {

  //
  // Remark: This is a list of methods that exist
  // on String that you should not overwrite.
  //
  var stringPrototypeBlacklist = [
    '__defineGetter__', '__defineSetter__', '__lookupGetter__', '__lookupSetter__', 'charAt', 'constructor',
    'hasOwnProperty', 'isPrototypeOf', 'propertyIsEnumerable', 'toLocaleString', 'toString', 'valueOf', 'charCodeAt',
    'indexOf', 'lastIndexof', 'length', 'localeCompare', 'match', 'replace', 'search', 'slice', 'split', 'substring',
    'toLocaleLowerCase', 'toLocaleUpperCase', 'toLowerCase', 'toUpperCase', 'trim', 'trimLeft', 'trimRight'
  ];

  Object.keys(theme).forEach(function (prop) {
    if (stringPrototypeBlacklist.indexOf(prop) !== -1) {
      console.log('warn: '.red + ('String.prototype' + prop).magenta + ' is probably something you don\'t want to override. Ignoring style name');
    }
    else {
      if (typeof(theme[prop]) === 'string') {
        addProperty(prop, function () {
          return exports[theme[prop]](this);
        });
      }
      else {
        addProperty(prop, function () {
          var ret = this;
          for (var t = 0; t < theme[prop].length; t++) {
            ret = exports[theme[prop][t]](ret);
          }
          return ret;
        });
      }
    }
  });
}


//
// Iterate through all default styles and colors
//
var x = ['bold', 'underline', 'strikethrough', 'italic', 'inverse', 'grey', 'black', 'yellow', 'red', 'green', 'blue', 'white', 'cyan', 'magenta', 'greyBG', 'blackBG', 'yellowBG', 'redBG', 'greenBG', 'blueBG', 'whiteBG', 'cyanBG', 'magentaBG'];
x.forEach(function (style) {

  // __defineGetter__ at the least works in more browsers
  // http://robertnyman.com/javascript/javascript-getters-setters.html
  // Object.defineProperty only works in Chrome
  addProperty(style, function () {
    return stylize(this, style);
  });
});

function sequencer(map) {
  return function () {
    if (!isHeadless) {
      return this.replace(/( )/, '$1');
    }
    var exploded = this.split(""), i = 0;
    exploded = exploded.map(map);
    return exploded.join("");
  };
}

var rainbowMap = (function () {
  var rainbowColors = ['red', 'yellow', 'green', 'blue', 'magenta']; //RoY G BiV
  return function (letter, i, exploded) {
    if (letter === " ") {
      return letter;
    } else {
      return stylize(letter, rainbowColors[i++ % rainbowColors.length]);
    }
  };
})();

exports.themes = {};

exports.addSequencer = function (name, map) {
  addProperty(name, sequencer(map));
};

exports.addSequencer('rainbow', rainbowMap);
exports.addSequencer('zebra', function (letter, i, exploded) {
  return i % 2 === 0 ? letter : letter.inverse;
});

exports.setTheme = function (theme) {
  if (typeof theme === 'string') {
    try {
      exports.themes[theme] = require(theme);
      applyTheme(exports.themes[theme]);
      return exports.themes[theme];
    } catch (err) {
      console.log(err);
      return err;
    }
  } else {
    applyTheme(theme);
  }
};


addProperty('stripColors', function () {
  return ("" + this).replace(/\x1B\[\d+m/g, '');
});

// please no
function zalgo(text, options) {
  var soul = {
    "up" : [
      '̍', '̎', '̄', '̅',
      '̿', '̑', '̆', '̐',
      '͒', '͗', '͑', '̇',
      '̈', '̊', '͂', '̓',
      '̈', '͊', '͋', '͌',
      '̃', '̂', '̌', '͐',
      '̀', '́', '̋', '̏',
      '̒', '̓', '̔', '̽',
      '̉', 'ͣ', 'ͤ', 'ͥ',
      'ͦ', 'ͧ', 'ͨ', 'ͩ',
      'ͪ', 'ͫ', 'ͬ', 'ͭ',
      'ͮ', 'ͯ', '̾', '͛',
      '͆', '̚'
    ],
    "down" : [
      '̖', '̗', '̘', '̙',
      '̜', '̝', '̞', '̟',
      '̠', '̤', '̥', '̦',
      '̩', '̪', '̫', '̬',
      '̭', '̮', '̯', '̰',
      '̱', '̲', '̳', '̹',
      '̺', '̻', '̼', 'ͅ',
      '͇', '͈', '͉', '͍',
      '͎', '͓', '͔', '͕',
      '͖', '͙', '͚', '̣'
    ],
    "mid" : [
      '̕', '̛', '̀', '́',
      '͘', '̡', '̢', '̧',
      '̨', '̴', '̵', '̶',
      '͜', '͝', '͞',
      '͟', '͠', '͢', '̸',
      '̷', '͡', ' ҉'
    ]
  },
  all = [].concat(soul.up, soul.down, soul.mid),
  zalgo = {};

  function randomNumber(range) {
    var r = Math.floor(Math.random() * range);
    return r;
  }

  function is_char(character) {
    var bool = false;
    all.filter(function (i) {
      bool = (i === character);
    });
    return bool;
  }

  function heComes(text, options) {
    var result = '', counts, l;
    options = options || {};
    options["up"] = options["up"] || true;
    options["mid"] = options["mid"] || true;
    options["down"] = options["down"] || true;
    options["size"] = options["size"] || "maxi";
    text = text.split('');
    for (l in text) {
      if (is_char(l)) {
        continue;
      }
      result = result + text[l];
      counts = {"up" : 0, "down" : 0, "mid" : 0};
      switch (options.size) {
      case 'mini':
        counts.up = randomNumber(8);
        counts.min = randomNumber(2);
        counts.down = randomNumber(8);
        break;
      case 'maxi':
        counts.up = randomNumber(16) + 3;
        counts.min = randomNumber(4) + 1;
        counts.down = randomNumber(64) + 3;
        break;
      default:
        counts.up = randomNumber(8) + 1;
        counts.mid = randomNumber(6) / 2;
        counts.down = randomNumber(8) + 1;
        break;
      }

      var arr = ["up", "mid", "down"];
      for (var d in arr) {
        var index = arr[d];
        for (var i = 0 ; i <= counts[index]; i++) {
          if (options[index]) {
            result = result + soul[index][randomNumber(soul[index].length)];
          }
        }
      }
    }
    return result;
  }
  return heComes(text);
}


// don't summon zalgo
addProperty('zalgo', function () {
  return zalgo(this);
});

},{}],7:[function(require,module,exports){
/*
	Class - JavaScript inheritance

	Construction:
		Setup and construction should happen in the construct() method.
		The construct() method is automatically chained, so all construct() methods defined by superclass methods will be called first.

	Initialization:
		Initialziation that needs to happen after all construct() methods have been called should be done in the init() method.
		The init() method is not automatically chained, so you must call _super() if you intend to call the superclass' init method.
		init() is not passed any arguments

	Destruction:
		Teardown and destruction should happen in the destruct() method. The destruct() method is also chained.

	Mixins:
		An array of mixins can be provided with the mixins[] property. An object or the prototype of a class should be provided, not a constructor.
		Mixins can be added at any time by calling this.mixin(properties)

	Usage:
		var MyClass = Class(properties);
		var MyClass = new Class(properties);
		var MyClass = Class.extend(properties);

	Credits:
		Inspired by Simple JavaScript Inheritance by John Resig http://ejohn.org/

	Usage differences:
		construct() is used to setup instances and is automatically chained so superclass construct() methods run automatically
		destruct() is used  to tear down instances. destruct() is also chained
		init(), if defined, is called after construction is complete and is not chained
		toString() can be defined as a string or a function
		mixin() is provided to mix properties into an instance
		properties.mixins as an array results in each of the provided objects being mixed in (last object wins)
		_super is passed as an argument (not as this._super) and can be used asynchronously
*/
(function(global) {
	// Used for default initialization methods
	var noop = function() {};

	// Given a function, the superTest RE will match if _super is the first argument to a function
	// The function will be serialized, then the serialized string will be searched for _super
	// If the environment isn't capable of function serialization, make it so superTest.test always returns true
	var superTest = /xyz/.test(function(){return 'xyz';}) ? /\(\s*_super\b/ : { test: function() { return true; } };

	// Remove the _super function from the passed arguments array
	var removeSuper = function(args, _super) {
		// For performance, first check if at least one argument was passed
		if (args && args.length && args[0] === _super)
			args = Array.prototype.slice.call(args, 1);
		return args;
	};

	// Bind an overriding method such that it gets the overridden method as its first argument
	var superify = function(name, func, superPrototype, isStatic) {
		var _super;

		// We redefine _super.apply so _super is stripped from the passed arguments array
		// This allows implementors to call _super.apply(this, arguments) without manually stripping off _super
		if (isStatic) {
			// Static binding: If the passed superPrototype is modified, the bound function will still call the ORIGINAL method
			// This comes into play when functions are mixed into an object that already has a function by that name (i.e. two mixins are used)
			var superFunc = superPrototype[name];
			_super = function _superStatic() {
				return superFunc.apply(this, arguments);
			};

			_super.apply = function _applier(context, args) {
				return Function.prototype.apply.call(superFunc, context, removeSuper(args, _super));
			};
		}
		else {
			// Dynamic binding: If the passed superPrototype is modified, the bound function will call the NEW method
			// This comes into play when functions are mixed into a class at declaration time
			_super = function _superDynamic() {
				return superPrototype[name].apply(this, arguments);
			};

			_super.apply = function _applier(context, args) {
				return Function.prototype.apply.call(superPrototype[name], context, removeSuper(args, _super));
			};
		}

		// Name the function for better stack traces
		return function _passSuper() {
			// Add the super function to the start of the arguments array
			var args = Array.prototype.slice.call(arguments);
			args.unshift(_super);

			// Call the function with the modified arguments
			return func.apply(this, args);
		};
	};

	// Mix the provided properties into the current context with the ability to call overridden methods with _super()
	var mixin = function(properties, superPrototype) {
		// Use this instance
		superPrototype = superPrototype || this.constructor && this.constructor.prototype;
		
		// Copy the properties onto the new prototype
		for (var name in properties) {
			// Never mix construct or destruct
			if (name === 'construct' || name === 'destruct')
				continue;

			// Check if the function uses _super
			// It should be a function, the super prototype should have a function by the same name
			// And finally, the function should take _super as its first argument
			var usesSuper = superPrototype && typeof properties[name] === 'function' && typeof superPrototype[name] === 'function' && superTest.test(properties[name]);

			if (usesSuper) {
				// Wrap the function such that _super will be passed accordingly
				if (this.hasOwnProperty(name))
					this[name] = superify(name, properties[name], this, true);
				else
					this[name] = superify(name, properties[name], superPrototype, false);
			}
			else {
				// Directly assign the property
				this[name] = properties[name];
			}
		}
	};

	// The base Class implementation acts as extend alias, with the exception that it can take properties.extend as the Class to extend
	var Class = function(properties) {
		// If a class-like object is passed as properties.extend, just call extend on it
		if (properties && properties.extend)
			return properties.extend.extend(properties);

		// Otherwise, just create a new class with the passed properties
		return Class.extend(properties);
	};
	
	// Add the mixin method to all classes created with Class
	Class.prototype.mixin = mixin;
	
	// Creates a new Class that inherits from this class
	// Give the function a name so it can refer to itself without arguments.callee
	Class.extend = function extend(properties) {
		var superPrototype = this.prototype;
		
		// Create an object with the prototype of the superclass
		var prototype = Object.create(superPrototype);
		
		if (properties) {
			// Mix the new properties into the class prototype
			// This does not copy construct and destruct
			mixin.call(prototype, properties, superPrototype);
			
			// Mix in all the mixins
			// This also does not copy construct and destruct
			if (Array.isArray(properties.mixins)) {
				for (var i = 0, ni = properties.mixins.length; i < ni; i++) {
					// Mixins should be _super enabled, with the methods defined in the prototype as the superclass methods
					mixin.call(prototype, properties.mixins[i], prototype);
				}
			}
			
			// Chain the construct() method (supermost executes first) if necessary
			if (properties.construct && superPrototype.construct) {
				prototype.construct = function() {
					superPrototype.construct.apply(this, arguments);
					properties.construct.apply(this, arguments);
				};
			}
			else if (properties.construct)
				prototype.construct = properties.construct;
			
			// Chain the destruct() method in reverse order (supermost executes last) if necessary
			if (properties.destruct && superPrototype.destruct) {
				prototype.destruct = function() {
					properties.destruct.apply(this, arguments);
					superPrototype.destruct.apply(this, arguments);
				};
			}
			else if (properties.destruct)
				prototype.destruct = properties.destruct;
			
			// Allow definition of toString as a string (turn it into a function)
			if (typeof properties.toString === 'string') {
				var className = properties.toString;
				prototype.toString = function() { return className; };
			}
		}

		// Define construct and init as noops if undefined
		// This serves to avoid conditionals inside of the constructor
		if (typeof prototype.construct !== 'function')
			prototype.construct = noop;
		if (typeof prototype.init !== 'function')
			prototype.init = noop;

		// The constructor handles creating an instance of the class, applying mixins, and calling construct() and init() methods
		function Class() {
			// Optimization: Requiring the new keyword and avoiding usage of Object.create() increases performance by 5x
			if (this instanceof Class === false) {
				throw new Error('Cannot create instance without new operator');
			}
			
			// Optimization: Avoiding conditionals in constructor increases performance of instantiation by 2x
			this.construct.apply(this, arguments);
			this.init();
		}

		// Assign prototype.constructor to the constructor itself
		// This allows instances to refer to this.constructor.prototype
		// This also allows creation of new instances using instance.constructor()
		prototype.constructor = Class;

		// Store the superPrototype
		// It will be accessible on an instance as follows:
		//	instance.superPrototype
		//	instance.constructor.prototype.superPrototype
		prototype.superPrototype = superPrototype;

		// Store the extended class' prototype as the prototype of the constructor
		Class.prototype = prototype;

		// Add extend() as a static method on the constructor
		Class.extend = extend;

		return Class;
	};
	
	if (typeof module !== 'undefined' && module.exports) {
		// Node.js Support
		module.exports = Class;
	}
	else if (typeof global.define === 'function') {
		(function(define) {
			// AMD Support
			define(function() { return Class; });
		}(global.define));
	}
	else {
		// Browser support
		global.Class = Class;
	}
}(this));

},{}]},{},[1])
(1)
});
;