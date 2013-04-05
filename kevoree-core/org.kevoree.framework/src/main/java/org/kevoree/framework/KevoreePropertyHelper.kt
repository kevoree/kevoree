package org.kevoree.framework

import java.util.ArrayList
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.NodeNetwork
import org.kevoree.TypeDefinition

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/01/12
 * Time: 15:08
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public object KevoreePropertyHelper {
    /**
   * look for a specific network property define by a key and specified with a specific target
   * @param model the model which contains the property
   * @param targetNodeName the name of the target
   * @param key the key
   * @return a list of the existing values
   */
    fun getNetworkProperties (model: ContainerRoot, targetNodeName: String, key: String): List<String> {
        val properties = ArrayList<String>()
        val filteredNodeNetwork = ArrayList<NodeNetwork>()
        for (lNN in model.getNodeNetworks()){
            if (lNN.getTarget()!!.getName() == targetNodeName) {
                filteredNodeNetwork.add(lNN)
            }
        }
        for (fnn in filteredNodeNetwork) {
            for (fnl in fnn.getLink()) {
                for (p in fnl.getNetworkProperties()) {
                    if (p.getName() == key) {
                        properties.add(p.getValue())
                        break
                    }
                }
            }
        }
        return properties
    }

    /**
     * look for a specific property for an Instance element
     * @param instance the instance which may contains the property you are looking for
     * @param key the key identifier of the property
     * @param isFragment true if you are looking for property on channel or group and the property is fragment dependent, false else
     * @param nodeNameForFragment the name of the fragment, null else
     * @return an Option corresponding to the value of the property
     */
    fun getProperty (instance: Instance, key: String, isFragment: Boolean = false, nodeNameForFragment: String = ""): String? {
        if (instance.getDictionary() == null) {
            return getDefaultValue(instance.getTypeDefinition(), key)
        } else {
            val dictionary = instance.getDictionary()!!
            for (dictionaryAttribute in dictionary.getValues()){
                if (dictionaryAttribute.getAttribute()!!.getName() == key &&
                ((isFragment && dictionaryAttribute.getTargetNode() != null && dictionaryAttribute.getTargetNode()!!.getName() == nodeNameForFragment) || !isFragment)) {
                    return   dictionaryAttribute.getValue()
                }

            }
            return getDefaultValue(instance.getTypeDefinition(), key)
        }
    }

    private fun getDefaultValue (typeDefinition: TypeDefinition?, key: String): String? {
        if (typeDefinition != null && typeDefinition.getDictionaryType() != null) {
            for (defaultValue in typeDefinition.getDictionaryType()!!.getDefaultValues()) {
                if (defaultValue.getAttribute()!!.getName() == key) {
                    return defaultValue.getValue()
                }
            }
        }
        return null
    }
}