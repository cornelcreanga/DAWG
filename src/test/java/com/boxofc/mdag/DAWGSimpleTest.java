/**
 * MDAG is a Java library capable of constructing character-sequence-storing,
 * directed acyclic graphs of minimal size.
 *
 *  Copyright (C) 2012 Kevin Lawson <Klawson88@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.boxofc.mdag;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DAWGSimpleTest {
    private static final Random RANDOM = new Random(System.nanoTime());
    
    @Test
    public void addSimple() {
        String words[] = {
            "a", "xes", "xe", "xs"
        };
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll(words);
        Arrays.sort(words);
        CompressedDAWGSet cdawg = dawg.compress();
        assertEquals(cdawg, dawg.compress());

        int i = 0;
        for (String word : dawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        i = 0;
        for (String word : cdawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        String wordsXe[] = {"xe", "xes"};
        i = 0;
        for (String word : dawg.getStringsStartingWith("xe"))
            assertEquals(wordsXe[i++], word);
        
        i = 0;
        for (String word : cdawg.getStringsStartingWith("xe"))
            assertEquals(wordsXe[i++], word);

        String wordsS[] = {"xes", "xs"};
        Set<String> expected = new HashSet<>(Arrays.asList(wordsS));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("s"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("s"))
            actual.add(word);
        assertEquals(expected, actual);
        
        assertEquals(4, dawg.size());
        assertEquals(4, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
        
        assertEquals(4, cdawg.size());
        assertEquals(4, cdawg.getNodeCount());
        assertEquals(5, cdawg.getTransitionCount());
        
        // Non-existent.
        dawg.remove("b");
        assertEquals(cdawg, dawg.compress());
        cdawg = dawg.compress();

        i = 0;
        for (String word : dawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        i = 0;
        for (String word : cdawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);
        
        assertEquals(4, dawg.size());
        assertEquals(4, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
        
        assertEquals(4, cdawg.size());
        assertEquals(4, cdawg.getNodeCount());
        assertEquals(5, cdawg.getTransitionCount());
        
        dawg.remove("");
        cdawg = dawg.compress();

        i = 0;
        for (String word : dawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        i = 0;
        for (String word : cdawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);
        
        assertEquals(4, dawg.size());
        assertEquals(4, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
        
        assertEquals(4, cdawg.size());
        assertEquals(4, cdawg.getNodeCount());
        assertEquals(5, cdawg.getTransitionCount());
    }
  
    @Test
    public void addCasual() {
        String words[] = {
            "assiez",
            "assions",
            "eriez",
            "erions",
            "eront",
            "iez",
            "ions"
        };
        Set<String> expected = new HashSet<>(Arrays.asList(words));
        for (String w[] : Permutations.from(words)) {
            ModifiableDAWGSet dawg = new ModifiableDAWGSet();
            dawg.addAll(w);
            CompressedDAWGSet cdawg = dawg.compress();
            int i = 0;
            for (String s : dawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);
            
            i = 0;
            for (String s : cdawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);

            Set<String> actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);
        }
    }
  
    @Test
    public void addCasualWithBlank() {
        String words[] = {
            "",
            "assiez",
            "assions",
            "eriez",
            "erions",
            "eront",
            "iez",
            "ions"
        };
        Set<String> expected = new HashSet<>(Arrays.asList(words));
        String removingWord = words[3];
        Set<String> expectedRemoveOne = new HashSet<>();
        for (String word : words)
            if (!word.equals(removingWord))
                expectedRemoveOne.add(word);
        Set<String> expectedRemoveBlank = new HashSet<>();
        for (String word : words)
            if (!word.isEmpty())
                expectedRemoveBlank.add(word);
        for (String w[] : Permutations.from(words)) {
            ModifiableDAWGSet dawg = new ModifiableDAWGSet();
            dawg.addAll(w);
            CompressedDAWGSet cdawg = dawg.compress();
            int i = 0;
            for (String s : dawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);
            
            List<String> list = new ArrayList<>();
            for (String s : dawg.getStrings("", "", true, null, false, null, false))
                list.add(s);
            Collections.reverse(list);
            assertEquals(Arrays.asList(words), list);
            
            i = 0;
            for (String s : cdawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);

            Set<String> actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);
            
            dawg.remove(removingWord);
            cdawg = dawg.compress();
            
            i = 0;
            for (String s : dawg) {
                if (words[i].equals(removingWord))
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);
            
            i = 0;
            for (String s : cdawg) {
                if (words[i].equals(removingWord))
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);

            actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveOne, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveOne, actual);
            
            dawg = new ModifiableDAWGSet();
            dawg.addAll(w);
            dawg.remove("");
            cdawg = dawg.compress();
            
            i = 0;
            for (String s : dawg) {
                if (words[i].isEmpty())
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);
            
            i = 0;
            for (String s : cdawg) {
                if (words[i].isEmpty())
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);

            actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveBlank, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveBlank, actual);
        }
    }
    
    @Test
    public void getStrings() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        String words[] = {
            "aa", "aaa", "aaa", "aab",
            "baaaa", "baba", "babb", "babbc",
            "bac", "baca", "bacb", "bacba",
            "bada", "badb", "badbc", "badd",
            "bb", "bcd", "cac", "cc"
        };
        dawg.addAll(words);
        
        List<String> expected;
        List<String> actual;
        
        expected = Arrays.asList("bac", "baca", "bacb", "bacba");
        for (int desc = 0; desc < 2; desc++) {
            if (desc == 1)
                Collections.reverse(expected);
            actual = new ArrayList<>();
            for (String word : dawg.getStrings("ba", "", desc == 1, "bac", true, "bad", true))
                actual.add(word);
            assertEquals(expected, actual);
        }
        
        expected = Arrays.asList("bac", "baca", "bacb", "bacba", "bada", "badb");
        for (int desc = 0; desc < 2; desc++) {
            if (desc == 1)
                Collections.reverse(expected);
            actual = new ArrayList<>();
            for (String word : dawg.getStrings("ba", "", desc == 1, "bac", true, "badb", true))
                actual.add(word);
            assertEquals(expected, actual);
        }
        
        expected = Arrays.asList("bacb", "bacba", "bada", "badb", "badbc", "badd");
        for (int desc = 0; desc < 2; desc++) {
            if (desc == 1)
                Collections.reverse(expected);
            actual = new ArrayList<>();
            for (String word : dawg.getStrings("ba", "", desc == 1, "bacb", true, "badd", true))
                actual.add(word);
            assertEquals(expected, actual);
        }
        
        expected = Arrays.asList("bac", "baca", "bacb", "bacba", "bada", "badb", "badbc");
        for (int desc = 0; desc < 2; desc++) {
            if (desc == 1)
                Collections.reverse(expected);
            actual = new ArrayList<>();
            for (String word : dawg.getStrings("ba", "", desc == 1, "bac", true, "badc", true))
                actual.add(word);
            assertEquals(expected, actual);
        }
    }
    
    @Test
    public void to() {
        String words[] = {"", "b"};
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll(words);
        
        List<String> expected = Collections.EMPTY_LIST;
        List<String> actual = new ArrayList<>();
        for (String word : dawg.getStrings("", "", false, "", false, "a", false))
            actual.add(word);
        assertEquals(expected, actual);
    }
    
    @Test
    public void range() {
        String words[] = {"hddb", "hddd", "hddf", "hddh", "hdf", "hdfb", "hdfd", "hdff", "hdfh", "hdh", "hdhb", "hdhd", "hdhf", "hdhh", "hf", "hfb", "hfbb", "hfbd", "hfbf", "hfbh", "hfd", "hfdb", "hfdd", "hfdf", "hfdh", "hff", "hffb", "hffd", "hfff", "hffh", "hfh", "hfhb", "hfhd", "hfhf", "hfhh"};
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll(words);
        
        List<String> expected = Arrays.asList(words);
        List<String> actual = new ArrayList<>();
        for (String word : dawg.getStrings("", "", false, "hdd", false, "hgecc", false))
            actual.add(word);
        assertEquals(expected, actual);
    }
    
    @Test
    public void rangeDesc() {
        String words[] = {"bhhh", "bhhf", "bhhd", "bhhb", "bhh", "bhfh", "bhff", "bhfd", "bhfb", "bhf", "bhdh", "bhdf", "bhdd", "bhdb", "bhd", "bhbh", "bhbf", "bhbd", "bhbb", "bhb", "bh", "bfhh", "bfhf", "bfhd", "bfhb", "bfh", "bffh", "bfff", "bffd", "bffb", "bff", "bfdh", "bfdf", "bfdd", "bfdb"};
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll(words);
        
        List<String> expected = Arrays.asList(words);
        List<String> actual = new ArrayList<>();
        for (String word : dawg.getStrings("", "", true, "bfd", false, "cdgd", false))
            actual.add(word);
        assertEquals(expected, actual);
    }
    
    @Test
    public void getStringsAll() {
        for (int attempt = 0; attempt < 10; attempt++) {
            NavigableSet<String> wordsSet = new TreeSet<>();
            for (int i = 0; i < 625; i++)
                if (attempt < 2 || RANDOM.nextBoolean() || RANDOM.nextBoolean())
                    wordsSet.add(Integer.toString(i, 5).replace('1', 'b').replace('2', 'd').replace('3', 'f').replace('4', 'h').replace("0", ""));
            if (RANDOM.nextBoolean())
                wordsSet.add("");
            String words[] = wordsSet.toArray(new String[wordsSet.size()]);
            ModifiableDAWGSet dawg = new ModifiableDAWGSet(wordsSet);
            CompressedDAWGSet cdawg = dawg.compress();
            
            NavigableSet<String> patternsSet = new TreeSet<>();
            while (patternsSet.size() < 9) {
                int i = RANDOM.nextInt(100000);
                char s[] = String.valueOf(i).replace("0", "").toCharArray();
                for (int j = 0; j < s.length; j++)
                    s[j] = (char)(s[j] - '1' + 'a');
                patternsSet.add(String.valueOf(s));
            }
            patternsSet.add("");
            for (int i = 0; i < 4; i++)
                patternsSet.add(words[RANDOM.nextInt(words.length)]);
            String patterns[] = patternsSet.toArray(new String[patternsSet.size()]);
            
            for (String prefix : patterns) {
                for (String substring : patterns) {
                    for (String from : patterns) {
                        for (String to : patterns) {
                            for (int inclFrom = 0; inclFrom < 2; inclFrom++) {
                                boolean inclF = inclFrom == 1;
                                for (int inclTo = 0; inclTo < 2; inclTo++) {
                                    boolean inclT = inclTo == 1;
                                    for (int desc = 0; desc < 2; desc++) {
                                        boolean descending = desc == 1;
                                        List<String> actual = new ArrayList<>();
                                        for (String s : dawg.getStrings(prefix, substring, descending, from, inclF, to, inclT))
                                            actual.add(s);
                                        List<String> expected = getStrings(words, prefix, substring, descending, from, inclF, to, inclT);
                                        assertEquals("Prefix: " + prefix + ", substring: " + substring + ", " + (inclF ? "[ " : "( ") + from + " .. " + to + (inclT ? " ]" : " )") + ", " + (descending ? "desc" : "asc"), expected, actual);
                                        
                                        actual = new ArrayList<>();
                                        for (String s : cdawg.getStrings(prefix, substring, descending, from, inclF, to, inclT))
                                            actual.add(s);
                                        assertEquals("Prefix: " + prefix + ", substring: " + substring + ", " + (inclF ? "[ " : "( ") + from + " .. " + to + (inclT ? " ]" : " )") + ", " + (descending ? "desc" : "asc"), expected, actual);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static List<String> getStrings(String words[], String prefix, String substring, boolean desc, String from, boolean inclFrom, String to, boolean inclTo) {
        List<String> ret = new ArrayList<>();
        for (String word : words) {
            if (!word.startsWith(prefix) || !word.contains(substring))
                continue;
            int cmp = word.compareTo(from);
            if (cmp < 0 || cmp == 0 && !inclFrom)
                continue;
            cmp = word.compareTo(to);
            if (cmp > 0 || cmp == 0 && !inclTo)
                continue;
            ret.add(word);
        }
        if (desc)
            Collections.reverse(ret);
        return ret;
    }

    @Test
    public void add() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        String words[] = {
            "aient", "ais", "ait", "ai", "ant",
            "assent", "asses", "asse", "assiez", "assions", "as", "a",
            "ent", "eraient", "erais", "erait", "erai", "eras", "era",
            "erez", "eriez", "erions",
            "erons", "eront", "er", "es", "ez", "e",
            "iez", "ions", "ons", "qmes", "qtes", "qt",
            "wrent", "xes", "xe", "xs", "x"
        };
        dawg.addAll(words);
        CompressedDAWGSet cdawg = dawg.compress();

        Arrays.sort(words);
        int i = 0;
        for (String word : dawg)
            assertEquals(words[i++], word);
        assertEquals(words.length, i);
        
        i = 0;
        for (String word : cdawg)
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        String wordsAs[] = {"as", "asse", "assent", "asses", "assiez", "assions"};
        i = 0;
        for (String word : dawg.getStringsStartingWith("as"))
            assertEquals(wordsAs[i++], word);
        
        i = 0;
        for (String word : cdawg.getStringsStartingWith("as"))
            assertEquals(wordsAs[i++], word);

        String wordsOns[] = {"assions", "erions", "erons", "ions", "ons"};
        Set<String> expected = new HashSet<>(Arrays.asList(wordsOns));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("ons"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("ons"))
            actual.add(word);
        assertEquals(expected, actual);

        String wordsXe[] = {"xe"};
        expected = new HashSet<>(Arrays.asList(wordsXe));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("xe"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("xe"))
            actual.add(word);
        assertEquals(expected, actual);
        
        assertEquals(25, dawg.getNodeCount());
        assertEquals(39, dawg.size());
        
        assertEquals(25, cdawg.getNodeCount());
        assertEquals(39, cdawg.size());
        
        int maxLength = Arrays.stream(words).mapToInt(s -> s.length()).max().orElse(0);
        assertEquals(maxLength, cdawg.getMaxLength(cdawg.sourceNode, 0));
    }

    @Test(expected = NoSuchElementException.class)
    public void empty() {
        DAWGSet dawg = new ModifiableDAWGSet();
        assertFalse(dawg.iterator().hasNext());
        dawg.iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyCompressed() {
        CompressedDAWGSet dawg = new ModifiableDAWGSet().compress();
        assertEquals(0, dawg.getMaxLength(dawg.sourceNode, 0));
        assertFalse(dawg.iterator().hasNext());
        dawg.iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySuffix() {
        DAWGSet dawg = new ModifiableDAWGSet();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySuffixCompressed() {
        DAWGSet dawg = new ModifiableDAWGSet().compress();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyCollection() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyCollectionCompressed() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll();
        CompressedDAWGSet cdawg = dawg.compress();
        assertFalse(cdawg.getStringsEndingWith("").iterator().hasNext());
        cdawg.getStringsEndingWith("").iterator().next();
    }

    @Test
    public void file() throws IOException {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        // Source: http://www.mieliestronk.com/wordlist.html
        try (FileInputStream fis = new FileInputStream("corncob_lowercase.txt")) {
            dawg.addAll(fis);
        }
        CompressedDAWGSet cdawg = dawg.compress();
        ModifiableDAWGSet udawg = cdawg.uncompress();
        
        int i = 0;
        for (String word : dawg)
            i++;
        assertEquals(58109, i);
        
        i = 0;
        for (String word : cdawg)
            i++;
        assertEquals(58109, i);
        
        i = 0;
        for (String word : udawg)
            i++;
        assertEquals(58109, i);

        i = 0;
        for (String word : dawg.getStringsEndingWith(""))
            i++;
        assertEquals(58109, i);

        i = 0;
        for (String word : cdawg.getStringsEndingWith(""))
            i++;
        assertEquals(58109, i);

        i = 0;
        for (String word : udawg.getStringsEndingWith(""))
            i++;
        assertEquals(58109, i);
    }

    @Test
    public void blankCollection() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll("");
        CompressedDAWGSet cdawg = dawg.compress();
        assertEquals(0, cdawg.getMaxLength(cdawg.sourceNode, 0));

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void blank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
        
        dawg.remove("");
        cdawg = dawg.compress();

        assertFalse(dawg.iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());

        assertFalse(cdawg.iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("").iterator().hasNext());
    }

    @Test
    public void shortWord() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("a");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
        
        dawg.remove("a");
        cdawg = dawg.compress();

        assertFalse(dawg.iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());

        assertFalse(cdawg.iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("").iterator().hasNext());
    }

    @Test
    public void zero() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("\0");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void wordWithBlank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        dawg.add("add");
        CompressedDAWGSet cdawg = dawg.compress();

        Set<String> expected = new HashSet<>(Arrays.asList("", "add"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        dawg.add("a");
        cdawg = dawg.compress();
        expected = new HashSet<>(Arrays.asList("", "a", "add"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        dawg.add("ad");
        cdawg = dawg.compress();
        expected = new HashSet<>(Arrays.asList("", "a", "ad", "add"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        dawg.remove("");
        cdawg = dawg.compress();
        expected = new HashSet<>(Arrays.asList("a", "ad", "add"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
    }

    @Test
    public void shortWordWithBlank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        dawg.add("a");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("", "a"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void zeroWithBlank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        dawg.add("\0");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("", "\0"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void similarBeginningAndEnd() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("tet");
        dawg.add("tetatet");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("tet", "tetatet"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        expected = new HashSet<>(Arrays.asList("tet", "tetatet"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("teta").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("teta").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("atet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("atet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void oneWordPartOfAnother() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("tet");
        dawg.add("tetra");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("tet", "tetra"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        expected = new HashSet<>(Arrays.asList("tet"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);

        expected = new HashSet<>(Arrays.asList("tet"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("t"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("t"))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("tetr").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tetr").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("etra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("etra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }
}