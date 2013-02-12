# Kevoree : Structural model

![Kevoree icon](http://kevoree.org/img/kevoree-logo.png)

## Overview

This short documentation illustrates elements of the Kevoree modeling layer.


##Kevoree Modeling Framework - Query Language (KMFQL)

The Kevoree Modeling Framework Query Language (KMFQL) is developed to offer an effective way to gather model elements at runtime. KMFQL decomposes in two different selectors: Path Selector (PS) and Query Selector(QS).

KMFQL is automatically included in the classes of model's elements during the generation phase.

A detailed documentation for the syntactic part the KMFQL is available [here](https://github.com/dukeboard/kevoree-modeling-framework/blob/master/doc/kmf_path.md)

### Query samples

Navigates the `nodes` relation of the ContainerRoot and selects a node which `id` is "DukeNode" 

	nodes[DukeNode]
	
Navigates the `nodes` relation of the ContainerRoot and selects the node with `id` "ia3"; then looks for a sub node with `id` "DukeNode" in the `subNodes` relation of the "ia3" node.

	nodes[ia3]/subNodes[DukeNode]
	
Collects all component instances named "logger" hosted on "DukeNode", himself hosted on node "ia3" 

	nodes[ia3]/subNodes[DukeNode]/components[logger]
	
Collects all nodes which name starts with "Duke"

	nodes[{ name = Duke*}]
	
Collects all components named "logger", from any first level node.

	nodes[{ name = *}]/components[logger]

Collects all RequiredPorts named "log", on all components from all first level nodes

	nodes[{ name = * }]/components[{ name = * }]/required[{name = log}]	
Collects all ports named "log" connected to a channel

	hubs[{ name = * }]/port[{ name = log}]
	
Collects all nodes hosted on "MiniCloudNode" instances.

	nodes[{ typeDefinition.name = MiniCloudNode }]/subNodes[{name = *}]
	
Collects any node hosting at least 4 components, itself hosted on a "MiniCloudNode" instance

	nodes[{ typeDefinition.name = MiniCloudNode }]/subNodes[{ &(name = *)(components.size > 3)}]
		

