
class kevoreeadaptation.AdaptationPrimitive  {
    primitiveType : String
    targetNodeName : String
    ref : kevoreeadaptation.Object
}

class kevoreeadaptation.AdaptationModel  {
    @contained
    adaptations : kevoreeadaptation.AdaptationPrimitive[0,*]
    @contained
    orderedPrimitiveSet : kevoreeadaptation.Step
}

class kevoreeadaptation.Step  {
    adaptations : kevoreeadaptation.AdaptationPrimitive[0,*]
    nextStep : kevoreeadaptation.Step
}

class kevoreeadaptation.ParallelStep : kevoreeadaptation.Step {
}

class kevoreeadaptation.SequentialStep : kevoreeadaptation.Step {
}
