package com.boxofc.mdag;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;

public class DAWGMap implements Map<String, String> {
    private final DAWGSet dawg;
    private final char keyValueSeparator;
    
    DAWGMap(DAWGSet dawg, char keyValueSeparator) {
        this.dawg = dawg;
        this.keyValueSeparator = keyValueSeparator;
    }

    @Override
    public int size() {
        return dawg.size();
    }

    @Override
    public boolean isEmpty() {
        return dawg.isEmpty();
    }
    
    private static void checkNotNull(Object o) {
        if (o == null)
            throw new NullPointerException();
    }
    
    private static String getFirstElement(Iterable<String> i) {
        for (String s : i)
            return s;
        return null;
    }
    
    private static String pollFirstElement(Iterable<String> i) {
        for (Iterator<String> it = i.iterator(); it.hasNext();) {
            String s = it.next();
            it.remove();
            return s;
        }
        return null;
    }
    
    private static String valueOfStringEntry(String stringEntry, Object key) {
        return stringEntry == null ? null : stringEntry.substring(((String)key).length() + 1);
    }

    @Override
    public boolean containsKey(Object key) {
        checkNotNull(key);
        return dawg.getStringsStartingWith((String)key + keyValueSeparator).iterator().hasNext();
    }

    @Override
    public boolean containsValue(Object value) {
        checkNotNull(value);
        return dawg.getStringsEndingWith(keyValueSeparator + (String)value).iterator().hasNext();
    }

    @Override
    public String get(Object key) {
        checkNotNull(key);
        return valueOfStringEntry(getFirstElement(dawg.getStringsStartingWith((String)key + keyValueSeparator)), key);
    }

    @Override
    public String put(String key, String value) {
        checkNotNull(value);
        String old = get(key);
        if (old != null && old.equals(value))
            return old;
        String keyWithSeparator = key + keyValueSeparator;
        dawg.add(keyWithSeparator + value);
        if (old != null)
            dawg.remove(keyWithSeparator + old);
        return old;
    }

    @Override
    public String remove(Object key) {
        checkNotNull(key);
        return valueOfStringEntry(pollFirstElement(dawg.getStringsStartingWith((String)key + keyValueSeparator)), key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Map.Entry<? extends String, ? extends String> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    @Override
    public void clear() {
        dawg.clear();
    }

    @Override
    public Set<String> keySet() {
        return null;
    }

    @Override
    public Collection<String> values() {
        return null;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return null;
    }
}