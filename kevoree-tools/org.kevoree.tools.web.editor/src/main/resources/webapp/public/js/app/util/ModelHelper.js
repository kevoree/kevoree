define(
    [
        'kevoree',
        'kotlin/kotlin'
    ],
    function (Kevoree, Kotlin) {

        function ModelHelper () {
            this._factory = new Kevoree.org.kevoree.impl.DefaultKevoreeFactory();
        }

        ModelHelper.prototype.getLibraries = function (model) {
            var ret = [];

            // inflate TypeDefinitions by browsing libraries
            var libz = model.getLibraries();
            for (var i=0; i < libz.size(); i++) {
                var lib = libz.get(i);
                ret.push({
                    name: lib.getName(),
                    components: (function (tDefs) {
                        var compz = [];
                        for (var i=0; i < tDefs.size(); i++) {
                            addTDefToArray(tDefs.get(i), compz);
                        }
                        return compz;
                    })(lib.getSubTypes())
                });
            }

            // add to "Default" library components that are not in any libraries' subtypes
            var tDefs = model.getTypeDefinitions(),
                defaultCompz = [];

            for (var i=0; i < tDefs.size(); i++) {
                var inLib = false;
                for (var j=0; j < ret.length; j++) {
                    if (arrayContainsTDef(ret[j].components, tDefs.get(i))) {
                        inLib = true;
                    }
                }
                if (!inLib) {
                    addTDefToArray(tDefs.get(i), defaultCompz);
                }
            }

            // do not add default libTree if there is no library-less comp
            if (defaultCompz.length > 0) {
                ret.push({
                    name: 'Default',
                    components: defaultCompz
                });
            }

            return ret;
        }

        // private method
        function addTDefToArray(tDef, array) {
            var type = "UnknownType";

            if (Kotlin.isType(tDef, Kevoree.org.kevoree.impl.ComponentTypeImpl)) {
                type = "ComponentType";

            } else if (Kotlin.isType(tDef, Kevoree.org.kevoree.impl.GroupTypeImpl)) {
                type = "GroupType"

            } else if (Kotlin.isType(tDef, Kevoree.org.kevoree.impl.ChannelTypeImpl)) {
                type = "ChannelType";

            } else if (Kotlin.isType(tDef, Kevoree.org.kevoree.impl.NodeTypeImpl)) {
                type = "NodeType";

            } else {
                //console.log("not handled for now", tDef.getName());
            }

            array.push({
                name: tDef.getName(),
                type: type
            });
        }

        // private method
        function arrayContainsTDef(array, tDef) {
            for (var i=0; i < array.length; i++) {
                if (array[i].name == tDef.getName()) return true;
            }
            return false;
        }

        return new ModelHelper();
    }
);