
class org.kevoree.ComponentInstance : org.kevoree.Instance {
    @contained
    provided : org.kevoree.Port[0,*]
    @contained
    required : org.kevoree.Port[0,*]
    namespace : org.kevoree.Namespace
}

class org.kevoree.ComponentType : org.kevoree.TypeDefinition {
    @contained
    required : org.kevoree.PortTypeRef[0,*]
    @contained
    provided : org.kevoree.PortTypeRef[0,*]
}

class org.kevoree.ContainerNode : org.kevoree.Instance {
    @contained
    components : org.kevoree.ComponentInstance[0,*]
    hosts : org.kevoree.ContainerNode[0,*] oppositeOf host
    host : org.kevoree.ContainerNode oppositeOf hosts
    groups : org.kevoree.Group[0,*] oppositeOf subNodes
    @contained
    networkInformation : org.kevoree.NetworkInfo[0,*]
}

class org.kevoree.ContainerRoot  {
    @contained
    nodes : org.kevoree.ContainerNode[0,*]
    @contained
    typeDefinitions : org.kevoree.TypeDefinition[0,*]
    @contained
    repositories : org.kevoree.Repository[0,*]
    @contained
    dataTypes : org.kevoree.TypedElement[0,*]
    @contained
    libraries : org.kevoree.TypeLibrary[0,*]
    @contained
    hubs : org.kevoree.Channel[0,*]
    @contained
    mBindings : org.kevoree.MBinding[0,*]
    @contained
    deployUnits : org.kevoree.DeployUnit[0,*]
    @contained
    nodeNetworks : org.kevoree.NodeNetwork[0,*]
    @contained
    groups : org.kevoree.Group[0,*]
}

class org.kevoree.PortType : org.kevoree.TypeDefinition {
    synchrone : Bool
}

class org.kevoree.Port : org.kevoree.NamedElement {
    bindings : org.kevoree.MBinding[0,*] oppositeOf port
    portTypeRef : org.kevoree.PortTypeRef
}

class org.kevoree.Namespace : org.kevoree.NamedElement {
    elements : org.kevoree.Instance[0,*]
}

class org.kevoree.Dictionary  {
    @contained
    values : org.kevoree.Value[0,*]
}

class org.kevoree.FragmentDictionary : org.kevoree.Dictionary {
    @id
    name : String
}

class org.kevoree.DictionaryType  {
    @contained
    attributes : org.kevoree.DictionaryAttribute[0,*]
}

class org.kevoree.DictionaryAttribute : org.kevoree.TypedElement {
    optional : Bool
    state : Bool
    datatype : String
    fragmentDependant : Bool
    defaultValue : String
}

class org.kevoree.Value  {
    @id
    name : String
    value : String
}

class org.kevoree.PortTypeRef : org.kevoree.NamedElement {
    optional : Bool
    noDependency : Bool
    ref : org.kevoree.PortType
    @contained
    mappings : org.kevoree.PortTypeMapping[0,*]
}

class org.kevoree.ServicePortType : org.kevoree.PortType {
    interface : String
    @contained
    operations : org.kevoree.Operation[0,*]
}

class org.kevoree.Operation : org.kevoree.NamedElement {
    @contained
    parameters : org.kevoree.Parameter[0,*]
    returnType : org.kevoree.TypedElement
}

class org.kevoree.Parameter : org.kevoree.NamedElement {
    order : Int
    type : org.kevoree.TypedElement
}

class org.kevoree.TypedElement : org.kevoree.NamedElement {
    genericTypes : org.kevoree.TypedElement[0,*]
}

class org.kevoree.MessagePortType : org.kevoree.PortType {
    filters : org.kevoree.TypedElement[0,*]
}

class org.kevoree.Repository  {
    @id
    url : String
}

class org.kevoree.DeployUnit : org.kevoree.NamedElement {
    @id
    groupName : String
    @id
    version : String
    url : String
    @id
    hashcode : String
    type : String
    requiredLibs : org.kevoree.DeployUnit[0,*]

    @contained
    filters : org.kevoree.Value[0,*]
}

class org.kevoree.TypeLibrary : org.kevoree.NamedElement {
    subTypes : org.kevoree.TypeDefinition[0,*]
}

class org.kevoree.NamedElement  {
    @id
    name : String
}

class org.kevoree.PortTypeMapping  {
    beanMethodName : String
    serviceMethodName : String
    paramTypes : String
}

class org.kevoree.Channel : org.kevoree.Instance {
    bindings : org.kevoree.MBinding[0,*] oppositeOf hub
}

class org.kevoree.MBinding  {
    port : org.kevoree.Port oppositeOf bindings
    hub : org.kevoree.Channel oppositeOf bindings
}

class org.kevoree.NodeNetwork  {
    @contained
    link : org.kevoree.NodeLink[0,*]
    initBy : org.kevoree.ContainerNode
    target : org.kevoree.ContainerNode
}

class org.kevoree.NodeLink  {
    networkType : String
    estimatedRate : Int
    lastCheck : String
    zoneID : String
    @contained
    networkProperties : org.kevoree.NetworkProperty[0,*]
}

class org.kevoree.NetworkInfo : org.kevoree.NamedElement {
    @contained
    values : org.kevoree.NetworkProperty[0,*]
}

class org.kevoree.NetworkProperty : org.kevoree.NamedElement {
    @id
    name : String
    value : String
}

class org.kevoree.ChannelType : org.kevoree.TypeDefinition {
    lowerBindings : Int
    upperBindings : Int
    lowerFragments : Int
    upperFragments : Int
}

class org.kevoree.TypeDefinition : org.kevoree.NamedElement {
    @id
    version : String
    factoryBean : String
    bean : String
    abstract : Bool
    deployUnits : org.kevoree.DeployUnit[0,*]
    @contained
    dictionaryType : org.kevoree.DictionaryType
    superTypes : org.kevoree.TypeDefinition[0,*]

    @contained
    values : org.kevoree.Value[0,*]
}

class org.kevoree.Instance : org.kevoree.NamedElement {
    metaData : String
    started : Bool
    typeDefinition : org.kevoree.TypeDefinition
    @contained
    dictionary : org.kevoree.Dictionary
    @contained
    fragmentDictionary : org.kevoree.FragmentDictionary[0,*]

    @contained
    values : org.kevoree.Value[0,*]
}

class org.kevoree.Group : org.kevoree.Instance {
    subNodes : org.kevoree.ContainerNode[0,*] oppositeOf groups
}

class org.kevoree.GroupType : org.kevoree.TypeDefinition {
}

class org.kevoree.NodeType : org.kevoree.TypeDefinition {
}
