package cz.agents.gtlibrary.utils;

import java.util.ArrayList;

/**
 * Created by Jakub Cerny on 23/10/2017.
 */
public class ObjectPool<T extends Object>  {

    private ArrayList<T> pool;
    private int maxSize;

    public ObjectPool(int size) {
        pool = new ArrayList<T>(size / 8);
        maxSize = size;
    }

    public void push(T obj) {
        if(pool.size() < maxSize) {
            pool.add(obj);
        }
    }

    public T pop() {
        if(!pool.isEmpty()) {
            return pool.remove(pool.size() - 1);
        }
        return null;
    }

    public int size()
    {
        return pool.size();
    }

    public int maxSize()
    {
        return maxSize;
    }

    public boolean isEmpty()
    {
        return pool.isEmpty();
    }
}
