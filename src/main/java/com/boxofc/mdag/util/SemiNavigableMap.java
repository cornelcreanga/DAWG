package com.boxofc.mdag.util;

public interface SemiNavigableMap<K, V> extends Iterable<SimpleEntry<K, V>> {
    public boolean isEmpty();
    public SemiNavigableMap<K, V> descendingMap();
}