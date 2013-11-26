define(
    function() {
        function Observable() {
            this._observers = new Array();
        }

        Observable.prototype.addObserver = function(obs) {
            if (this._observers.indexOf(obs) == -1) {
                this._observers.push(obs);
            }
        }

        Observable.prototype.notifyObservers = function() {
            if (this._observers && this._observers.length > 0) {
                for (var i=0; i<this._observers.length; i++) {
                    if (this._observers[i].update && typeof (this._observers[i].update) == "function") {
                        this._observers[i].update(this);
                    } else {
                        console.warn("There is no update() method in your Observer. Did you extends Observer and/or override update(subject) method?");
                    }
                }
            }
        }

        return Observable;
    }
);