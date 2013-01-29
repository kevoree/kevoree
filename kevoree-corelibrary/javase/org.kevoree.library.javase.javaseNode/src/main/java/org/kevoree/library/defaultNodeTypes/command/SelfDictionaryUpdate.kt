package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.Instance
import org.kevoree.framework.AbstractNodeType
import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import java.util.HashMap


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 15:29
 */

class SelfDictionaryUpdate(val c: Instance, val node: AbstractNodeType): PrimitiveCommand {

    var logger = LoggerFactory.getLogger(this.javaClass)!!

    private var lastDictioanry: HashMap<String?, Any?>? = null

    override fun execute(): Boolean {
        //BUILD MAP
        //SET DEFAULT VAL
        val dictionary: HashMap<String?, Any?> = HashMap<String?, Any?>()
        if (c.getTypeDefinition()!!.getDictionaryType() != null) {
            if (c.getTypeDefinition()!!.getDictionaryType()!!.getDefaultValues() != null) {
                for(dv in c.getTypeDefinition()!!.getDictionaryType()!!.getDefaultValues()) {
                    dictionary.put(dv.getAttribute()!!.getName(), dv.getValue())
                }
            }
        }
        //SET DIC VAL
        if (c.getDictionary() != null) {
            for(v in c.getDictionary()!!.getValues()) {
                dictionary.put(v.getAttribute()!!.getName(), v.getValue())
            }
        }
        //SAVE DICTIONARY
        lastDictioanry = node.getDictionary()
        node.setDictionary(dictionary)
        node.updateNode() // TODO REFLEXIVE CALL
        return true
    }

    override fun undo() {
        if (lastDictioanry != null) {
            node.setDictionary(lastDictioanry)
            node.updateNode() // TODO REFLEXIVE CALL
        }
    }

}
