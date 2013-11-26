define(
    function () {

        function Delayer() {
            this._timer = 0;
        }

        /**
         * delay(callback, delay)
         * Executes callback in delay ms
         * If called multiple times, old callback will be cleared
         * and timeout reset so the execution will happen in delay ms
         * from the new call
         */
        Delayer.prototype.delay = (function () {
            return function (callback, ms) {
                clearTimeout (this._timer);
                this._timer = setTimeout(callback, ms);
            };
        })();

        return Delayer;
    }
);