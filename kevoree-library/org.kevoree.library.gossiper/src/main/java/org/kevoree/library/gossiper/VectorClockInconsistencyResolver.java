package org.kevoree.library.gossiper;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.kevoree.library.gossiper.version.Occured;
import org.kevoree.library.gossiper.version.Versioned;

/**
 * An inconsistency resolver that uses the object VectorClocks leaving only a
 * set of concurrent versions remaining.
 * 
 * 
 */
public class VectorClockInconsistencyResolver<T> implements InconsistencyResolver<Versioned<T>> {

    public List<Versioned<T>> resolveConflicts(List<Versioned<T>> items) {
        int size = items.size();
        if(size <= 1)
            return items;

        List<Versioned<T>> newItems = new ArrayList<Versioned<T>>();
        for(Versioned<T> v1: items) {
            boolean found = false;
            for(ListIterator<Versioned<T>> it2 = newItems.listIterator(); it2.hasNext();) {
                Versioned<T> v2 = it2.next();
                Occured compare = v1.getVersion().compare(v2.getVersion());
                if(compare == Occured.AFTER) {
                    if(found)
                        it2.remove();
                    else
                        it2.set(v1);
                }
                if(compare != Occured.CONCURRENTLY)
                    found = true;
            }
            if(!found)
                newItems.add(v1);
        }
        return newItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return (o != null && getClass() == o.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
