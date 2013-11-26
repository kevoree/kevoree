define(
    [
        'abstraction/property/KNodeNetwork',
        'util/Pooffs'
    ],
    function (KNodeNetwork, Pooffs) {

        Pooffs.extends(CNodeNetwork, KNodeNetwork);

        function CNodeNetwork(initBy, target) {
            KNodeNetwork.prototype.constructor.call(this, initBy, target);
        }

        return CNodeNetwork;
    }
);