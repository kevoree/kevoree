define(
    function () {
        return {
            parseBoolean: function (variable) {
                switch (variable) {
                    case 'true':
                        return true;
                    case 'false':
                        return false;
                    case true:
                        return true;
                    case false:
                        return false;
                    default:
                        return false;
                }
            }
        };
    }
);