package org.kevoree.api.adaptation;

/**
 * Created by duke on 8/6/14.
 */

public class AdaptationPrimitive {
    private String primitiveType;
    private String targetNodeName;
    private Object ref;

    public String getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(String primitiveType) {
        this.primitiveType = primitiveType;
    }

    public String getTargetNodeName() {
        return targetNodeName;
    }

    public void setTargetNodeName(String targetNodeName) {
        this.targetNodeName = targetNodeName;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
