define(
    function() {
        function AController() {}

        AController.prototype.getUI = function() {
            return this._ui;
        }

        return AController;
    }
);