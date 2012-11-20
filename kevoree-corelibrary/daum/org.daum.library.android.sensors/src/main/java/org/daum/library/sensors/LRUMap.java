package org.daum.library.sensors;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 12/07/12
 * Time: 18:33
 * A cache that holds a finite number of entries and removes the least recently used entry when it becomes filled beyond this fixed capacity.
 */
public class LRUMap<T, U> implements ILRUMap {

    protected final int cacheSize;
    protected final Map<T, U> cache;
    protected final List<T> lruKeys;

    public LRUMap(int cacheSize)
    {
        this.cacheSize = cacheSize;
        this.cache = new HashMap<T, U>(cacheSize);
        this.lruKeys = new LinkedList<T>();
    }

    public U getLast(){
        if(lruKeys.size() > 0){
        T lru = lruKeys.get(0);
        return    cache.get(lru);
        }else {
            return null;
        }
    }
     public int size()
     {
        return lruKeys.size();
     }

    public boolean containsKey(T key)
    {
        return cache.containsKey(key);
    }

    public U get(T key)
    {
        if (lruKeys.get(0) != key) // Pointer comparison
        {
            if (!this.lruKeys.remove(key)) return null;
            lruKeys.add(0, key);
        }
        return cache.get(key);
    }

    public void clear()
    {
        cache.clear();
        lruKeys.clear();
    }

    public void put(T key, U value)
    {
        if (cacheIsFull())
            removeLeastRecentlyUsedFromCache();

        cache.put(key, value);
        lruKeys.add(key);
    }

    private boolean cacheIsFull()
    {
        return cache.size() == cacheSize;
    }

    protected void removeLeastRecentlyUsedFromCache()
    {
        T lru = lruKeys.remove(0);
        cache.remove(lru);
    }


    public Set<T> keySet(){
        return cache.keySet();
    }
    public void remove(T key)
    {
        lruKeys.remove(key);
        cache.remove(key);
    }
}
