package cz.agents.gtlibrary.utils;

import java.util.HashMap;
import java.util.function.Function;

public class DummyMap<K, V> extends HashMap<K, V> {

    @Override
    public V put(K key, V value) {
        return value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return value;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return mappingFunction.apply(key);
    }
}
