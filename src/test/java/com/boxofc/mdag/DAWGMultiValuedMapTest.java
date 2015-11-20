package com.boxofc.mdag;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DAWGMultiValuedMapTest {
    @Test(expected = IllegalArgumentException.class)
    public void getAndPutWithZeros() {
        ModifiableDAWGMultiValuedMap map = new ModifiableDAWGMultiValuedMap();
        Set<String> set = map.get("a");
        set.add("c\0b");
    }
    
    @Test
    public void putAll() {
        Map<String, Set<String>> data = new HashMap<>();
        Set<String> set = new HashSet<>();
        set.add("0");
        set.add("1");
        set.add("2");
        data.put("a", set);
        Set<String> another = new HashSet<>();
        another.add("3");
        another.add("4");
        another.add("5");
        data.put("b", another);
        ModifiableDAWGMultiValuedMap map = new ModifiableDAWGMultiValuedMap();
        map.putAll(data);
        assertEquals(6, map.size());
        assertEquals(3, map.get("a").size());
        assertEquals(3, map.get("b").size());
        assertEquals(0, map.get("c").size());
        assertEquals(set, map.get("a"));
        assertEquals(another, map.get("b"));
        assertEquals(Collections.EMPTY_SET, map.get("c"));
    }
}