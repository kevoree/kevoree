package org.kevoree.core;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.ModelSerializer;
import org.kevoree.modeling.api.util.ActionType;
import org.kevoree.modeling.api.util.ModelAttributeVisitor;
import org.kevoree.modeling.api.util.ModelVisitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 *
 * Created by leiko on 2/23/17.
 */
public class ModelCloner {

    private KevoreeFactory factory;

    public ModelCloner(KevoreeFactory factory) {
        this.factory = factory;
    }

    public <T extends KMFContainer> T clone(KMFContainer model) {
        return this.clone(model, false);
    }

    public <T extends KMFContainer> T clone(KMFContainer model, boolean readOnly) {
        return this.internalClone(model, readOnly);
    }

    private Map<KMFContainer, KMFContainer> createContext() {
        return new IdentityHashMap<KMFContainer, KMFContainer>();
    }

    private <T extends KMFContainer> T internalClone(KMFContainer rootElem, final boolean readOnly) {
        final Map<KMFContainer, KMFContainer> context = createContext();

        // clone root element (attributes only)
        final KMFContainer clonedRootElem = attributesClone(rootElem);
        // keep a ref to that cloned element
        context.put(rootElem, clonedRootElem);

        // visit all elements in graph and create clones
        rootElem.visit(new ModelVisitor() {
            @Override
            public void visit(@NotNull KMFContainer elem, @NotNull String refNameInParent, @NotNull KMFContainer parent) {
                KMFContainer clonedElem = context.get(elem);
                if (clonedElem == null) {
                    context.put(elem, attributesClone(elem));
                }
            }
        }, true, true, true);

        // visit every contained elements in all graph and ADD clones
        rootElem.visit(new ModelVisitor() {
            @Override
            public void visit(@NotNull KMFContainer elem, @NotNull String refNameInParent, @NotNull KMFContainer parent) {
                KMFContainer clonedParent = context.get(parent);
                KMFContainer clonedElem = context.get(elem);
                clonedParent.reflexiveMutator(ActionType.ADD, refNameInParent, clonedElem, false, false);
            }
        }, true, true, false);

        // visit all elements in graph and ADD clones again because KMF must have generate buggy code behind
        // and that's the only way to get it work u_u
        rootElem.visit(new ModelVisitor() {
            @Override
            public void visit(@NotNull KMFContainer elem, @NotNull String refNameInParent, @NotNull KMFContainer parent) {
                KMFContainer clonedParent = context.get(parent);
                KMFContainer clonedElem = context.get(elem);
                clonedParent.reflexiveMutator(ActionType.ADD, refNameInParent, clonedElem, false, false);
            }
        }, true, true, true);

        clonedRootElem.visit(new ModelVisitor() {
            @Override
            public void visit(@NotNull KMFContainer elem, @NotNull String refNameInParent, @NotNull KMFContainer parent) {
                if (readOnly) {
                    elem.setInternalReadOnly();
                }
            }
        }, true, true, true);

        return (T) clonedRootElem;
    }

    private KMFContainer attributesClone(KMFContainer elem) {
        final KMFContainer clonedElem = factory.create(elem.metaClassName());
        if (clonedElem != null) {
            if (elem.isRoot()) {
                factory.root(clonedElem);
            }
            if (elem.isReadOnly()) {
                clonedElem.setInternalReadOnly();
            }
        }

        elem.visitAttributes(new ModelAttributeVisitor() {
            @Override
            public void visit(Object value, @NotNull String refInParent, @NotNull KMFContainer parent) {
                if (value != null) {
                    if (value instanceof ArrayList) {
                        ArrayList list = (ArrayList) value;
                        ArrayList<Object> clonedList = new ArrayList<Object>(list.size());
                        clonedList.addAll(list);
                        clonedElem.reflexiveMutator(ActionType.SET, refInParent, clonedList, false, false);
                    } else {
                        clonedElem.reflexiveMutator(ActionType.SET, refInParent, value, false, false);
                    }
                }
            }
        });

        return clonedElem;
    }

    public static void main(String[] args) throws IOException {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ModelLoader loader = factory.createJSONLoader();
        ModelSerializer saver = factory.createJSONSerializer();
        InputStream modelInputStream = ModelCloner.class.getResourceAsStream("/model.json");
        ContainerRoot model = (ContainerRoot) loader.loadModelFromStream(modelInputStream).get(0);

        ContainerRoot clone = new ModelCloner(factory).clone(model, true);
        String cloneStr = saver.serialize(clone);
        FileUtils.writeStringToFile(new File("/tmp/cloneStr.json"), cloneStr, "utf8");
    }
}
