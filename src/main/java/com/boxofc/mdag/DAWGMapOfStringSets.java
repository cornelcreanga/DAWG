package com.boxofc.mdag;

import com.boxofc.mdag.util.LookaheadIterator;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

class DAWGMapOfStringSets extends AbstractDAWGMap<Set<String>> {
    DAWGMapOfStringSets() {
    }
    
    DAWGMapOfStringSets(DAWGSet dawg) {
        super(dawg);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value instanceof String)
            return super.containsValue(value);
        Iterator<Entry<String, Set<String>>> i = entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, Set<String>> e = i.next();
            if (value.equals(e.getValue()))
                return true;
        }
        return false;
    }

    @Override
    public boolean removeValue(Object value) {
        if (value instanceof String)
            return super.removeValue(value);
        Iterator<Entry<String, Set<String>>> i = entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, Set<String>> e = i.next();
            if (value.equals(e.getValue())) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> get(Object key) {
        checkNotNullAndContainsNoZeros(key);
        return new ValuesSetFromIterable(dawg.getStringsStartingWith((String)key + KEY_VALUE_SEPARATOR), (String)key);
    }

    @Override
    public Set<String> remove(Object key) {
        Set<String> ret = get((String)key);
        if (ret.isEmpty())
            ret = Collections.EMPTY_SET;
        else // An unmodifiable set of previously stored data.
            ret = new ModifiableDAWGSet(false, ret).compress();
        for (String s : ret)
            dawg.remove((String)key + KEY_VALUE_SEPARATOR + s);
        return ret;
    }
    
    public boolean put(String key, String value) {
        checkNotNullAndContainsNoZeros(key);
        checkNotNullAndContainsNoZeros(value);
        return dawg.add(key + KEY_VALUE_SEPARATOR + value);
    }

    @Override
    public Set<String> put(String key, Set<String> value) {
        Set<String> ret = get(key);
        if (ret.isEmpty())
            ret = null;
        else // An unmodifiable set of previously stored data.
            ret = new ModifiableDAWGSet(false, ret).compress();
        putAll(key, value);
        return ret;
    }
    
    public boolean putAll(String key, Iterable<? extends String> values) {
        return dawg.addAll(new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    private final Iterator<? extends String> it = values.iterator();

                    @Override
                    public String next() {
                        String s = it.next();
                        checkNotNullAndContainsNoZeros(s);
                        return key + KEY_VALUE_SEPARATOR + s;
                    }

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                };
            }
        });
    }

    @Override
    public void putAll(Map m) {
        Set entrySet = m.entrySet();
        Iterator entryIt = entrySet.iterator();
        if (!entryIt.hasNext())
            return;
        Entry entry = (Entry)entryIt.next();
        String key = (String)entry.getKey();
        Object value = entry.getValue();
        if (value instanceof Set) {
            put(key, (Set)value);
            dawg.addAll(new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return new LookaheadIterator<String>() {
                        private String key;
                        private Iterator<String> it;

                        @Override
                        public String nextElement() {
                            while (true) {
                                if (key == null) {
                                    if (entryIt.hasNext()) {
                                        Entry e = (Entry)entryIt.next();
                                        key = (String)e.getKey();
                                        checkNotNullAndContainsNoZeros(key);
                                        it = ((Set<String>)e.getValue()).iterator();
                                    } else
                                        throw new NoSuchElementException();
                                }
                                if (it.hasNext()) {
                                    String value = it.next();
                                    checkNotNullAndContainsNoZeros(value);
                                    return key + KEY_VALUE_SEPARATOR + value;
                                } else
                                    key = null;
                            }
                        }
                    };
                }
            });
        } else {
            put(key, (String)value);
            dawg.addAll(new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return new Iterator<String>() {
                        @Override
                        public String next() {
                            Entry e = (Entry)entryIt.next();
                            String key = (String)e.getKey();
                            checkNotNullAndContainsNoZeros(key);
                            String value = (String)e.getValue();
                            checkNotNullAndContainsNoZeros(value);
                            return key + KEY_VALUE_SEPARATOR + value;
                        }

                        @Override
                        public boolean hasNext() {
                            return entryIt.hasNext();
                        }
                    };
                }
            });
        }
    }
    
    @Override
    public Entry<String, Set<String>> lowerEntry(String key) {
        key = lowerKey(key);
        return key == null ? null : new MapEntry(key, get(key));
    }
    
    @Override
    public Entry<String, Set<String>> floorEntry(String key) {
        key = floorKey(key);
        return key == null ? null : new MapEntry(key, get(key));
    }
    
    @Override
    public Entry<String, Set<String>> ceilingEntry(String key) {
        key = ceilingKey(key);
        return key == null ? null : new MapEntry(key, get(key));
    }
    
    @Override
    public Entry<String, Set<String>> higherEntry(String key) {
        key = higherKey(key);
        return key == null ? null : new MapEntry(key, get(key));
    }
    
    @Override
    public Entry<String, Set<String>> firstEntry() {
        String key = firstKey();
        return key == null ? null : new MapEntry(key, get(key));
    }
    
    @Override
    public Entry<String, Set<String>> lastEntry() {
        String key = lastKey();
        return key == null ? null : new MapEntry(key, get(key));
    }
    
    @Override
    public Entry<String, Set<String>> pollFirstEntry() {
        String key = firstKey();
        return key == null ? null : new MapEntry(key, remove(key));
    }
    
    @Override
    public Entry<String, Set<String>> pollLastEntry() {
        String key = lastKey();
        return key == null ? null : new MapEntry(key, remove(key));
    }
    
    private class ValuesSetFromIterable extends AbstractSet<String> implements Set<String> {
        private final Iterable<String> values;
        private final String key;
        private int size = -1;
        
        public ValuesSetFromIterable(Iterable<String> values, String key) {
            this.values = values;
            this.key = key;
        }

        @Override
        public int size() {
            if (size < 0) {
                int s = 0;
                for (String value : values)
                    s++;
                if (dawg.isImmutable())
                    size = s;
                else
                    return s;
            }
            return size;
        }

        @Override
        public boolean isEmpty() {
            return size < 0 ? !values.iterator().hasNext() : size == 0;
        }

        @Override
        public Iterator<String> iterator() {
            return values.iterator();
        }

        @Override
        public boolean add(String e) {
            checkNotNullAndContainsNoZeros(e);
            return dawg.add(key + KEY_VALUE_SEPARATOR + e);
        }

        @Override
        public boolean remove(Object o) {
            checkNotNullAndContainsNoZeros(o);
            return dawg.remove(key + KEY_VALUE_SEPARATOR + o);
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            return dawg.addAll(new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return new Iterator<String>() {
                        private final Iterator<? extends String> it = c.iterator();
                        
                        @Override
                        public String next() {
                            String s = it.next();
                            checkNotNullAndContainsNoZeros(s);
                            return key + KEY_VALUE_SEPARATOR + s;
                        }

                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }
                    };
                }
            });
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean ret = false;
            for (Object e : c)
                ret |= remove((String)e);
            return ret;
        }
    }
}