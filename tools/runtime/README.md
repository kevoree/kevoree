# Kevoree standalone runtime
## Command line options
```bash
$ java -jar ${options} org.kevoree.platform.standalone-${version}.jar # the options are detailed below.
```

### Command line options

  * **-Dkevoree.registry=http://host:port** - overload the url to the kevoree registry used for the resolution (default : http://registry.kevoree.org)
  * **-Dkevoree.version=${version}** - specify the version of the runtime (default : latest stable version).
  * **-Dnode.runtime=${path to a kevscript (.kevs) or a serialized model (.json)  }** - specify the path to a kevscript. This kevscript/model to be used at startup (see below for default kevscript).
  * **-Dnode.name=${nodeName}** - specify the node name of the current instance (default : node0). It will override the name of the default node in the default kevscript at startup if no kevscript is specified.
  
## Default startup model
```kevs
add node0 : JavaNode # node0 can be overridden
add sync : WSGroup

attach node0 sync

```
