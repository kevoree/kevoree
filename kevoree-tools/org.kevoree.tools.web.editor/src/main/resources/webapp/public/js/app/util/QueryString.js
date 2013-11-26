define(
    function () {

        function QueryString() {
            this._qs = {};
            var query = window.location.search.substring(1);
            var vars = query.split("&");
            for (var i=0;i<vars.length;i++) {
                var pair = vars[i].split("=");
                // If first entry with this name
                if (typeof this._qs[pair[0]] === "undefined") {
                    this._qs[pair[0]] = pair[1];
                    // If second entry with this name
                } else if (typeof this._qs[pair[0]] === "string") {
                    var arr = [ this._qs[pair[0]], pair[1] ];
                    this._qs[pair[0]] = arr;
                    // If third or later entry with this name
                } else {
                    this._qs[pair[0]].push(pair[1]);
                }
            }
        }

        /**
         * Process params in document.URL and call the functions associated if
         * any param match the given "actions" parameter field.
         * For instance:
         *   var qs = new QueryString();
         *   qs.process({
         *       param1: function(value) {
         *          // do something with param1's value
         *       },
         *       param2: function(foo) {
         *          // do something with param2's value
         *       }
         *   });
         *
         * This example will call param1 & param2 functions when process is called
         * when the URL looks like this: http://example.com/?param1=bar&param2=42
         * @param actions
         */
        QueryString.prototype.process = function (actions) {
            for (var param in actions) {
                if (this._qs[param]) {
                    var field = actions[param];

                    if (typeof(field) === "function") {
                        field.call(this._qs, this._qs[param]);

                    } else if (typeof(field) === "object") {
                        if (field.deps !== undefined && typeof(field.hasDeps) === "function" && typeof(field.missDep) === "function") {
                            var values = [],
                                missField = null,
                                isValid = true;
                            values.push(this._qs[param]);

                            for (var i in field.deps) {
                                if (this._qs[field.deps[i]] == undefined) {
                                    missField = field.deps[i];
                                    isValid = false;
                                    break;
                                } else {
                                    values.push(this._qs[field.deps[i]]);
                                }
                            }

                            if (isValid) field.hasDeps.apply(this._qs, values);
                            else field.missDep.call(this._qs, missField);

                        } else {
                            console.log("QueryString Error: '"+param+"' field should be an object like: field: {deps: [], hasDeps: function (argVals) {}, missDeps: function (missArgs) {}}");
                        }

                    } else {
                        console.error("QueryString Error: '"+param+"' field is not a function or object. Skipped!");
                    }
                }
            }
        }

        return QueryString;
    }
);