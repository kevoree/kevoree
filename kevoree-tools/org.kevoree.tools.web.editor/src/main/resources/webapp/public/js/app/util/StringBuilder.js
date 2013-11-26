define(
    function () {
        function StringBuilder(str) {
            this.strings = [];
            if (str) this.append(str);
        }

        StringBuilder.prototype.append = function (str) {
            if (str) this.strings.push(str);
            return this;
        }

        StringBuilder.prototype.clear = function () {
            this.strings.length = 0;
            return this;
        }

        StringBuilder.prototype.toString = function () {
            return this.strings.join('');
        }

        return StringBuilder;
    }
);