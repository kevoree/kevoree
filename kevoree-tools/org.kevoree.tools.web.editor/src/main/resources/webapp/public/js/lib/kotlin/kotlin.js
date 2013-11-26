/*  Prototype JavaScript framework, version 1.6.1
 *  (c) 2005-2009 Sam Stephenson
 *
 *  Prototype is freely distributable under the terms of an MIT-style license.
 *  For details, see the Prototype web site: http://www.prototypejs.org/
 *
 *--------------------------------------------------------------------------*/
var Kotlin = {};

(function () {
    "use strict";
    var emptyFunction = function () {
    };

    if (!Array.isArray) {
        Array.isArray = function (vArg) {
            return Object.prototype.toString.call(vArg) === "[object Array]";
        };
    }

    if (!Function.prototype.bind) {
        Function.prototype.bind = function (oThis) {
            if (typeof this !== "function") {
                // closest thing possible to the ECMAScript 5 internal IsCallable function
                throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
            }

            var aArgs = Array.prototype.slice.call(arguments, 1),
                fToBind = this,
                fNOP = function () {
                },
                fBound = function () {
                    return fToBind.apply(this instanceof fNOP && oThis
                                             ? this
                                             : oThis,
                                         aArgs.concat(Array.prototype.slice.call(arguments)));
                };

            fNOP.prototype = this.prototype;
            fBound.prototype = new fNOP();

            return fBound;
        };
    }

    Kotlin.keys = Object.keys || function (o) {
        var result = [];
        var i = 0;
        for (var p in o) {
            if (o.hasOwnProperty(p)) {
                result[i++] = p;
            }
        }
        return result;
    };

    function copyProperties(to, from) {
        for (var p in from) {
            if (from.hasOwnProperty(p)) {
                to[p] = from[p];
            }
        }
    }

    Kotlin.isType = function (object, klass) {
        if (object === null || object === undefined) {
            return false;
        }

        var current = object.get_class();
        while (current !== klass) {
            if (current === null || current === undefined) {
                return false;
            }
            current = current.superclass;
        }
        return true;
    };

    Kotlin.createTrait = function () {
        var n = arguments.length - 1;
        var result = arguments[n] || {};
        for (var i = 0; i < n; i++) {
            copyProperties(result, arguments[i]);
        }
        return result;
    };

    Kotlin.definePackage = function (members) {
        return members === null ? {} : members;
    };

    Kotlin.createClass = (function () {
        function subclass() {
        }

        function create(parent, properties, staticProperties) {
            var traits = null;
            if (parent instanceof Array) {
                traits = parent;
                parent = parent[0];
            }

            function klass() {
                this.initializing = klass;
                if (this.initialize) {
                    this.initialize.apply(this, arguments);
                }
            }

            klass.addMethods = addMethods;
            klass.superclass = parent || null;
            klass.subclasses = [];
            klass.object$ = object$;

            if (parent) {
                if (typeof (parent) == "function") {
                    subclass.prototype = parent.prototype;
                    klass.prototype = new subclass();
                    parent.subclasses.push(klass);
                }
                else {
                    // trait
                    klass.addMethods(parent);
                }
            }

            klass.addMethods({get_class: function () {
                return klass;
            }});

            if (parent !== null) {
                klass.addMethods({super_init: function () {
                    this.initializing = this.initializing.superclass;
                    this.initializing.prototype.initialize.apply(this, arguments);
                }});
            }

            if (traits !== null) {
                for (var i = 1, n = traits.length; i < n; i++) {
                    klass.addMethods(traits[i]);
                }
            }
            if (properties !== null && properties !== undefined) {
                klass.addMethods(properties);
            }

            if (!klass.prototype.initialize) {
                klass.prototype.initialize = emptyFunction;
            }

            klass.prototype.constructor = klass;
            if (staticProperties !== null && staticProperties !== undefined) {
                copyProperties(klass, staticProperties);
            }
            return klass;
        }

        function addMethods(source) {
            copyProperties(this.prototype, source);
            return this;
        }

        function object$() {
            if (typeof this.$object$ === "undefined") {
                this.$object$ = this.object_initializer$();
            }

            return this.$object$;
        }

        return create;
    })();

    Kotlin.$createClass = function (parent, properties) {
        if (parent !== null && typeof (parent) != "function") {
            properties = parent;
            parent = null;
        }
        return Kotlin.createClass(parent, properties, null);
    };

    Kotlin.createObjectWithPrototype = function (prototype) {
        function C() {}
        C.prototype = prototype;
        return new C();
    };

    Kotlin.$new = function (f) {
        var o = Kotlin.createObjectWithPrototype(f.prototype);
        return function () {
            f.apply(o, arguments);
            return o;
        };
    };

    Kotlin.createObject = function () {
        var singletonClass = Kotlin.createClass.apply(null, arguments);
        return new singletonClass();
    };

    Kotlin.defineModule = function (id, module) {
        if (id in Kotlin.modules) {
            throw Kotlin.$new(Kotlin.IllegalArgumentException)();
        }

        Kotlin.modules[id] = module;
    };
})();


// TODO drop this:
(function () {
    'use strict';

    if (!Array.isArray) {
        Array.isArray = function (vArg) {
            return Object.prototype.toString.call(vArg) === "[object Array]";
        };
    }

    if (!Function.prototype.bind) {
        Function.prototype.bind = function (oThis) {
            if (typeof this !== "function") {
                // closest thing possible to the ECMAScript 5 internal IsCallable function
                throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
            }

            var aArgs = Array.prototype.slice.call(arguments, 1),
                fToBind = this,
                fNOP = function () {
                },
                fBound = function () {
                    return fToBind.apply(this instanceof fNOP && oThis
                                             ? this
                                             : oThis,
                                         aArgs.concat(Array.prototype.slice.call(arguments)));
                };

            fNOP.prototype = this.prototype;
            fBound.prototype = new fNOP();

            return fBound;
        };
    }

    if (!Object.keys) {
        Object.keys = function (o) {
            var result = [];
            var i = 0;
            for (var p in o) {
                if (o.hasOwnProperty(p)) {
                    result[i++] = p;
                }
            }
            return result;
        };
    }

    if (!Object.create) {
        Object.create = function(proto) {
            function F() {}
            F.prototype = proto;
            return new F();
        }
    }

    // http://ejohn.org/blog/objectgetprototypeof/
    if ( typeof Object.getPrototypeOf !== "function" ) {
        if ( typeof "test".__proto__ === "object" ) {
            Object.getPrototypeOf = function(object){
                return object.__proto__;
            };
        } else {
            Object.getPrototypeOf = function(object){
                // May break if the constructor has been tampered with
                return object.constructor.prototype;
            };
        }
    }
})();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

var Kotlin = {};

(function () {

    function toArray(obj) {
        var array;
        if (obj == null) {
            array = [];
        }
        else if(!Array.isArray(obj)) {
            array = [obj];
        }
        else {
            array = obj;
        }
        return array;
    }

    function copyProperties(to, from) {
        if (to == null || from == null) {
            return;
        }
        for (var p in from) {
            if (from.hasOwnProperty(p)) {
                to[p] = from[p];
            }
        }
    }

    function getClass(basesArray) {
        for (var i = 0; i < basesArray.length; i++) {
            if (isNativeClass(basesArray[i]) || basesArray[i].$metadata$.type === Kotlin.TYPE.CLASS) {
                return basesArray[i];
            }
        }
        return null;
    }

    var emptyFunction = function() {
        return function() {};
    };

    Kotlin.TYPE = {
        CLASS: "class",
        TRAIT: "trait",
        OBJECT: "object"
    };

    Kotlin.classCount = 0;
    Kotlin.newClassIndex = function() {
        var tmp = Kotlin.classCount;
        Kotlin.classCount++;
        return tmp;
    };

    function isNativeClass(obj) {
        return !(obj == null) && obj.$metadata$ == null;
    }

    function applyExtension(current, bases, baseGetter) {
        for (var i = 0; i < bases.length; i++) {
            if (isNativeClass(bases[i])) {
                continue;
            }
            var base = baseGetter(bases[i]);
            for (var p in  base) {
                if (base.hasOwnProperty(p)) {
                    if(!current.hasOwnProperty(p) || current[p].$classIndex$ < base[p].$classIndex$) {
                        current[p] = base[p];
                    }
                }
            }
        }
    }

    function computeMetadata(bases, properties) {
        var metadata = {};

        metadata.baseClasses = toArray(bases);
        metadata.baseClass = getClass(metadata.baseClasses);
        metadata.classIndex = Kotlin.newClassIndex();
        metadata.functions = {};
        metadata.properties = {};

        if (!(properties == null)) {
            for (var p in properties) {
                if (properties.hasOwnProperty(p)) {
                    var property = properties[p];
                    property.$classIndex$ = metadata.classIndex;
                    if (typeof property === "function") {
                        metadata.functions[p] = property;
                    } else {
                        metadata.properties[p] = property;
                    }
                }
            }
        }
        applyExtension(metadata.functions, metadata.baseClasses, function (it) {
            return it.$metadata$.functions
        });
        applyExtension(metadata.properties, metadata.baseClasses, function (it) {
            return it.$metadata$.properties
        });

        return metadata;
    }

    function class_object() {
        var object = this.object_initializer$();
        Object.defineProperty(this, "object", {value: object});
        return object;
    }

    Kotlin.createClass = function (bases, constructor, properties, staticProperties) {
        if (constructor == null) {
            constructor = emptyFunction();
        }
        copyProperties(constructor, staticProperties);

        var metadata = computeMetadata(bases, properties);
        metadata.type = Kotlin.TYPE.CLASS;

        var prototypeObj;
        if (metadata.baseClass !== null) {
            prototypeObj = Object.create(metadata.baseClass.prototype);
        } else {
            prototypeObj = {};
        }
        Object.defineProperties(prototypeObj, metadata.properties);
        copyProperties(prototypeObj, metadata.functions);
        prototypeObj.constructor = constructor;

        if (metadata.baseClass != null) {
            constructor.baseInitializer = metadata.baseClass;
        }

        constructor.$metadata$ = metadata;
        constructor.prototype = prototypeObj;
        Object.defineProperty(constructor, "object", {get: class_object, configurable: true});
        return constructor;
    };

    Kotlin.createObject = function (bases, constructor, functions) {
        var noNameClass = Kotlin.createClass(bases, constructor, functions);
        var obj = new noNameClass();
        obj.$metadata$ = {
            type: Kotlin.TYPE.OBJECT
        };
        return  obj;
    };

    Kotlin.createTrait = function (bases, properties, staticProperties) {
        var obj = function () {};
        copyProperties(obj, staticProperties);

        obj.$metadata$ = computeMetadata(bases, properties);
        obj.$metadata$.type = Kotlin.TYPE.TRAIT;
        return obj;
    };

    function isInheritanceFromTrait (objConstructor, trait) {
        if (isNativeClass(objConstructor) || objConstructor.$metadata$.classIndex < trait.$metadata$.classIndex) {
            return false;
        }
        var baseClasses = objConstructor.$metadata$.baseClasses;
        var i;
        for (i = 0; i < baseClasses.length; i++) {
            if (baseClasses[i] === trait) {
                return true;
            }
        }
        for (i = 0; i < baseClasses.length; i++) {
            if (isInheritanceFromTrait(baseClasses[i], trait)) {
                return true;
            }
        }
        return false;
    }

    Kotlin.isType = function (object, klass) {
        if (object == null || klass == null) {
            return false;
        } else {
            if (object instanceof klass) {
                return true;
            }
            else if (isNativeClass(klass) || klass.$metadata$.type == Kotlin.TYPE.CLASS) {
                return false;
            }
            else {
                return isInheritanceFromTrait(object.constructor, klass);
            }
        }
    };


////////////////////////////////// packages & modules //////////////////////////////

    function createPackageGetter(instance, initializer) {
        return function () {
            if (initializer !== null) {
                var tmp = initializer;
                initializer = null;
                tmp.call(instance);
            }

            return instance;
        };
    }

    function createDefinition(members) {
        var definition = {};
        if (members == null) {
            return definition;
        }
        for (var p in members) {
            if (members.hasOwnProperty(p)) {
                if ((typeof members[p]) === "function") {
                    definition[p] = members[p];
                } else {
                    Object.defineProperty(definition, p, members[p]);
                }
            }
        }
        return definition;
    }

    Kotlin.definePackage = function (initializer, members) {
        var definition = createDefinition(members);
        if (initializer === null) {
            return {value: definition};
        }
        else {
            var getter = createPackageGetter(definition, initializer);
            return {get: getter};
        }
    };

    Kotlin.defineRootPackage = function (initializer, members) {
        var definition = createDefinition(members);

        if (initializer === null) {
            definition.$initializer$ = emptyFunction();
        } else {
            definition.$initializer$ = initializer;
        }
        return definition;
      };

    Kotlin.defineModule = function (id, declaration) {
        if (id in Kotlin.modules) {
            throw new Error("Module " + id + " is already defined");
        }
        declaration.$initializer$.call(declaration); // TODO: temporary hack
        Object.defineProperty(Kotlin.modules, id, {value: declaration});
    };

})();




/**
 * Copyright 2010 Tim Down.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";

// todo inlined
String.prototype.startsWith = function (s) {
  return this.indexOf(s) === 0;
};

String.prototype.endsWith = function (s) {
  return this.indexOf(s, this.length - s.length) !== -1;
};

String.prototype.contains = function (s) {
  return this.indexOf(s) !== -1;
};

(function () {
    Kotlin.equals = function (obj1, obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }

        if (Array.isArray(obj1)) {
            return Kotlin.arrayEquals(obj1, obj2);
        }

        if (typeof obj1 == "object" && obj1.equals !== undefined) {
            return obj1.equals(obj2);
        }

        return obj1 === obj2;
    };

    Kotlin.toString = function (o) {
        if (o == null) {
            return "null";
        }
        else if (Array.isArray(o)) {
            return Kotlin.arrayToString(o);
        }
        else {
            return o.toString();
        }
    };
    
    Kotlin.arrayToString = function(a) {
        return "[" + a.join(", ") + "]";
    };

    Kotlin.intUpto = function (from, to) {
        return new Kotlin.NumberRange(from, to);
    };

    Kotlin.intDownto = function (from, to) {
        return new Kotlin.Progression(from, to, -1);
    };

    Kotlin.modules = {};

    Kotlin.RuntimeException = Kotlin.createClass();
    Kotlin.NullPointerException = Kotlin.createClass();
    Kotlin.NoSuchElementException = Kotlin.createClass();
    Kotlin.IllegalArgumentException = Kotlin.createClass();
    Kotlin.IllegalStateException = Kotlin.createClass();
    Kotlin.UnsupportedOperationException = Kotlin.createClass();
    Kotlin.IOException = Kotlin.createClass();

    Kotlin.throwNPE = function () {
        throw new Kotlin.NullPointerException();
    };

    function throwAbstractFunctionInvocationError(funName) {
        return function() {
            var message;
            if (funName !== undefined) {
                message = "Function " + funName + " is abstract";
            } else {
                message = "Function is abstract";
            }
            throw new TypeError(message);
        };
    }

    Kotlin.Iterator = Kotlin.createClass(null, null, {
        next: throwAbstractFunctionInvocationError("Iterator#next"),
        hasNext: throwAbstractFunctionInvocationError("Iterator#hasNext")
    });

    var ArrayIterator = Kotlin.createClass(Kotlin.Iterator,
        function (array) {
            this.array = array;
            this.size = array.length;
            this.index = 0;
        }, {
            next: function () {
                return this.array[this.index++];
            },
            hasNext: function () {
                return this.index < this.size;
            }
    });

    var ListIterator = Kotlin.createClass(ArrayIterator,
        function (list) {
            this.list = list;
            this.size = list.size();
            this.index = 0;
        }, {
            next: function () {
                return this.list.get(this.index++);
            }
    });

    Kotlin.Collection = Kotlin.createClass();

    Kotlin.Enum = Kotlin.createClass(null,
        function () {
            this.name$ = undefined;
            this.ordinal$ = undefined;
        }, {
            name: function () {
                return this.name$;
            },
            ordinal: function () {
                return this.ordinal$;
            },
            toString: function () {
                return this.name();
            }
    });
    (function (){
        function valueOf(name) {
            return this[name];
        }
        function getValues() {
            return this.values$;
        }

        Kotlin.createEnumEntries = function(enumEntryList) {
            var i = 0;
            var values = [];
            for (var entryName in enumEntryList) {
                if (enumEntryList.hasOwnProperty(entryName)) {
                    var entryObject = enumEntryList[entryName];
                    values[i] = entryObject;
                    entryObject.ordinal$ = i;
                    entryObject.name$ = entryName;
                    i++;
                }
            }
            enumEntryList.values$ = values;
            enumEntryList.valueOf = valueOf;
            enumEntryList.values = getValues;
            return enumEntryList;
        };
    })();

    Kotlin.PropertyMetadata = Kotlin.createClass(null,
        function(name) {
            this.name = name;
        }
    );

    Kotlin.AbstractCollection = Kotlin.createClass(Kotlin.Collection, null, {
        size: function () {
            return this.$size;
        },
        addAll: function (collection) {
            var it = collection.iterator();
            var i = this.size();
            while (i-- > 0) {
                this.add(it.next());
            }
        },
        isEmpty: function () {
            return this.size() === 0;
        },
        iterator: function () {
            return new ArrayIterator(this.toArray());
        },
        equals: function (o) {
            if (this.size() !== o.size()) return false;

            var iterator1 = this.iterator();
            var iterator2 = o.iterator();
            var i = this.size();
            while (i-- > 0) {
                if (!Kotlin.equals(iterator1.next(), iterator2.next())) {
                    return false;
                }
            }

            return true;
        },
        toString: function () {
            var builder = "[";
            var iterator = this.iterator();
            var first = true;
            var i = this.$size;
            while (i-- > 0) {
                if (first) {
                    first = false;
                }
                else {
                    builder += ", ";
                }
                builder += iterator.next();
            }
            builder += "]";
            return builder;
        },
        toJSON: function () {
            return this.toArray();
        }
    });

    Kotlin.AbstractList = Kotlin.createClass(Kotlin.AbstractCollection, null, {
        iterator: function () {
            return new ListIterator(this);
        },
        remove: function (o) {
            var index = this.indexOf(o);
            if (index !== -1) {
                this.removeAt(index);
            }
        },
        contains: function (o) {
            return this.indexOf(o) !== -1;
        }
    });

    //TODO: should be JS Array-like (https://developer.mozilla.org/en-US/docs/JavaScript/Guide/Predefined_Core_Objects#Working_with_Array-like_objects)
    Kotlin.ArrayList = Kotlin.createClass(Kotlin.AbstractList,
        function () {
            this.array = [];
            this.$size = 0;
        }, {
            get: function (index) {
                this.checkRange(index);
                return this.array[index];
            },
            set: function (index, value) {
                this.checkRange(index);
                this.array[index] = value;
            },
            size: function () {
                return this.$size;
            },
            iterator: function () {
                return Kotlin.arrayIterator(this.array);
            },
            add: function (element) {
                this.array[this.$size++] = element;
            },
            addAt: function (index, element) {
                this.array.splice(index, 0, element);
                this.$size++;
            },
            addAll: function (collection) {
                var it = collection.iterator();
                for (var i = this.$size, n = collection.size(); n-- > 0;) {
                    this.array[i++] = it.next();
                }

                this.$size += collection.size();
            },
            removeAt: function (index) {
                this.checkRange(index);
                this.$size--;
                return this.array.splice(index, 1)[0];
            },
            clear: function () {
                this.array.length = 0;
                this.$size = 0;
            },
            indexOf: function (o) {
                for (var i = 0, n = this.$size; i < n; ++i) {
                    if (Kotlin.equals(this.array[i], o)) {
                        return i;
                    }
                }
                return -1;
            },
            toArray: function () {
                return this.array.slice(0, this.$size);
            },
            toString: function () {
                return "[" + this.array.join(", ") + "]";
            },
            toJSON: function () {
                return this.array;
            },
            checkRange: function(index) {
                if (index < 0 || index >= this.$size) {
                    throw new RangeError();
                }
            }
    });

    Kotlin.Runnable = Kotlin.createClass(null, null, {
        run: throwAbstractFunctionInvocationError("Runnable#run")
    });

    Kotlin.Comparable = Kotlin.createClass(null, null, {
        compareTo: throwAbstractFunctionInvocationError("Comparable#compareTo")
    });

    Kotlin.Appendable = Kotlin.createClass(null, null, {
        append: throwAbstractFunctionInvocationError("Appendable#append")
    });

    Kotlin.Closeable = Kotlin.createClass(null, null, {
        close: throwAbstractFunctionInvocationError("Closeable#close")
    });

    Kotlin.safeParseInt = function(str) {
        var r = parseInt(str, 10);
        return isNaN(r) ? null : r;
    };

    Kotlin.safeParseDouble = function(str) {
        var r = parseFloat(str);
        return isNaN(r) ? null : r;
    };

    Kotlin.arrayEquals = function (a, b) {
        if (a === b) {
            return true;
        }
        if (!Array.isArray(b) || a.length !== b.length) {
            return false;
        }

        for (var i = 0, n = a.length; i < n; i++) {
            if (!Kotlin.equals(a[i], b[i])) {
                return false;
            }
        }
        return true;
    };

    Kotlin.System = function () {
        var output = "";

        var print = function (obj) {
            if (obj !== undefined) {
                if (obj === null || typeof obj !== "object") {
                    output += obj;
                }
                else {
                    output += obj.toString();
                }
            }
        };
        var println = function (obj) {
            this.print(obj);
            output += "\n";
        };

        return {
            out: function () {
                return {
                    print: print,
                    println: println
                };
            },
            output: function () {
                return output;
            },
            flush: function () {
                output = "";
            }
        };
    }();

    Kotlin.println = function (s) {
        Kotlin.System.out().println(s);
    };

    Kotlin.print = function (s) {
        Kotlin.System.out().print(s);
    };

    Kotlin.RangeIterator = Kotlin.createClass(Kotlin.Iterator,
        function (start, end, increment) {
            this.start = start;
            this.end = end;
            this.increment = increment;
            this.i = start;
        }, {
            next: function () {
                var value = this.i;
                this.i = this.i + this.increment;
                return value;
            },
            hasNext: function () {
                return this.i <= this.end;
            }
    });

    Kotlin.NumberRange = Kotlin.createClass(null,
        function (start, end) {
            this.start = start;
            this.end = end;
            this.increment = 1;
        }, {
            contains: function (number) {
                return this.start <= number && number <= this.end;
            },
            iterator: function () {
                return new Kotlin.RangeIterator(this.start, this.end);
            }
    });

    Kotlin.Progression = Kotlin.createClass(null,
        function (start, end, increment) {
            this.start = start;
            this.end = end;
            this.increment = increment;
        }, {
        iterator: function () {
            return new Kotlin.RangeIterator(this.start, this.end, this.increment);
        }
    });

    Kotlin.Comparator = Kotlin.createClass(null, null, {
        compare: throwAbstractFunctionInvocationError("Comparator#compare")
    });

    var ComparatorImpl = Kotlin.createClass(Kotlin.Comparator,
        function (comparator) {
            this.compare = comparator;
        }
    );

    Kotlin.comparator = function (f) {
        return new ComparatorImpl(f);
    };

    Kotlin.collectionsMax = function (c, comp) {
        if (c.isEmpty()) {
            //TODO: which exception?
            throw new Error();
        }
        var it = c.iterator();
        var max = it.next();
        while (it.hasNext()) {
            var el = it.next();
            if (comp.compare(max, el) < 0) {
                max = el;
            }
        }
        return max;
    };

    Kotlin.collectionsSort = function (mutableList, comparator) {
        var boundComparator = undefined;
        if (comparator !== undefined) {
            boundComparator = comparator.compare.bind(comparator);
        }

        if (mutableList instanceof Array) {
            mutableList.sort(boundComparator);
        }

        //TODO: should be deleted when List will be JS Array-like (https://developer.mozilla.org/en-US/docs/JavaScript/Guide/Predefined_Core_Objects#Working_with_Array-like_objects)
        var array = [];
        var it = mutableList.iterator();
        while (it.hasNext()) {
            array.push(it.next());
        }

        array.sort(boundComparator);

        for (var i = 0, n = array.length; i < n; i++) {
            mutableList.set(i, array[i]);
        }
    };

    Kotlin.copyToArray = function (collection) {
        var array = [];
        var it = collection.iterator();
        while (it.hasNext()) {
            array.push(it.next());
        }

        return array;
    };


    Kotlin.StringBuilder = Kotlin.createClass(null,
        function () {
            this.string = "";
        }, {
        append:function (obj) {
            this.string = this.string + obj.toString();
        },
        toString:function () {
            return this.string;
        }
    });

    Kotlin.splitString = function (str, regex, limit) {
        return str.split(new RegExp(regex), limit);
    };

    Kotlin.nullArray = function (size) {
        var res = [];
        var i = size;
        while (i > 0) {
            res[--i] = null;
        }
        return res;
    };

    Kotlin.numberArrayOfSize = function (size) {
        return Kotlin.arrayFromFun(size, function(){ return 0; });
    };

    Kotlin.charArrayOfSize = function (size) {
        return Kotlin.arrayFromFun(size, function(){ return '\0'; });
    };

    Kotlin.booleanArrayOfSize = function (size) {
        return Kotlin.arrayFromFun(size, function(){ return false; });
    };

    Kotlin.arrayFromFun = function (size, initFun) {
        var result = new Array(size);
        for (var i = 0; i < size; i++) {
            result[i] = initFun(i);
        }
        return result;
    };

    Kotlin.arrayIndices = function (arr) {
        return new Kotlin.NumberRange(0, arr.length - 1);
    };

    Kotlin.arrayIterator = function (array) {
        return new ArrayIterator(array);
    };

    Kotlin.jsonFromTuples = function (pairArr) {
        var i = pairArr.length;
        var res = {};
        while (i > 0) {
            --i;
            res[pairArr[i][0]] = pairArr[i][1];
        }
        return res;
    };

    Kotlin.jsonAddProperties = function (obj1, obj2) {
        for (var p in obj2) {
            if (obj2.hasOwnProperty(p)) {
                obj1[p] = obj2[p];
            }
        }
        return obj1;
    };
})();

Kotlin.assignOwner = function(f, o) {
  f.o = o;
  return f;
};

/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";
(function () {
    var FUNCTION = "function";
    var arrayRemoveAt = (typeof Array.prototype.splice == FUNCTION) ?
                        function (arr, idx) {
                            arr.splice(idx, 1);
                        } :

                        function (arr, idx) {
                            var itemsAfterDeleted, i, len;
                            if (idx === arr.length - 1) {
                                arr.length = idx;
                            }
                            else {
                                itemsAfterDeleted = arr.slice(idx + 1);
                                arr.length = idx;
                                for (i = 0, len = itemsAfterDeleted.length; i < len; ++i) {
                                    arr[idx + i] = itemsAfterDeleted[i];
                                }
                            }
                        };

    function hashObject(obj) {
        var hashCode;
        if (typeof obj == "string") {
            return obj;
        }
        else if (typeof obj.hashCode == FUNCTION) {
            // Check the hashCode method really has returned a string
            hashCode = obj.hashCode();
            return (typeof hashCode == "string") ? hashCode : hashObject(hashCode);
        }
        else if (typeof obj.toString == FUNCTION) {
            return obj.toString();
        }
        else {
            try {
                return String(obj);
            }
            catch (ex) {
                // For host objects (such as ActiveObjects in IE) that have no toString() method and throw an error when
                // passed to String()
                return Object.prototype.toString.call(obj);
            }
        }
    }

    function equals_fixedValueHasEquals(fixedValue, variableValue) {
        return fixedValue.equals(variableValue);
    }

    function equals_fixedValueNoEquals(fixedValue, variableValue) {
        return (typeof variableValue.equals == FUNCTION) ?
               variableValue.equals(fixedValue) : (fixedValue === variableValue);
    }

    function createKeyValCheck(kvStr) {
        return function (kv) {
            if (kv === null) {
                throw new Error("null is not a valid " + kvStr);
            }
            else if (typeof kv == "undefined") {
                throw new Error(kvStr + " must not be undefined");
            }
        };
    }

    var checkKey = createKeyValCheck("key"), checkValue = createKeyValCheck("value");

    function Bucket(hash, firstKey, firstValue, equalityFunction) {
        this[0] = hash;
        this.entries = [];
        this.addEntry(firstKey, firstValue);

        if (equalityFunction !== null) {
            this.getEqualityFunction = function () {
                return equalityFunction;
            };
        }
    }

    var EXISTENCE = 0, ENTRY = 1, ENTRY_INDEX_AND_VALUE = 2;

    function createBucketSearcher(mode) {
        return function (key) {
            var i = this.entries.length, entry, equals = this.getEqualityFunction(key);
            while (i--) {
                entry = this.entries[i];
                if (equals(key, entry[0])) {
                    switch (mode) {
                        case EXISTENCE:
                            return true;
                        case ENTRY:
                            return entry;
                        case ENTRY_INDEX_AND_VALUE:
                            return [ i, entry[1] ];
                    }
                }
            }
            return false;
        };
    }

    function createBucketLister(entryProperty) {
        return function (aggregatedArr) {
            var startIndex = aggregatedArr.length;
            for (var i = 0, len = this.entries.length; i < len; ++i) {
                aggregatedArr[startIndex + i] = this.entries[i][entryProperty];
            }
        };
    }

    Bucket.prototype = {
        getEqualityFunction: function (searchValue) {
            return (typeof searchValue.equals == FUNCTION) ? equals_fixedValueHasEquals : equals_fixedValueNoEquals;
        },

        getEntryForKey: createBucketSearcher(ENTRY),

        getEntryAndIndexForKey: createBucketSearcher(ENTRY_INDEX_AND_VALUE),

        removeEntryForKey: function (key) {
            var result = this.getEntryAndIndexForKey(key);
            if (result) {
                arrayRemoveAt(this.entries, result[0]);
                return result[1];
            }
            return null;
        },

        addEntry: function (key, value) {
            this.entries[this.entries.length] = [key, value];
        },

        keys: createBucketLister(0),

        values: createBucketLister(1),

        getEntries: function (entries) {
            var startIndex = entries.length;
            for (var i = 0, len = this.entries.length; i < len; ++i) {
                // Clone the entry stored in the bucket before adding to array
                entries[startIndex + i] = this.entries[i].slice(0);
            }
        },

        containsKey: createBucketSearcher(EXISTENCE),

        containsValue: function (value) {
            var i = this.entries.length;
            while (i--) {
                if (value === this.entries[i][1]) {
                    return true;
                }
            }
            return false;
        }
    };

    /*----------------------------------------------------------------------------------------------------------------*/

    // Supporting functions for searching hashtable buckets

    function searchBuckets(buckets, hash) {
        var i = buckets.length, bucket;
        while (i--) {
            bucket = buckets[i];
            if (hash === bucket[0]) {
                return i;
            }
        }
        return null;
    }

    function getBucketForHash(bucketsByHash, hash) {
        var bucket = bucketsByHash[hash];

        // Check that this is a genuine bucket and not something inherited from the bucketsByHash's prototype
        return ( bucket && (bucket instanceof Bucket) ) ? bucket : null;
    }

    /*----------------------------------------------------------------------------------------------------------------*/

    var Hashtable = function (hashingFunctionParam, equalityFunctionParam) {
        var that = this;
        var buckets = [];
        var bucketsByHash = {};

        var hashingFunction = (typeof hashingFunctionParam == FUNCTION) ? hashingFunctionParam : hashObject;
        var equalityFunction = (typeof equalityFunctionParam == FUNCTION) ? equalityFunctionParam : null;

        this.put = function (key, value) {
            checkKey(key);
            checkValue(value);
            var hash = hashingFunction(key), bucket, bucketEntry, oldValue = null;

            // Check if a bucket exists for the bucket key
            bucket = getBucketForHash(bucketsByHash, hash);
            if (bucket) {
                // Check this bucket to see if it already contains this key
                bucketEntry = bucket.getEntryForKey(key);
                if (bucketEntry) {
                    // This bucket entry is the current mapping of key to value, so replace old value and we're done.
                    oldValue = bucketEntry[1];
                    bucketEntry[1] = value;
                }
                else {
                    // The bucket does not contain an entry for this key, so add one
                    bucket.addEntry(key, value);
                }
            }
            else {
                // No bucket exists for the key, so create one and put our key/value mapping in
                bucket = new Bucket(hash, key, value, equalityFunction);
                buckets[buckets.length] = bucket;
                bucketsByHash[hash] = bucket;
            }
            return oldValue;
        };

        this.get = function (key) {
            checkKey(key);

            var hash = hashingFunction(key);

            // Check if a bucket exists for the bucket key
            var bucket = getBucketForHash(bucketsByHash, hash);
            if (bucket) {
                // Check this bucket to see if it contains this key
                var bucketEntry = bucket.getEntryForKey(key);
                if (bucketEntry) {
                    // This bucket entry is the current mapping of key to value, so return the value.
                    return bucketEntry[1];
                }
            }
            return null;
        };

        this.containsKey = function (key) {
            checkKey(key);
            var bucketKey = hashingFunction(key);

            // Check if a bucket exists for the bucket key
            var bucket = getBucketForHash(bucketsByHash, bucketKey);

            return bucket ? bucket.containsKey(key) : false;
        };

        this.containsValue = function (value) {
            checkValue(value);
            var i = buckets.length;
            while (i--) {
                if (buckets[i].containsValue(value)) {
                    return true;
                }
            }
            return false;
        };

        this.clear = function () {
            buckets.length = 0;
            bucketsByHash = {};
        };

        this.isEmpty = function () {
            return !buckets.length;
        };

        var createBucketAggregator = function (bucketFuncName) {
            return function () {
                var aggregated = [], i = buckets.length;
                while (i--) {
                    buckets[i][bucketFuncName](aggregated);
                }
                return aggregated;
            };
        };

        this._keys = createBucketAggregator("keys");
        this._values = createBucketAggregator("values");
        this._entries = createBucketAggregator("getEntries");

        this.values = function () {
            var values = this._values();
            var i = values.length;
            var result = new Kotlin.ArrayList();
            while (i--) {
                result.add(values[i]);
            }
            return result;
        };

        this.remove = function (key) {
            checkKey(key);

            var hash = hashingFunction(key), bucketIndex, oldValue = null;

            // Check if a bucket exists for the bucket key
            var bucket = getBucketForHash(bucketsByHash, hash);

            if (bucket) {
                // Remove entry from this bucket for this key
                oldValue = bucket.removeEntryForKey(key);
                if (oldValue !== null) {
                    // Entry was removed, so check if bucket is empty
                    if (!bucket.entries.length) {
                        // Bucket is empty, so remove it from the bucket collections
                        bucketIndex = searchBuckets(buckets, hash);
                        arrayRemoveAt(buckets, bucketIndex);
                        delete bucketsByHash[hash];
                    }
                }
            }
            return oldValue;
        };

        this.size = function () {
            var total = 0, i = buckets.length;
            while (i--) {
                total += buckets[i].entries.length;
            }
            return total;
        };

        this.each = function (callback) {
            var entries = that._entries(), i = entries.length, entry;
            while (i--) {
                entry = entries[i];
                callback(entry[0], entry[1]);
            }
        };


        this.putAll = function (hashtable, conflictCallback) {
            var entries = hashtable._entries();
            var entry, key, value, thisValue, i = entries.length;
            var hasConflictCallback = (typeof conflictCallback == FUNCTION);
            while (i--) {
                entry = entries[i];
                key = entry[0];
                value = entry[1];

                // Check for a conflict. The default behaviour is to overwrite the value for an existing key
                if (hasConflictCallback && (thisValue = that.get(key))) {
                    value = conflictCallback(key, thisValue, value);
                }
                that.put(key, value);
            }
        };

        this.clone = function () {
            var clone = new Hashtable(hashingFunctionParam, equalityFunctionParam);
            clone.putAll(that);
            return clone;
        };

        this.keySet = function () {
            var res = new Kotlin.ComplexHashSet();
            var keys = this._keys();
            var i = keys.length;
            while (i--) {
                res.add(keys[i]);
            }
            return res;
        };
    };


    Kotlin.HashTable = Hashtable;
})();

Kotlin.Map = Kotlin.createClass();

Kotlin.HashMap = Kotlin.createClass(Kotlin.Map,
    function () {
        Kotlin.HashTable.call(this);
    }
);

Kotlin.ComplexHashMap = Kotlin.HashMap;

(function () {
    var PrimitiveHashMapValuesIterator = Kotlin.createClass(Kotlin.Iterator,
        function (map, keys) {
            this.map = map;
            this.keys = keys;
            this.size = keys.length;
            this.index = 0;
        }, {
            next: function () {
                return this.map[this.keys[this.index++]];
            },
            hasNext: function () {
                return this.index < this.size;
            }
    });

    var PrimitiveHashMapValues = Kotlin.createClass(Kotlin.Collection,
        function (map) {
            this.map = map;
        }, {
            iterator: function () {
                return new PrimitiveHashMapValuesIterator(this.map.map, Object.keys(this.map.map));
            },
            isEmpty: function () {
                return this.map.$size === 0;
            },
            contains: function (o) {
                return this.map.containsValue(o);
            }
    });

    Kotlin.PrimitiveHashMap = Kotlin.createClass(Kotlin.Map,
        function () {
            this.$size = 0;
            this.map = {};
        }, {
            size: function () {
                return this.$size;
            },
            isEmpty: function () {
                return this.$size === 0;
            },
            containsKey: function (key) {
                return this.map[key] !== undefined;
            },
            containsValue: function (value) {
                var map = this.map;
                for (var key in map) {
                    if (map.hasOwnProperty(key) && map[key] === value) {
                        return true;
                    }
                }

                return false;
            },
            get: function (key) {
                return this.map[key];
            },
            put: function (key, value) {
                var prevValue = this.map[key];
                this.map[key] = value === undefined ? null : value;
                if (prevValue === undefined) {
                    this.$size++;
                }
                return prevValue;
            },
            remove: function (key) {
                var prevValue = this.map[key];
                if (prevValue !== undefined) {
                    delete this.map[key];
                    this.$size--;
                }
                return prevValue;
            },
            clear: function () {
                this.$size = 0;
                this.map = {};
            },
            putAll: function (fromMap) {
                var map = fromMap.map;
                for (var key in map) {
                    if (map.hasOwnProperty(key)) {
                        this.map[key] = map[key];
                        this.$size++;
                    }
                }
            },
            keySet: function () {
                var result = new Kotlin.PrimitiveHashSet();
                var map = this.map;
                for (var key in map) {
                    if (map.hasOwnProperty(key)) {
                        result.add(key);
                    }
                }

                return result;
            },
            values: function () {
                return new PrimitiveHashMapValues(this);
            },
            toJSON: function () {
                return this.map;
            }
    });
}());

Kotlin.Set = Kotlin.createClass(Kotlin.Collection);

Kotlin.PrimitiveHashSet = Kotlin.createClass(Kotlin.AbstractCollection,
    function () {
        this.$size = 0;
        this.map = {};
    }, {
        contains: function (key) {
            return this.map[key] === true;
        },
        add: function (element) {
            var prevElement = this.map[element];
            this.map[element] = true;
            if (prevElement === true) {
                return false;
            }
            else {
                this.$size++;
                return true;
            }
        },
        remove: function (element) {
            if (this.map[element] === true) {
                delete this.map[element];
                this.$size--;
                return true;
            }
            else {
                return false;
            }
        },
        clear: function () {
            this.$size = 0;
            this.map = {};
        },
        toArray: function () {
            return Object.keys(this.map);
        }
});

(function () {
    function HashSet(hashingFunction, equalityFunction) {
        var hashTable = new Kotlin.HashTable(hashingFunction, equalityFunction);

        this.add = function (o) {
            hashTable.put(o, true);
        };

        this.addAll = function (arr) {
            var i = arr.length;
            while (i--) {
                hashTable.put(arr[i], true);
            }
        };

        this.values = function () {
            return hashTable._keys();
        };

        this.iterator = function () {
            return Kotlin.arrayIterator(this.values());
        };

        this.remove = function (o) {
            return hashTable.remove(o) ? o : null;
        };

        this.contains = function (o) {
            return hashTable.containsKey(o);
        };

        this.clear = function () {
            hashTable.clear();
        };

        this.size = function () {
            return hashTable.size();
        };

        this.isEmpty = function () {
            return hashTable.isEmpty();
        };

        this.clone = function () {
            var h = new HashSet(hashingFunction, equalityFunction);
            h.addAll(hashTable.keys());
            return h;
        };

        this.equals = function (o) {
            if (o === null || o === undefined) return false;
            if (this.size() === o.size()) {
                var iter1 = this.iterator();
                var iter2 = o.iterator();
                while (true) {
                    var hn1 = iter1.hasNext();
                    var hn2 = iter2.hasNext();
                    if (hn1 != hn2) return false;
                    if (!hn2)
                        return true;
                    else {
                        var o1 = iter1.next();
                        var o2 = iter2.next();
                        if (!Kotlin.equals(o1, o2)) return false;
                    }
                }
            }
            return false;
        };

        this.toString = function() {
            var builder = "[";
            var iter = this.iterator();
            var first = true;
            while (iter.hasNext()) {
                if (first)
                    first = false;
                else
                    builder += ", ";
                builder += iter.next();
            }
            builder += "]";
            return builder;
        };

        this.intersection = function (hashSet) {
            var intersection = new HashSet(hashingFunction, equalityFunction);
            var values = hashSet.values(), i = values.length, val;
            while (i--) {
                val = values[i];
                if (hashTable.containsKey(val)) {
                    intersection.add(val);
                }
            }
            return intersection;
        };

        this.union = function (hashSet) {
            var union = this.clone();
            var values = hashSet.values(), i = values.length, val;
            while (i--) {
                val = values[i];
                if (!hashTable.containsKey(val)) {
                    union.add(val);
                }
            }
            return union;
        };

        this.isSubsetOf = function (hashSet) {
            var values = hashTable.keys(), i = values.length;
            while (i--) {
                if (!hashSet.contains(values[i])) {
                    return false;
                }
            }
            return true;
        };
    }

    Kotlin.HashSet = Kotlin.createClass(Kotlin.Set,
        function () {
            HashSet.call(this);
        }
    );

    Kotlin.ComplexHashSet = Kotlin.HashSet;
}());


define(function () { return Kotlin; });