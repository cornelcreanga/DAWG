package com.boxofc.mdag;

import java.util.Map;

public interface SemiNavigableMap<K, V> extends Iterable<Map.Entry<K, V>> {
    public boolean isEmpty();
    public SemiNavigableMap<K, V> descendingMap();
}