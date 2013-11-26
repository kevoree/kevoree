define(
    function() {

        function Observer() {}

        Observer.prototype.update = function (subject) {
            console.warn("You did not override update method for your subject's observer. Subject:", subject);
        }

        return Observer;
    }
);