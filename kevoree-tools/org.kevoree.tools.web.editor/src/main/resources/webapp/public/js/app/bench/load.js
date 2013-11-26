var model = null;
var Kevoree = null;

this.addEventListener('message', function (e) {
    var msg = e.data;

    switch (msg.cmd) {
        case 'vars':
            model = msg.data.model;
            Kevoree = msg.data.kevoree;
            break;

        case 'start':
            if (model && Kevoree) {
                var loader = new Kevoree.org.kevoree.loader.JSONModelLoader();
                var i=0;
                while (true) {
                    loader.loadModelFromString(model).get(0);
                    postMessage(++i);
                }
            }
            break;
    }
}, false);