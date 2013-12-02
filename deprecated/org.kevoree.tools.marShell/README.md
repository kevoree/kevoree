# KevScript documentation

Marshell module contains the definition of the KevScript language.

## What could I use it for?
KevScript language is a DSL to manipulate Kevoree model to define configuration from scratch or from an already existing one. It can be use offline to build a bootstrap model that will be use to start a runtime or online to dynamically update the configuration of a system.

## KevScript language as a offline DSL
It can be use on the editor to build a model that will be then saved or pushed on a node.
Basically you only need to write your script.

## KevScript language as a online DSL
It can also be used at runtime to dynamically update the current configuration of the hosting node. Indeed each component, channel, group and node is able to get a KevScript engine to submit modification on the current configuration.

To do so, the component, channel, group or node must use this code:

    // Asking for a kevscript engine
    KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
    // build script
    ...
    // Applying the script to the current configuration
    kengine.atomicInterpretDeploy();

To build a script means to define a set of command to apply on the model. This set of commands must be added on the engine. To do so, the engine provide two operations:

    engine.addVariable("<variableName>", "<variableValue>");
    engine.append("<command>");

Variables are used to ease the definition of script by defining generic command on which some elements will be replaced by variable on the execution of the script.

**Example of variables usage:**

    engine.addVariable("nodeName", "node0");
    engine.addVariable("componentType", "FakeConsole");
    engine.append("addComponent@{nodeName} : {componentType});

# KevScript commands
 Each command provide a way to update a specific element on the model

## Instance manipulation
    updateDictionary <InstanceName>[@<NodeName>] [({ key = "value" (, key = "value") }[@<NodeName>][,])*]

### Node manipulation
    addNode <nodeName> [ , <nodeName> ] : <NodeType>  [{ key = "value" (, key = "value") }]
 
    removeNode <nodeName>
 
    addChild <nodeName>[ , <nodeName> ]@<NodeName>
 
    removeChild <nodeName>[ , <nodeName> ]@<NodeName>
 
    moveChild <nodeName>[ , <nodeName> ]@<NodeName> => <nodeName>
### Component manipulation
    addComponent <ComponentInstanceName> @ <nodeName> : <ComponentTypeName> [{ key = "value" (, key = "value") }]
 
    removeComponent <ComponentInstanceName> @ <nodeName>
 
    moveComponent <ComponentInstanceName> @ <nodeName> => <nodeName>
### Channel manipulation
    addChannel <ChannelInstanceName> : <ChannelTypeName> [{ key = "value" (, key = "value") }]
 
    removeChannel <ChannelInstanceName>
### Group manipulation
    addGroup <GroupName> : <GroupTypeName> [{ key = "value" (, key = "value") }]
 
    removeGroup <GroupName>
## Type manipulation
 TODO

## Network manipulation
    network [<NodeSrc> =>] <NodeInstanceName> {key="val","key2"="val2"} [:<NetworkType>] [/<weight>]

