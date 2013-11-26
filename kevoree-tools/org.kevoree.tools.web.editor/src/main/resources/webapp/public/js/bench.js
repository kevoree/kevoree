requirejs.config({
    //By default load any module IDs from js/lib
    baseUrl: '/js/lib',
    //except, if the module ID starts with "app",
    //load it from the js/app directory. paths
    //config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
    paths: {
        app:    '../app'
    }
});

define(
    [
        'kevoree',
        'jquery'
    ],

    function (Kevoree, $) {

        var model = null;

        $('#start-bench').on('click', function () {
            if (!model) {
                $.getJSON('/merge/all', function (data) {
                    model = JSON.stringify(data);
                    doBench();
                });

            } else {
                doBench();
            }
        });

        function doBench() {
            var loader = new Kevoree.org.kevoree.loader.JSONModelLoader(),
                serializer = new Kevoree.org.kevoree.serializer.JSONModelSerializer(),
                os = new Kevoree.java.io.OutputStream(),
                action = $('#action-type option:selected').val(),
                count = 20; // default

            var rawCount = $('#loop-count').val();
            if (isNumber(rawCount)) {
                count = rawCount;
            } else {
                $('#loop-count').val(count);
            }

            switch (action) {
                case 'load':
                    console.log('Starting load bench');
                    var start = Date.now();
                    for (var i=0; i < count; i++) {
                        loader.loadModelFromString(model).get(0);
                    }
                    var stop = Date.now();
                    break;

                case 'save':
                    console.log('Starting save bench');
                    var root = loader.loadModelFromString(model).get(0);

                    var start = Date.now();
                    for (var i=0; i < count; i++) {
                        serializer.serialize(root, os);
                    }
                    var stop = Date.now();
                    break;

                case 'merge':
                    console.log('Starting merge bench');
                    // TODO
                    break;
            }

            $('#results').append('<div class="row-fluid">Took me '+(stop-start)+'ms to <b>'+action+'</b> '+count+' times !</li>');
        }

        function isNumber(n) {
            return !isNaN(parseFloat(n)) && isFinite(n);
        }
    }
);