package com.boxofc.mdag;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;

public class DAWGMap implements NavigableMap<String, String> {
    private static final char KEY_VALUE_SEPARATOR = '\0';
    private static final String DOUBLE_KEY_VALUE_SEPARATOR = "\0\0";
    private final DAWGSet dawg;
    
    DAWGMap(DAWGSet dawg) {
        this.dawg = dawg;
    }

    @Override
    public int size() {
        return dawg.size();
    }

    @Override
    public boolean isEmpty() {
        return dawg.isEmpty();
    }
    
    private static void checkNotNullAndContainsNoZeros(Object o) {
        if (o == null)
            throw new NullPointerException();
        if (((String)o).indexOf(KEY_VALUE_SEPARATOR) >= 0)
            throw new IllegalArgumentException("Argument contains zero character");
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
    
    private static String keyOfStringEntry(String stringEntry) {
        return stringEntry == null ? null : stringEntry.substring(0, stringEntry.indexOf(KEY_VALUE_SEPARATOR));
    }
    
    private Entry<String, String> entryOfStringEntry(String stringEntry) {
        if (stringEntry == null)
            return null;
        int idx = stringEntry.indexOf(KEY_VALUE_SEPARATOR);
        return new MapEntry(stringEntry.substring(0, idx), stringEntry.substring(idx + 1));
    }

    @Override
    public boolean containsKey(Object key) {
        checkNotNullAndContainsNoZeros(key);
        return dawg.getStringsStartingWith((String)key + KEY_VALUE_SEPARATOR).iterator().hasNext();
    }

    @Override
    public boolean containsValue(Object value) {
        checkNotNullAndContainsNoZeros(value);
        return dawg.getStringsEndingWith(KEY_VALUE_SEPARATOR + (String)value).iterator().hasNext();
    }

    @Override
    public String get(Object key) {
        checkNotNullAndContainsNoZeros(key);
        return valueOfStringEntry(getFirstElement(dawg.getStringsStartingWith((String)key + KEY_VALUE_SEPARATOR)), key);
    }

    @Override
    public String put(String key, String value) {
        checkNotNullAndContainsNoZeros(value);
        String old = get(key);
        if (old != null && old.equals(value))
            return old;
        String keyWithSeparator = key + KEY_VALUE_SEPARATOR;
        dawg.add(keyWithSeparator + value);
        if (old != null)
            dawg.remove(keyWithSeparator + old);
        return old;
    }

    @Override
    public String remove(Object key) {
        checkNotNullAndContainsNoZeros(key);
        return valueOfStringEntry(pollFirstElement(dawg.getStringsStartingWith((String)key + KEY_VALUE_SEPARATOR)), key);
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
    public Entry<String, String> lowerEntry(String key) {
        checkNotNullAndContainsNoZeros(key);
        return entryOfStringEntry(dawg.lower(key + KEY_VALUE_SEPARATOR));
    }

    @Override
    public String lowerKey(String key) {
        checkNotNullAndContainsNoZeros(key);
        return keyOfStringEntry(dawg.lower(key + KEY_VALUE_SEPARATOR));
    }

    @Override
    public Entry<String, String> floorEntry(String key) {
        checkNotNullAndContainsNoZeros(key);
        return entryOfStringEntry(dawg.lower(key + DOUBLE_KEY_VALUE_SEPARATOR));
    }

    @Override
    public String floorKey(String key) {
        checkNotNullAndContainsNoZeros(key);
        return keyOfStringEntry(dawg.lower(key + DOUBLE_KEY_VALUE_SEPARATOR));
    }

    @Override
    public Entry<String, String> ceilingEntry(String key) {
        checkNotNullAndContainsNoZeros(key);
        return entryOfStringEntry(dawg.ceiling(key + KEY_VALUE_SEPARATOR));
    }

    @Override
    public String ceilingKey(String key) {
        checkNotNullAndContainsNoZeros(key);
        return keyOfStringEntry(dawg.ceiling(key + KEY_VALUE_SEPARATOR));
    }

    @Override
    public Entry<String, String> higherEntry(String key) {
        checkNotNullAndContainsNoZeros(key);
        return entryOfStringEntry(dawg.ceiling(key + DOUBLE_KEY_VALUE_SEPARATOR));
    }

    @Override
    public String higherKey(String key) {
        checkNotNullAndContainsNoZeros(key);
        return keyOfStringEntry(dawg.ceiling(key + DOUBLE_KEY_VALUE_SEPARATOR));
    }

    @Override
    public Entry<String, String> firstEntry() {
        return entryOfStringEntry(dawg.first());
    }

    @Override
    public Entry<String, String> lastEntry() {
        return entryOfStringEntry(dawg.last());
    }

    @Override
    public String firstKey() {
        return keyOfStringEntry(dawg.first());
    }

    @Override
    public String lastKey() {
        return keyOfStringEntry(dawg.last());
    }

    @Override
    public Entry<String, String> pollFirstEntry() {
        return entryOfStringEntry(dawg.pollFirst());
    }

    @Override
    public Entry<String, String> pollLastEntry() {
        return entryOfStringEntry(dawg.pollLast());
    }

    @Override
    public Comparator<? super String> comparator() {
        // Natural ordering.
        return null;
    }

    @Override
    public NavigableSet<String> keySet() {
        return null;
    }

    @Override
    public Collection<String> values() {
        return null;
    }

    @Override
    public NavigableSet<Entry<String, String>> entrySet() {
        return null;
    }

    @Override
    public NavigableSet<String> navigableKeySet() {
        return null;
    }

    @Override
    public NavigableSet<String> descendingKeySet() {
        return null;
    }

    @Override
    public NavigableMap<String, String> descendingMap() {
        return null;
    }

    @Override
    public NavigableMap<String, String> subMap(String fromKey, boolean fromInclusive, String toKey, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableMap<String, String> headMap(String toKey, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableMap<String, String> tailMap(String fromKey, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableMap<String, String> subMap(String fromKey, String toKey) {
        return null;
    }

    @Override
    public NavigableMap<String, String> headMap(String toKey) {
        return null;
    }

    @Override
    public NavigableMap<String, String> tailMap(String fromKey) {
        return null;
    }
    
    private class MapEntry implements Entry<String, String> {
        private final String key;
        private String value;
        
        public MapEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            String old = put(key, value);
            this.value = value;
            return old;
        }
    }
}