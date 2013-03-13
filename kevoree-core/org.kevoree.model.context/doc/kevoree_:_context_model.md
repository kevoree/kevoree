# Kevoree : Context Model

![Kevoree icon](http://kevoree.org/img/kevoree-logo.png)

## Overview

This is a short introduction of the Kevoree **context** model.
While structural Model@runtime aims at capturing architectural view of the system, this model capture extra functional properties which decorates the structural view with dynamic values.

In short this model store informations from monitoring like power context, cpu consumption of each components,… In short, everything which help to build an autonomous system aware of is state. The context model is hosted by each Kevoree node, so any Kevoree instance can manage their own probes to fill this model and any instances can query the models to get feedback on metrics.

Kevoree context model largely use the syntaxique sugar of KMFQL so prior please read the  [documentation for the syntaxique part the KMFQL](https://github.com/dukeboard/kevoree-modeling-framework/blob/master/doc/kmf_path.md)

![Kevoree Context Model](https://raw.github.com/dukeboard/kevoree/master/kevoree-core/org.kevoree.model.context/doc/kevoree.context.png)

Context model is organized around a flat hierachie, the root contains severals `contexts`.
Each `context` define the family of data contained, it is also identified uniquely by a name attribute.

Context contains themself `MetricType`. A `MetricType` define a semantic of a data and is associated to a unit (ex: CPU% or celcius degree). A `MetricType` are also identify by its name attribute.

Each `MetricType` contains then several `Metrics` which associate the type to a particular model element, for instance a node or a component. A `Metric` define also the number of value or time to keep the history of data.

Finally each `Metric` contains a set of `MetricValue` which represente the concrete value mesured by a probe. This value is timestamped and can have two kind, instantanuous mesurement (MetricValue) or a elapsed time mesurement (DurationMetricValue), for example if a mesure take one hour. Between Metrics and there Types are defined also several relationship like min,max,first or last value to keep quick pointers on interesting values.

Metric are identify by a name and a query, the query allow to identify an element of the structural model of Kevoree with for instance a path.

## Generated API

The generated API is strictally compliant with KMF other models and will not be detailled here.
In addition an helper name PutHelper is present to help the creation of the entire hierarchie of a Metric with is path.
The following code show an example of creation of a Metric for perf context and cpu.load data for a Kevoree node named node42.
The getMetric method return the existing metric or a previous one exists otherwise it create the context and MetricType according to the parameters of the helper.

	Metric cpuMetric = PutHelper.getMetric(model, "perf/cpu.load/{node42}", PutHelper.getParam().setMetricTypeClazzName(CounterHistoryMetric.class.getName()).setNumber(100));
    
The same helper can be used to add value to an existing Metric as illustrate by the code follow :    
  
    PutHelper.addValue(cpuMetric, rand.nextLong() + "");


## Usage with KMFQL

This section illustrate the use of the context model to reason about the state of the platform with several query :

Get the CPU load Metric of the node42 :

	perf/cpu.load/{node42}
	
Get the last metric value of the CPU load of the node42 :

	perf/cpu.load/{node42}/last[]
	
Get the last value of the latency recorded for the component srv hosted on node0 :

	perf/latency/{nodes[node0]/components[srv]}/last[]
	
Get the last value of the latency recorded for all components if the type SprayServer hosted on node0 :

	perf/latency/{nodes[node0]/components[typeDefinition.name = SprayServer]}/last[]

Get all metric containing CPU in the name of the node42 :

	perf/{name = *cpu*}/{node42}/last[]

