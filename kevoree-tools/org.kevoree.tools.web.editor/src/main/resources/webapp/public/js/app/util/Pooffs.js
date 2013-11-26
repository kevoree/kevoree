define(
    function() {
        /**
         * POO FFS
         * Because OOP is good.
         * @constructor
         */
        function Pooffs() {}

        /**
         * Use this method to "extend" another class (it does not really extends like in Java, but
         * it gives to the child all the parent's functions/attributes)
         * So you can not do checks like A instanceof B
         * (well you can do them, but they will return false)
         * Usage:
         * <code>Pooffs.extends(ChildClass, ParentClass);</code>
         * You can multiply Pooffs.extends(A, B) as many times as you want as it just
         * adds B.prototype attributes to A.prototype attributes.
         *
         * @param child
         * @param parent
         */
        Pooffs.extends = function (child, parent) {
            for (var attr in parent.prototype) {
                child.prototype[attr] = parent.prototype[attr];
            }
        }

        /**
         * Returns the given object's class name or undefined if none found
         * @param obj class instance
         * @returns {String} Given object's class name
         */
        Pooffs.objectClass = function (obj) {
            if (obj && obj.constructor && obj.constructor.toString) {
                var arr = obj.constructor.toString().match(
                    /function\s*(\w+)/);

                if (arr && arr.length == 2) {
                    return arr[1];
                }
            }

            return undefined;
        }

        return Pooffs;
    }
);