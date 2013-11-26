requirejs.config({
    //By default load any module IDs from js/lib
    baseUrl: '/js/lib',
    //except, if the module ID starts with "app",
    //load it from the js/app directory. paths
    //config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
    paths: {
        bootstrap:  'bootstrap/src',
        app:        '../app',
        util:       '../app/runtime/util',
        app_util:   '../app/util',
        ui:         '../app/runtime/ui',
        ctrl:       '../app/runtime/ctrl',
        core:       '../app/runtime/core'
    }
});

define(
    [
        'ctrl/Runtime',
        'bootstrap/collapse',
        'bootstrap/dropdown',
        'bootstrap/alert',
        'bootstrap/popover'
    ],

    function (Runtime) {

        // use createShadowRoot() method whether we are on Firefox or Webkit as method name
        HTMLElement.prototype.createShadowRoot = HTMLElement.prototype.createShadowRoot ||
            HTMLElement.prototype.webkitCreateShadowRoot;

        // Runtime app controller
        var runtime = new Runtime()

        // using require('runtime-main') in DOM document will give you access to the controller
        return runtime;
    }
);