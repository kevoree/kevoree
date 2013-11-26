define(
    [
        'control/CEditor',
        'control/CGroup',
        'control/CChannel',
        'control/CNode',
        'control/CComponent',
        'control/CWire',
        'control/CInputPort',
        'control/COutputPort',
        'control/property/CNodeNetwork',
        'control/property/CNodeLink',
        'control/property/CNetworkProperty',
        'control/property/CNodeProperties',
        'control/dictionary/CDictionary',
        'control/dictionary/CAttribute',
        'control/dictionary/CValue'
    ],

    function (CEditor, CGroup, CChannel, CNode, CComponent, CWire, CInputPort, COutputPort, CNodeNetwork, CNodeLink,
              CNetworkProperty, CNodeProperties, CDictionary, CAttribute, CValue) {

        function CFactory() {
            if (CFactory.prototype._instance) {
                return CFactory._instance;
            }
            CFactory._instance = this;

            return CFactory._instance;
        }

        CFactory.getInstance = function() {
            if (!CFactory._instance) {
                return new CFactory();
            }
            return CFactory._instance;
        }

        CFactory.prototype.newEditor = function (containerID) {
            return new CEditor(containerID);
        };

        CFactory.prototype.newGroup = function (editor, type) {
            return new CGroup(editor, type);
        };

        CFactory.prototype.newNode = function (editor, type) {
            return new CNode(editor, type);
        };

        CFactory.prototype.newComponent = function (editor, type) {
            return new CComponent(editor, type);
        };

        CFactory.prototype.newChannel = function (editor, type) {
            return new CChannel(editor, type);
        };

        CFactory.prototype.newWire = function (origin) {
            return new CWire(origin);
        };

        CFactory.prototype.newInputPort = function (name) {
            return new CInputPort(name);
        };

        CFactory.prototype.newOutputPort = function (name) {
            return new COutputPort(name);
        };

        CFactory.prototype.newOutputPort = function (name) {
            return new COutputPort(name);
        };

        CFactory.prototype.newNodeNetwork = function (initBy, target) {
            return new CNodeNetwork(initBy, target);
        };

        CFactory.prototype.newNodeLink = function (nodeProps) {
            return new CNodeLink(nodeProps);
        };

        CFactory.prototype.newNetworkProperty = function (link) {
            return new CNetworkProperty(link);
        };

        CFactory.prototype.newNodeProperties = function (node) {
            return new CNodeProperties(node);
        };

        CFactory.prototype.newDictionary = function (entity) {
            return new CDictionary(entity);
        };

        CFactory.prototype.newAttribute = function (dict) {
            return new CAttribute(dict);
        };

        CFactory.prototype.newValue = function (attr, targetNode) {
            return new CValue(attr, targetNode);
        };

        return CFactory;
    }
);