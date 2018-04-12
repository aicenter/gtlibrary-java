package cz.agents.gtlibrary.utils;

import java.util.HashSet;

public class DummySet<E> extends HashSet<E> {

    @Override
    public boolean add(E e) {
        return false;
    }
}
